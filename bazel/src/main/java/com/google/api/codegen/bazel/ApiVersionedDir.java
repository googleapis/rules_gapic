// Copyright 2021 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.codegen.bazel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A class representing versioned API directory.
// For example: google/example/library/v1
class ApiVersionedDir {
  private static final Pattern PROTO_PACKAGE =
      Pattern.compile("(?m)^package\\s+(?<protoPackage>[\\w+\\.]+)\\s*;\\s*$");
  private static final Pattern IMPORTS =
      Pattern.compile("(?m)^import\\s+\"(?<import>[\\w+\\\\./]+)\"\\s*;\\s*$");
  // A proto's language package options.
  private static final Pattern PROTO_LANG_PACKAGE =
      Pattern.compile(
          "(?m)^option\\s+(?<optName>(java|go|csharp|ruby|php|javascript)_(namespace|package))\\s+=\\s+\"(?<optValue>[\\w./;\\\\\\-]+)\"\\s*;\\s*$");
  private static final Pattern SERVICE =
      Pattern.compile("(?m)^service\\s+(?<service>\\w+)\\s+(\\{)*\\s*$");
  private static final Pattern LANG_PACKAGES =
      // Yes, this is an unreadable mess. Replace it with proper yaml parsing if regex
      // ever goes worse than this.
      Pattern.compile(
          "(?m)\\b(?<lang>java|python|go|csharp|ruby|php|nodejs)\\s*:\\s*$\\s+"
              + "package_name\\s*:\\s*(?<package>[\\w\\.\\/:\\\\]*)\\s*$\\s+"
              + "(interface_names:"
              + "(?<interfaceNames>(\\s*$\\s+\\w+\\.[\\w\\.]*\\s*:\\s*\\w+\\s*$)*))?");

  private static final Pattern INTERFACE_NAMES =
      Pattern.compile("(?m)\\s*(?<name>[\\w\\.]*)\\s*:\\s*(?<val>\\w+)\\s*");

  private static Pattern GAPIC_YAML_TYPE =
      Pattern.compile("(?m)^type\\s*:\\s*com.google.api.codegen.ConfigProto\\s*$");

  private static Pattern SERVICE_YAML_TYPE =
      Pattern.compile("(?m)^type\\s*:\\s*google.api.Service\\s*$");

  private static String CLOUD_AUTH_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

  private static String LOCATIONS_MIXIN = "name: google.cloud.location.Locations";

  private static String IAM_POLICY_MIXIN = "name: google.iam.v1.IAMPolicy";

  private static final String[] PRESERVED_PROTO_LIBRARY_STRING_ATTRIBUTES = {
    // Multiple languages:
    "package_name",
    "transport",
    // TypeScript:
    "main_service",
    "bundle_config",
    "iam_service",
    "mixins",
    // Ruby:
    "ruby_cloud_title",
    "ruby_cloud_description",
    // C#:
    "generate_nongapic_package",
    // Other languages: add below
  };

  // Mapping whose keys are the names of non-string-valued attributes whose pre-existing values, if any, should
  // override what would otherwise be generated for a brand-new file. Moreover, in some cases, if
  // we're reading a pre-existing file but the attribute is not found, we want to wind up with a
  // specific value in the final generated file that is different from what we would use if we were
  // generating the file for the first time. In that case, we will use the value in this map for
  // that attribute. (For example, for languages that support numeric enums: in a brand-new BUILD file,
  // we will generate `rest_numeric_enums=True`, but when updating an existing BUILD file that does
  // not mention `rest_numeric_enums`, we will generate `rest_numeric_enums=False` for backwards
  // compatibility, leaving it to humans to change the value explicitly.)
  private static final Map<String, String> PRESERVED_PROTO_LIBRARY_NONSTRING_ATTRIBUTES = new HashMap<>();

  private static final String[] PRESERVED_PROTO_LIBRARY_LIST_ATTRIBUTES = {
    // All languages:
    "extra_protoc_parameters",
    "extra_protoc_file_parameters",
    // Python:
    "opt_args",
    // Other languages: add below
  };

  // A reference to the object representing the parent dir of this versioned API dir.
  // For example: google/example/library.
  private ApiDir parent;

  // Values of the 'package' properties parsed from all protos in this versioned directory.
  // All protos in same directory must belong to the same package.
  // For example: "google.example.library.v1".
  private String protoPackage;

  // A map of '<lang>' to '<lang>_package` properties (like 'go_package' or 'java_package' in all
  // proto files in this versioned dir).
  // For example: {
  //   "go": "google.golang.org/genproto/googleapis/example/library/v1;library",
  //   "java": "com.google.example.library.v1"
  // }
  private final Map<String, String> langProtoPackages = new HashMap<>();

  // A set of proto imports parsed from all protos in this versioned directory.
  // For example: {"google/api/annotations.proto", "google/api/client.proto"}.
  private final Set<String> imports = new TreeSet<>();

  // A set of all protos found in this versioned directory.
  // For example: {"library.proto"}
  private final Set<String> protos = new TreeSet<>();

  // A set of all 'service <ServiceName>' definitions found in all protos in this versioned dir.
  // For example: {"LibraryService"}
  private final Set<String> services = new TreeSet<>();

  // A map of '<lang>' to 'language_settings.<lang>.package_name' property values parsed from gapic
  // yaml in this versioned directory.
  // For example: {
  //   "java": "com.google.cloud.example.library.v1",
  //   "python": "google.cloud.example.library_v1.gapic",
  //   // ...
  // }
  private final Map<String, String> langGapicPackages = new HashMap<>();

  // A map with service interface names overrides ('language_settings.<lang>.interface_names'
  // values from gapic yaml).
  // For example: {
  //   "java": "google.example.library.v1.LibraryService: Library"
  // }
  private final Map<String, Map<String, String>> langGapicNameOverrides = new HashMap<>();

  // Path to service yaml (file name if it is in this directory or path starting from repo root if
  // it is in a different directory).
  private String serviceYamlPath;

  // Path to gapic yaml (file name if it is in this directory or path starting from repo root if
  // it is in a different directory).
  private String gapicYamlPath;

  // Path to service config json  (file name if it is in this directory or path starting from repo
  // root if it is in a different directory).
  private String serviceConfigJsonPath;

  // Name of this API. It is calculated from protos package name and is equal to the last
  // non-versioned component of a package name.
  // For example, if package='google.example.library.v1', then name is 'library'.
  private String name;

  // Name of assembly.
  // For example:
  //   - if it is a top-level API: "library"
  //   - if it is a sub-API (like library/admin): "library-admin"
  //
  private String assemblyName;

  // Version of this verioned API.
  // For example: "v1"
  private String version;

  // Flag indicating Cloud Auth Scope presence in the service yaml.
  // For example:
  // authentication:
  //   rules:
  //   - selector: 'google.example.library.v1.LibraryService.*'
  //     oauth:
  //       canonical_scopes: |-
  //         https://www.googleapis.com/auth/cloud-platform
  private boolean cloudScope;

  private boolean containsLocations;

  private boolean containsIAMPolicy;

  // Names of *_gapic_assembly_* rules (since they may be overridden by the user)
  private final Map<String, String> assemblyPkgRulesNames = new HashMap<>();

  // Attributes of *_gapic_library rules to be overridden. The keys for these maps are the names of
  // the library rules.
  private final Map<String, Map<String, String>> overriddenStringAttributes = new HashMap<>();
  private final Map<String, Map<String, String>> overriddenNonStringAttributes = new HashMap<>();
  private final Map<String, Map<String, List<String>>> overriddenListAttributes = new HashMap<>();

  ApiVersionedDir() {
    // Multiple languages:
    PRESERVED_PROTO_LIBRARY_NONSTRING_ATTRIBUTES.put("rest_numeric_enums", "False");
    // Specific languages: add below
  }

  void setParent(ApiDir parent) {
    this.parent = parent;
  }

  String getProtoPackage() {
    return protoPackage;
  }

  Map<String, String> getLangProtoPackages() {
    return Collections.unmodifiableMap(langProtoPackages);
  }

  Set<String> getImports() {
    return Collections.unmodifiableSet(imports);
  }

  Set<String> getProtos() {
    return Collections.unmodifiableSet(protos);
  }

  Set<String> getServices() {
    return Collections.unmodifiableSet(services);
  }

  Map<String, String> getLangGapicPackages() {
    return Collections.unmodifiableMap(langGapicPackages);
  }

  Map<String, Map<String, String>> getLangGapicNameOverrides() {
    return Collections.unmodifiableMap(langGapicNameOverrides);
  }

  String getServiceYamlPath() {
    return serviceYamlPath;
  }

  String getGapicYamlPath() {
    return gapicYamlPath;
  }

  String getServiceConfigJsonPath() {
    return serviceConfigJsonPath;
  }

  String getName() {
    return name;
  }

  String getAssemblyName() {
    return assemblyName;
  }

  String getVersion() {
    return version;
  }

  boolean getCloudScope() {
    return cloudScope;
  }

  Map<String, Map<String, String>> getOverriddenStringAttributes() {
    return overriddenStringAttributes;
  }

  Map<String, Map<String, String>> getOverriddenNonStringAttributes() {
    return overriddenNonStringAttributes;
  }

  Map<String, Map<String, List<String>>> getOverriddenListAttributes() {
    return overriddenListAttributes;
  }

  Map<String, String> getAssemblyPkgRulesNames() {
    return assemblyPkgRulesNames;
  }

  boolean hasLocations() {
    return this.containsLocations;
  }

  boolean hasIAMPolicy() {
    return this.containsIAMPolicy;
  }

  void parseYamlFile(String fileName, String fileBody) {
    // It is a gapic yaml
    Matcher m = GAPIC_YAML_TYPE.matcher(fileBody);
    if (m.find()) {
      gapicYamlPath = fileName;

      m = LANG_PACKAGES.matcher(fileBody);
      while (m.find()) {
        String lang = m.group("lang");
        langGapicPackages.put(lang, m.group("package"));
        String interfaceNames = m.group("interfaceNames");
        Map<String, String> interfaceSubs = new HashMap<>();
        if (interfaceNames != null) {
          Matcher subM = INTERFACE_NAMES.matcher(interfaceNames);
          while (subM.find()) {
            interfaceSubs.put(subM.group("name"), subM.group("val"));
          }
        }
        langGapicNameOverrides.put(lang, interfaceSubs);
      }

      return;
    }

    // It is a service yaml
    m = SERVICE_YAML_TYPE.matcher(fileBody);
    if (m.find()) {
      serviceYamlPath = fileName;

      // API Servic config specifies the use of the Cloud oauth scope.
      this.cloudScope = fileBody.contains(CLOUD_AUTH_SCOPE);

      // API Serivce config has Locations API.
      if (fileBody.contains(LOCATIONS_MIXIN)) {
        this.containsLocations = true;
      }

      // API Serivce config has IAMPolicy API.
      if (fileBody.contains(IAM_POLICY_MIXIN)) {
        this.containsIAMPolicy = true;
      }
    }
  }

  void parseProtoFile(String fileName, String fileBody) {
    protos.add(fileName);

    // Parse proto package
    Matcher m = PROTO_PACKAGE.matcher(fileBody);
    if (m.find() && (protoPackage == null || name == null || version == null)) {
      protoPackage = m.group("protoPackage");
      String[] tokens = protoPackage.split("\\.");
      if (tokens.length >= 1 && name == null) {
        name = tokens[tokens.length - 1];
        assemblyName = name;
      }
      // Trying to figure out version of the versioned API in the most straightforward way,
      // by looking into the versioned part of the package, which should be the last component
      // of the package. Assuming that package must have at least 2 components (otherwise version
      // would be the top-level name and probably mean something different).
      if (tokens.length >= 2) {
        String ver = tokens[tokens.length - 1];
        // Count as "version" anything which is the last package component, starts with 'v'
        // character and is followed by a number
        if (ver.length() >= 2 && ver.startsWith("v") && Character.isDigit(ver.charAt(1))) {
          version = ver;
          name = tokens[tokens.length - 2];
          assemblyName = name;
          // In case if package has at least 3 components it might be a sub-api
          // (like bigtable/admin). Include name of the top package (i.e. name of the parent
          // api) to the assemblyName of this sub API.
          if (tokens.length >= 3) {
            String topPackage = tokens[tokens.length - 3];
            if (!"google".equals(topPackage) && !"cloud".equals(topPackage)) {
              assemblyName = topPackage + '-' + name;
            }
          }
        }
      }
    }

    // Parse imports
    m = IMPORTS.matcher(fileBody);
    while (m.find()) {
      imports.add(m.group("import"));
    }

    // Parse file-level options
    m = PROTO_LANG_PACKAGE.matcher(fileBody);
    while (m.find()) {
      String optName = m.group("optName");
      String optValue = m.group("optValue");
      optName = optName.split("_")[0];
      if (!langProtoPackages.containsKey(optName)) {
        langProtoPackages.put(optName, optValue);
      }
    }

    // Parse declared service names
    m = SERVICE.matcher(fileBody);
    while (m.find()) {
      services.add(m.group("service"));
    }
  }

  void parseJsonFile(String fileName, String fileBody) {
    if (fileBody.contains("methodConfig")) {
      serviceConfigJsonPath = fileName;
    }
  }

  // Parses `file` and stored attributes that will need to be preserved when updating the file.
  void parseBazelBuildFile(Path file) {
    try {
      Buildozer buildozer = Buildozer.getInstance();

      // We cannot and we do not want to preserve all the content of the file.
      // We will let the user edit just the following:
      // - names of the final targets (*_gapic_assembly_*) because they are user-facing;
      // - extra protoc plugin parameters for *_gapic_library rules.
      List<String> allRules = buildozer.execute(file, "print kind name", "*");
      for (String rule : allRules) {
        String[] split = rule.split(" ");
        if (split.length != 2) {
          // some rules e.g. package() don't have "name" attribute, just skip them
          continue;
        }
        String kind = split[0];
        String name = split[1];
        if (kind.contains("_gapic_assembly_")) {
          if (this.assemblyPkgRulesNames.containsKey(kind)) {
            // Duplicated rule of the same kind will break our logic for preserving rule name.
            System.err.println(
                String.format(
                    "There is more than one rule of kind %s. Bazel build file generator does not"
                        + " support regenerating BUILD.bazel in this case.  Please run it with"
                        + " --overwrite option to overwrite the existing BUILD.bazel completely.",
                    kind));
            throw new RuntimeException("Duplicated rule " + kind);
          }
          this.assemblyPkgRulesNames.put(kind, name);
        } else if (kind.endsWith("_gapic_library")) {
          this.overriddenStringAttributes.put(name, new HashMap<>());
          this.overriddenNonStringAttributes.put(name, new HashMap<>());
          this.overriddenListAttributes.put(name, new HashMap<>());

          for (String attr : PRESERVED_PROTO_LIBRARY_STRING_ATTRIBUTES) {
            String value = buildozer.getAttribute(file, name, attr);
            if (value != null) {
              this.overriddenStringAttributes.get(name).put(attr, value);
            }
          }

          for (Map.Entry<String, String> entry : PRESERVED_PROTO_LIBRARY_NONSTRING_ATTRIBUTES.entrySet()) {
            String attr = entry.getKey();
            String newDefaultValue = entry.getValue();
            String value = buildozer.getAttribute(file, name, attr);
            if (value != null) {
              // If a pre-existing value exists, override with that.
              this.overriddenNonStringAttributes.get(name).put(attr, value);
            } else {
              // Otherwise, override with the appropriate default for upgraded files
              if (newDefaultValue != null) {
                this.overriddenNonStringAttributes.get(name).put(attr, newDefaultValue);
              }
            }
          }

          for (String attr : PRESERVED_PROTO_LIBRARY_LIST_ATTRIBUTES) {
            String value = buildozer.getAttribute(file, name, attr);
            if (value != null && value.startsWith("[") && value.endsWith("]")) {
              value = value.substring(1, value.length() - 1);
              String[] values = value.split(" ");
              this.overriddenListAttributes.get(name).put(attr, Arrays.asList(values));
            }
          }
        }
      }
    } catch (IOException exception) {
      System.err.println(
          "Error parsing BUILD.bazel file in " + file.toString() + ": " + exception.toString());
    }
  }

  void injectFieldsFromTopLevel() {
    if (parent == null || serviceYamlPath != null) {
      return;
    }

    String topLevelServiceYaml = parent.getServiceYamlPaths().get(version);
    if (topLevelServiceYaml == null) {
      topLevelServiceYaml = parent.getServiceYamlPaths().get("");
    }
    if (topLevelServiceYaml != null && version != null) {
      serviceYamlPath = version + '/' + topLevelServiceYaml;
    }

    boolean topLevelCloudScope = parent.getCloudScopes().getOrDefault(version, false);
    cloudScope = topLevelCloudScope ? topLevelCloudScope : cloudScope;

    boolean topLevelContainsLocations = parent.getContainsLocations().getOrDefault(version, false);
    containsLocations = topLevelContainsLocations ? topLevelContainsLocations : containsLocations;

    boolean topLevelContainsIAMPolicy = parent.getContainsIAMPolicy().getOrDefault(version, false);
    containsIAMPolicy = topLevelContainsIAMPolicy ? topLevelContainsIAMPolicy : containsIAMPolicy;
  }
}

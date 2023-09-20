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

import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.util.Map.entry;

class BazelBuildFileView {
  private static final String COMMON_RESOURCES_PROTO = "//google/cloud:common_resources_proto";
  private static final Pattern LABEL_NAME = Pattern.compile(":\\w+$");
  private final Map<String, String> tokens = new HashMap<>();
  private final Map<String, Map<String, String>> overriddenStringAttributes = new HashMap<>();
  private final Map<String, Map<String, String>> overriddenNonStringAttributes = new HashMap<>();
  private final Map<String, Map<String, List<String>>> overriddenListAttributes = new HashMap<>();
  private final Map<String, String> assemblyPkgRulesNames = new HashMap<>();
  private final Map<String, String> goProtoDepMapping = Map.ofEntries(
    // annotations package
    entry("//google/api:client_proto", "//google/api:annotations_go_proto"),
    entry("//google/api:field_behavior_proto", "//google/api:annotations_go_proto"),
    entry("//google/api:field_proto", "//google/api:annotations_go_proto"),
    entry("//google/api:http_proto", "//google/api:annotations_go_proto"),
    entry("//google/api:resource_proto", "//google/api:annotations_go_proto"),
    entry("//google/api:routing_proto", "//google/api:annotations_go_proto"),
    // iam package
    entry("//google/iam/v1:iam_policy_proto", "//google/iam/v1:iam_go_proto"),
    entry("//google/iam/v1:policy_proto", "//google/iam/v1:iam_go_proto"),
    entry("//google/iam/v1:options_proto", "//google/iam/v1:iam_go_proto") ,
    // serviceconfig package
    entry("//google/api:auth_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:backend_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:billing_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:context_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:control_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:documentation_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:endpoint_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:log_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:logging_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:monitoring_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:policy_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:quota_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:service_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:source_info_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:system_parameter_proto", "//google/api/serviceconfig_go_proto"),
    entry("//google/api:usage_proto", "//google/api/serviceconfig_go_proto"),
    // single proto remappings
    entry("//google/api:config_change_proto", "//google/api:configchange_go_proto"),
    entry("//google/api:monitored_resource_proto", "//google/api:monitoredres_go_proto"),
    entry("//google/api:launch_stage_proto", "//google/api:api_go_proto"),
    entry("//google/longrunning:operations_proto", "//google/longrunning:longrunning_go_proto"),
    entry("//google/type:postal_address_proto", "//google/type:postaladdress_go_proto"),
    entry("//google/rpc:error_details_proto", "//google/rpc:errdetails_go_proto")
  );

  BazelBuildFileView(ApiVersionedDir bp, String transport, String numericEnums) {
    if (bp.getProtoPackage() == null) {
      return;
    }
    tokens.put("name", bp.getName());
    tokens.put("assembly_name", bp.getAssemblyName());

    tokens.put("proto_srcs", joinSetWithIndentation(bp.getProtos()));
    tokens.put("version", bp.getVersion());
    tokens.put("package", bp.getProtoPackage());
    tokens.put("migration_mode", '"' + bp.getPhpMigrationMode() + '"');

    // For regeneration of Java rules, we are particularly interested in what the saved transport value was,
    // if there was one, in order to correctly generate, or not, the rest/grpc specific targets and labels.
    String javaTransport = bp.getJavaTransportOverride();
    if (javaTransport == null) {
      javaTransport = transport;
    }
    String packPrefix = bp.getProtoPackage().replace(".", "/") + '/';
    Set<String> extraProtosNodeJS = new TreeSet<>();
    Set<String> actualImports = new TreeSet<>();
    for (String imp : bp.getImports()) {
      if (imp.startsWith(packPrefix) && imp.indexOf('/', packPrefix.length()) == -1) {
        // Ignore imports from same package, as all protos in same package are put in same
        // proto_library target.
        continue;
      }

      String actualImport = imp.replace(".proto", "_proto");
      if (actualImport.startsWith("google/protobuf/")) {
        actualImport = actualImport.replace("google/protobuf/", "@com_google_protobuf//:");
      } else if (actualImport.equals("google/cloud/common/operation_metadata_proto")) {
        actualImport = "//google/cloud/common:common_proto";
        extraProtosNodeJS.add(actualImport);
      } else {
        actualImport = convertPathToLabel("", actualImport);
      }
      actualImports.add(actualImport);
    }

    Set<String> extraImports = new TreeSet<>();
    extraImports.add(COMMON_RESOURCES_PROTO);
    // Add location_proto dependency for mix-in if individual language rules need it.
    if (bp.hasLocations() && !bp.getProtoPackage().equals("google.cloud.location")) {
      extraImports.add("//google/cloud/location:location_proto");
    }
    // Add iam_policy_proto dependency for mix-in if individual language rules need it.
    if (bp.hasIAMPolicy() && !bp.getProtoPackage().equals("google.iam.v1")) {
      extraImports.add("//google/iam/v1:iam_policy_proto");
    }
    // Add operations_proto dependency for mix-in if individual language rules need it.
    // Typically, the dependency is already there because the protos depend on it. In
    // some cases though, the mixin may declared when the protos do not depend on it.
    if (bp.hasLRO() && !bp.getProtoPackage().equals("google.longrunning")) {
      // Using actualImports is important, we want to spoof the dependency as if the
      // proto depended on it, like most do.
      actualImports.add("//google/longrunning:operations_proto");
    }
    tokens.put("extra_imports", joinSetWithIndentation(extraImports));
    tokens.put("proto_deps", joinSetWithIndentation(actualImports));
    tokens.put("extra_protos_nodejs", joinSetWithIndentationNl(extraProtosNodeJS));
    tokens.put("go_proto_importpath", bp.getLangProtoPackages().get("go").split(";")[0]);
    tokens.put("go_proto_deps", joinSetWithIndentation(mapGoProtoDeps(actualImports)));

    // Remove common_resources.proto because it is only needed for the proto_library_with_info
    // target.
    extraImports.remove(COMMON_RESOURCES_PROTO);

    // If there are no proto services, then there is no reason to generate GAPIC library targets. This is a
    // simple proto type directory with no API definitions.
    boolean isGapicLibrary = !bp.getServices().isEmpty();
    if (!isGapicLibrary) {
      tokens.put("type_only_assembly_name", bp.getProtoPackage().replaceAll("\\.", "-"));
      if (!bp.getLangProtoPackages().containsKey("csharp")) {
        throw new RuntimeException("Missing required option csharp_namespace: https://google.aip.dev/191#packaging-annotations");
      }
      tokens.put("csharp_namespace", bp.getLangProtoPackages().get("csharp"));
      return;
    }

    // Default grpc_service_config to None, unless there is one present.
    tokens.put("grpc_service_config", "None");
    if (bp.getServiceConfigJsonPath() != null) {
      // Wrap the label in quotes, because the template doesn't supply them
      // in case that None is supplied, which is a built-in value.
      tokens.put(
          "grpc_service_config",
          "\"" + convertPathToLabel(bp.getProtoPackage(), bp.getServiceConfigJsonPath()) + "\"");
    }

    String serviceYaml = "None";
    if (bp.getServiceYamlPath() != null) {
      // Wrap the label in quotations, because None doesn't need them, so they can't be in the
      // template.
      serviceYaml = "\"" + convertPathToLabel(bp.getProtoPackage(), bp.getServiceYamlPath()) + "\"";
    }
    tokens.put("service_yaml", serviceYaml);

    // We need to continue to supply the gapic_yaml to Java targets when the
    // gapic_yaml is available, because that means it was added for some override.
    String gapicYaml = "None";
    String gapicYamlPath = bp.getGapicYamlPath();
    if (gapicYamlPath != null && !gapicYamlPath.isEmpty()) {
      gapicYaml = "\"" + convertPathToLabel(bp.getProtoPackage(), gapicYamlPath) + "\"";
    }
    tokens.put("gapic_yaml", gapicYaml);

    Set<String> javaTests = new TreeSet<>();
    for (String service : bp.getServices()) {
      // Prioritize the language override in gapic.yaml if it is present.
      // New APIs (circa 2020) should rely on the protobuf options instead.
      String javaPackage =
          bp.getLangGapicPackages().containsKey("java")
              ? bp.getLangGapicPackages().get("java")
              : bp.getLangProtoPackages().get("java");
      if (javaPackage == null) {
        continue;
      }

      String actualService =
          bp.getLangGapicNameOverrides().containsKey("java")
              // The service name is overridden in gapic.yaml.
              ? bp.getLangGapicNameOverrides()
                  .get("java")
                  .getOrDefault(bp.getProtoPackage() + "." + service, service)
              // Default service name as it appears in the proto.
              : service;
      
      // Single transport OR grpc+rest results in test file names omitting the
      // transport that the test applies to.
      if (javaTransport.contains("grpc") || javaTransport.equals("rest")) {
        javaTests.add(javaPackage + "." + actualService + "ClientTest");
      }
      // Double transport (i.e. both grpc and rest) results in rest tests being
      // named as the transport variant.
      if (javaTransport.contains("rest") && javaTransport.contains("grpc")) {
        javaTests.add(javaPackage + "." + actualService + "ClientHttpJsonTest");
      }
    }

    actualImports.addAll(extraImports);

    tokens.put("java_tests", joinSetWithIndentation(javaTests));
    tokens.put("java_gapic_deps", joinSetWithIndentationNl(mapJavaGapicDeps(actualImports)));
    tokens.put(
        "java_gapic_test_deps", joinSetWithIndentation(mapJavaGapicTestDeps(actualImports, javaTransport, bp.getName())));
    tokens.put("java_gapic_assembly_gradle_pkg_deps", joinSetWithIndentation(javaGapicAssemblyDeps(javaTransport, bp.getName())));
    tokens.put("java_loads", joinSetWithCustomIndentation(javaLoadStatements(javaTransport), 4));
    tokens.put("java_transport", '"' + javaTransport + '"');
    
    // Posting an empty string for the java_grpc token is necessary so that the template
    // doesn't render the template variable instead when grpc isn't requested.
    String javaGrpcTarget = "";
    if (javaTransport.contains("grpc")) {
      javaGrpcTarget = javaGrpc(bp.getName());
    }
    tokens.put("java_grpc", javaGrpcTarget);

    actualImports.addAll(extraImports);

    // Construct GAPIC import path & package name based on go_package proto option
    String protoPkg = bp.getProtoPackage();
    boolean isCloud = bp.getCloudScope() || protoPkg.contains("cloud");
    String goImport = bp.getGoImportpathOverride();
    if (goImport == null) {
      goImport = assembleGoImportPath(isCloud, protoPkg, bp.getLangProtoPackages().get("go"));
    }
    tokens.put("go_gapic_importpath", goImport);
    tokens.put("go_gapic_test_importpath", goImport.split(";")[0]);
    tokens.put("go_gapic_deps", joinSetWithIndentationNl(mapGoGapicDeps(actualImports)));

    tokens.put("py_gapic_deps", joinSetWithIndentation(mapPyGapicDeps(actualImports)));

    overriddenStringAttributes.putAll(bp.getOverriddenStringAttributes());
    overriddenNonStringAttributes.putAll(bp.getOverriddenNonStringAttributes());
    overriddenListAttributes.putAll(bp.getOverriddenListAttributes());
    assemblyPkgRulesNames.putAll(bp.getAssemblyPkgRulesNames());

    tokens.put("transport", '"' + transport + '"');

    // Ideally, we'd use a slightly more sophisticated templating system, like Mustache, that would
    // allow us to omit `rest_numeric_enums` when `!transport.contains("rest")`
    tokens.put("rest_numeric_enums", numericEnums);

    // Again, ideally we wouldn't emit extra_opts for C# at all unless we had some...
    Map<String, List<String>> csharpListAttributes = bp.getOverriddenListAttributes().get("csharp");
    if (csharpListAttributes != null) {
      List<String> csharpProtoExtraOpts = csharpListAttributes.get("extra_opts");    
      if (csharpProtoExtraOpts != null) {
        tokens.put("csharp_proto_extra_opts", joinCollectionWithIndentation(csharpProtoExtraOpts));
      }
    }
    if (!tokens.containsKey("csharp_proto_extra_opts")) {
      tokens.put("csharp_proto_extra_opts", "");
    }
  }

  /**
   * Returns the import path for a Go GAPIC library package.
   *
   * @param isCloud is true if cloud is in the goPkg
   * @param protoPkg is the value of `package` in a proto file
   * @param goPkg is the value of `go_package` option in a proto file
   */
  static String assembleGoImportPath(boolean isCloud, String protoPkg, String goPkg) {
    boolean isMigratedProtoLib = goPkg.startsWith("cloud.google.com/go/");
    goPkg = goPkg.replaceFirst("google\\.golang\\.org\\/genproto\\/googleapis\\/", "");
    goPkg = goPkg.replaceFirst("cloud\\.google\\.com\\/go\\/", "");
    goPkg = goPkg.replaceFirst("cloud\\/", "");

    String goImport = "";
    if (isCloud && isMigratedProtoLib) {
      goImport = "cloud.google.com/go/";
      String[] goPkgParts= goPkg.split(";");
      // Trim the pb off the end
      String goPkgName = goPkgParts[1].substring(0, goPkgParts[1].length() - 2);
      // Remove the last path segment, which is the stubs dir
      goPkg = goPkgParts[0].substring(0, goPkgParts[0].lastIndexOf("/"));
      goPkg = String.format("%s;%s", goPkg, goPkgName);
    } else if (isCloud) {
      goImport = "cloud.google.com/go/";
      goPkg = goPkg.replaceFirst("\\/v([a-z0-9]+);", "\\/apiv$1;");
    } else {
      goImport = "google.golang.org/";
      String pkgName = goPkg.split(";")[1];

      // use the proto package path for a non-Cloud Go import path
      // example: google.golang.org/google/ads/googleads/v3/services;services
      goPkg = protoPkg.replaceAll("\\.", "\\/");
      goPkg += ";" + pkgName;
    }

    return goImport + goPkg;
  }

  private String convertPathToLabel(String pkg, String path) {
    if (path == null) {
      return path;
    }
    if (!path.contains("/")) {
      return path;
    }

    String[] pkgTokens = pkg.isEmpty() ? new String[0] : pkg.split("\\.");
    String[] pathTokens = path.split("/");

    // Find pkgTokens suffix & pathTokens prefix intersection
    int index = 0;
    for (; index < pkgTokens.length && index < pathTokens.length; index++) {
      if (!pathTokens[index].equals(pkgTokens[pkgTokens.length - index - 1])) {
        break;
      }
    }

    List<String> tokens = new ArrayList<>();
    for (int i = 0; i < pkgTokens.length - index; i++) {
      tokens.add(pkgTokens[i]);
    }
    for (int i = index; i < pathTokens.length; i++) {
      tokens.add(pathTokens[i]);
    }

    StringBuilder sb = new StringBuilder("/");
    for (String token : tokens) {
      sb.append('/').append(token);
    }
    int lastSlashIndex = sb.lastIndexOf("/");
    sb.replace(lastSlashIndex, lastSlashIndex + 1, ":");

    return sb.toString();
  }

  private String joinSetWithCustomIndentation(Set<String> set, int numSpaces) {
    String indent = "";
    for (int i = 0; i < numSpaces; i++) {
      indent += " ";
    }
    return set.isEmpty() ? "" : indent + '"' + String.join("\",\n"+indent+"\"", set) + "\",";
  }

  private String joinSetWithIndentation(Set<String> set) {
    return joinCollectionWithIndentation(set);
  }

  private String joinCollectionWithIndentation(Collection<String> collection) {
    return collection.isEmpty() ? "" : '"' + String.join("\",\n        \"", collection) + "\",";
  }

  private String joinSetWithIndentationNl(Set<String> set) {
    String rv = joinSetWithIndentation(set);
    return rv.isEmpty() ? rv : "\n        " + rv;
  }

  private String replaceLabelName(String labelPathAndName, String newLabelName) {
    return LABEL_NAME.matcher(labelPathAndName).replaceAll(newLabelName);
  }

  private String javaGrpc(String name) {
    return "java_grpc_library(\n"
      + String.format("    name = \"%s_java_grpc\",\n", name)
      + String.format("    srcs = [\":%s_proto\"],\n", name)
      + String.format("    deps = [\":%s_java_proto\"],\n", name)
      + ")";
  }

  private Set<String> javaGapicAssemblyDeps(String transport, String name) {
    Set<String> deps = new TreeSet<>();
    if (transport.contains("grpc")) {
      deps.add(String.format(":%s_java_grpc", name));
    }
    deps.add(String.format(":%s_java_gapic", name));
    deps.add(String.format(":%s_java_proto", name));
    deps.add(String.format(":%s_proto", name));
    return deps;
  }

  private Set<String> javaLoadStatements(String transport) {
    Set<String> loads = new TreeSet<>();
    if (transport.contains("grpc")) {
      loads.add("java_grpc_library");
    }
    loads.add("@com_google_googleapis_imports//:imports.bzl");
    loads.add("java_gapic_assembly_gradle_pkg");
    loads.add("java_gapic_library");
    loads.add("java_gapic_test");
    loads.add("java_proto_library");
    return loads;
  }

  private Set<String> mapJavaGapicDeps(Set<String> protoImports) {
    Set<String> javaImports = new TreeSet<>();
    for (String protoImport : protoImports) {
      if (protoImport.endsWith(":iam_policy_proto")
          || protoImport.endsWith(":policy_proto")
          || protoImport.endsWith(":options_proto")) {
        javaImports.add(replaceLabelName(protoImport, ":iam_java_proto"));
      } else if (protoImport.startsWith("//google/api:")) {
        javaImports.add(replaceLabelName(protoImport, ":api_java_proto"));
      } else if (protoImport.endsWith(":location_proto")) {
        javaImports.add("//google/cloud/location:location_java_proto");
      } else if (protoImport.endsWith(":common_proto")) {
        javaImports.add(replaceLabelName(protoImport, ":common_java_proto"));
      }
    }
    return javaImports;
  }

  private Set<String> mapJavaGapicTestDeps(Set<String> protoImports, String transport, String name) {
    Set<String> javaImports = new TreeSet<>();
    boolean grpcEnabled = transport.contains("grpc");
    if (grpcEnabled) {
      javaImports.add(String.format(":%s_java_grpc", name));
    }

    for (String protoImport : protoImports) {
      boolean iamDep = protoImport.endsWith(":iam_policy_proto")
        || protoImport.endsWith(":policy_proto")
        || protoImport.endsWith(":options_proto");

      if (iamDep && grpcEnabled) {
        javaImports.add(replaceLabelName(protoImport, ":iam_java_grpc"));
      } else if (protoImport.endsWith(":location_proto") && grpcEnabled) {
        javaImports.add("//google/cloud/location:location_java_grpc");
      }
    }

    return javaImports;
  }

  private Set<String> mapGoProtoDeps(Set<String> protoImports) {
    Set<String> goImports = new TreeSet<>();

    for (String protoImport : protoImports) {
      if (protoImport.startsWith("@com_google_protobuf//")) {
        continue;
      }
      if (goProtoDepMapping.containsKey(protoImport)) {
        goImports.add(goProtoDepMapping.get(protoImport));
      } else {
        goImports.add(protoImport.replaceAll("_proto$", "_go_proto"));
      }
    }
    return goImports;
  }

  private Set<String> mapGoGapicDeps(Set<String> protoImports) {
    Set<String> goImports = new TreeSet<>();

    for (String protoImport : protoImports) {
      if (protoImport.startsWith("@com_google_protobuf//")) {
        if (protoImport.endsWith(":duration_proto")) {
          goImports.add("@io_bazel_rules_go//proto/wkt:duration_go_proto");
        }
        continue;
      }

      if (protoImport.endsWith(":operations_proto")) {
        goImports.add(replaceLabelName(protoImport, ":longrunning_go_proto"));
        goImports.add("@com_google_cloud_go_longrunning//:go_default_library");
        goImports.add("@com_google_cloud_go_longrunning//autogen:go_default_library");
        for (String pi : protoImports) {
          if (pi.startsWith("@com_google_protobuf//")) {
            if (pi.endsWith(":struct_proto")) {
              goImports.add("@io_bazel_rules_go//proto/wkt:struct_go_proto");
            } else if (pi.endsWith(":any_proto")) {
              goImports.add("@io_bazel_rules_go//proto/wkt:any_go_proto");
            }
          }
        }
      } else if (protoImport.endsWith(":iam_policy_proto")
          || protoImport.endsWith(":policy_proto")
          || protoImport.endsWith(":options_proto")) {
        goImports.add(replaceLabelName(protoImport, ":iam_go_proto"));
      } else if (protoImport.endsWith(":service_proto")) {
        goImports.add(replaceLabelName(protoImport, ":serviceconfig_go_proto"));
      } else if (protoImport.endsWith(":httpbody_proto")) {
        goImports.add(replaceLabelName(protoImport, ":httpbody_go_proto"));
      } else if (protoImport.endsWith(":monitored_resource_proto")) {
        goImports.add(replaceLabelName(protoImport, ":monitoredres_go_proto"));
      } else if (protoImport.endsWith(":metric_proto")) {
        goImports.add(replaceLabelName(protoImport, ":metric_go_proto"));
      } else if (protoImport.endsWith(":location_proto")) {
        goImports.add(replaceLabelName(protoImport, ":location_go_proto"));
      } else if (protoImport.endsWith(":common_proto")) {
        goImports.add(replaceLabelName(protoImport, ":common_go_proto"));
      }
    }
    return goImports;
  }

  private Set<String> mapPyGapicDeps(Set<String> protoImports) {
    Set<String> pyImports = new TreeSet<>();
    for (String protoImport : protoImports) {
      if (protoImport.endsWith(":iam_policy_proto")
          || protoImport.endsWith(":policy_proto")
          || protoImport.endsWith(":options_proto")) {
        pyImports.add(replaceLabelName(protoImport, ":iam_policy_py_proto"));
      }
    }
    return pyImports;
  }

  Map<String, String> getTokens() {
    return Collections.unmodifiableMap(this.tokens);
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
}

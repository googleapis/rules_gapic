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

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ApiDir {
  private static Pattern SERVICE_YAML_TYPE =
      Pattern.compile("(?m)^type\\s*:\\s*google.api.Service\\s*$");
  private static Pattern SERVICE_YAML_NAME_VERSION =
      Pattern.compile("_(?<version>[a-zA-Z]+\\d+[\\w]*)\\.yaml");
  private static String CLOUD_AUTH_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

  private final Map<String, String> serviceYamlPaths = new TreeMap<>();
  private final Map<String, Boolean> cloudScopes = new TreeMap<>();

  Map<String, String> getServiceYamlPaths() {
    return serviceYamlPaths;
  }

  Map<String, Boolean> getCloudScopes() {
    return cloudScopes;
  }

  void parseYamlFile(String fileName, String fileBody) {
    // It is a service yaml
    Matcher m = SERVICE_YAML_TYPE.matcher(fileBody);
    if (m.find()) {
      Matcher subM = SERVICE_YAML_NAME_VERSION.matcher(fileName);
      String verKey = subM.find() ? subM.group("version") : "";
      serviceYamlPaths.put(verKey, fileName);

      if (fileBody.contains(CLOUD_AUTH_SCOPE)) {
        cloudScopes.put(verKey, true);
      }
    }
  }
}

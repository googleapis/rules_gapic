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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class ArgsParser {
  private final Map<String, String> parsedArgs = new HashMap<>();

  ArgsParser(String[] args) {
    for (String arg : args) {
      String[] argNameVal = arg.split("=");
      if (argNameVal.length == 1) {
        parsedArgs.put(argNameVal[0], "true");
        continue;
      }
      if (argNameVal.length != 2) {
        System.out.println("WARNING: Ignoring unrecognized argument: " + arg);
        continue;
      }
      parsedArgs.put(argNameVal[0], argNameVal[1].trim());
    }

    List<String> required = Collections.singletonList("--src");
    if (!parsedArgs.keySet().containsAll(required)) {
      String msg =
          "ERROR: Not all of the required arguments are specified. "
              + "The required arguments are: "
              + required;
      System.out.println(msg);
      ArgsParser.printUsage();
      throw new IllegalArgumentException(msg);
    }
  }

  static void printUsage() {
    String helpMessage =
        "Usage (when running from googleapis folder):\n"
            + "  bazel run //:build_gen -- --src=google/cloud/language # or any other API folder\n"
            + "\n"
            + "Command line options:\n"
            + "  --src=path: location of googleapis directory\n"
            + "  --dest=path: destination folder, defaults to the value of --src\n"
            + "  --overwrite: do not preserve any of the manually changed values in the generated BUILD.bazel files\n";
    System.out.println(helpMessage);
  }

  ApisVisitor createApisVisitor(ApisVisitor.FileWriter fileWriter, String relativePathPrefix)
      throws IOException {
    if (parsedArgs.get("--help") != null) {
      ArgsParser.printUsage();
      throw new IllegalArgumentException();
    }

    String buildozerPath = parsedArgs.get("--buildozer");
    String gapicApiTemplPath = parsedArgs.get("--gapic_api_templ");
    String rootApiTemplPath = parsedArgs.get("--root_api_templ");
    String rawApiTempl = parsedArgs.get("--raw_api_templ");
    String overwrite = parsedArgs.get("--overwrite");
    if (overwrite == null) {
      overwrite = "false";
    }

    Path srcPath = Paths.get(parsedArgs.get("--src")).normalize();
    Path destPath = srcPath;
    String destArg = parsedArgs.get("--dest");
    if (destArg != null) {
      destPath = Paths.get(destArg).normalize();
    }

    if (relativePathPrefix != null) {
      if (!srcPath.isAbsolute()) {
        srcPath = Paths.get(relativePathPrefix, srcPath.toString());
      }
      if (!destPath.isAbsolute()) {
        destPath = Paths.get(relativePathPrefix, destPath.toString());
      }
    }

    if (buildozerPath == null && !overwrite.equals("true")) {
      System.err.println("This tool requires Buildozer tool to parse BUILD.bazel files.");
      System.err.println("Please use --buildozer=/path/to/buildozer to point to Buildozer,");
      System.err.println("or use --overwrite if you want to rewrite all BUILD.bazel files.");
      throw new IllegalArgumentException();
    }
    Buildozer.setBinaryPath(buildozerPath);

    return new ApisVisitor(
        srcPath,
        destPath,
        gapicApiTemplPath == null
            ? readResource("BUILD.bazel.gapic_api.mustache")
            : ApisVisitor.readFile(gapicApiTemplPath),
        rootApiTemplPath == null
            ? readResource("BUILD.bazel.root_api.mustache")
            : ApisVisitor.readFile(rootApiTemplPath),
        rawApiTempl == null
            ? readResource("BUILD.bazel.raw_api.mustache")
            : ApisVisitor.readFile(rawApiTempl),
        overwrite.equals("true"),
        fileWriter);
  }

  private String readResource(String resourcename) {
    return new Scanner(getClass().getResourceAsStream(resourcename), "UTF-8")
        .useDelimiter("\\A")
        .next();
  }
}

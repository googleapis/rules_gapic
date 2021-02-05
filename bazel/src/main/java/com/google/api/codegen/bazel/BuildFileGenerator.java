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
import java.nio.file.Files;

// To run from bazel and overwrite existing BUILD.bazel files:
//    bazel run //bazel:build_file_generator -- \
//        --src=bazel/src/test/data/googleapis
//
// To compile and copy resources manually from the repository root directory (no bazel needed):
//    javac -d . bazel/src/main/java/com/google/api/codegen/bazel/*.java
//    cp bazel/src/main/java/com/google/api/codegen/bazel/*.mustache
// Then to run manually:
//    java -cp . com.google.api.codegen.bazel.BuildFileGenerator \
//        --src=bazel/src/test/data/googleapis
public class BuildFileGenerator {
  public static void main(String[] args) throws IOException {
    BuildFileGenerator bfg = new BuildFileGenerator();
    ApisVisitor visitor =
        new ArgsParser(args).createApisVisitor(null, System.getenv("BUILD_WORKSPACE_DIRECTORY"));
    bfg.generateBuildFiles(visitor);
  }

  void generateBuildFiles(ApisVisitor visitor) throws IOException {
    System.out.println("\n\n========== READING INPUT DIRECTORY ==========");
    Files.walkFileTree(visitor.getSrcDir(), visitor);
    visitor.setWriterMode(true);
    System.out.println("\n\n========== WRITING GENERATED FILES ==========");
    Files.walkFileTree(visitor.getSrcDir(), visitor);

    System.out.println("\nBUILD.bazel file generation completed successfully\n");
  }
}

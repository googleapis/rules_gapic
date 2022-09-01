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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Buildozer {
  private static Buildozer instance = null;
  private static File buildozerBinary = null;
  private final List<String> batch = new ArrayList<String>();

  private Buildozer() {
    if (buildozerBinary == null) {
      throw new RuntimeException("Buildozer binary path is not set.");
    }
  }

  // General purpose execute method. Runs buildozer with the given command line
  // arguments
  public List<String> execute(List<String> args, List<String> stdin) throws IOException {
    ArrayList<String> cmdList = new ArrayList<String>(args);
    cmdList.add(0, buildozerBinary.toString());
    ProcessBuilder processBuilder = new ProcessBuilder(cmdList);
    Process process = processBuilder.start();
    try (Writer processStdin =
        new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
      if (stdin != null) {
        for (String line : stdin) {
          processStdin.write(line + "\n");
        }
      }
    }

    List<String> result = new ArrayList<String>();
    try (BufferedReader processStdout =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line = null;
      while ((line = processStdout.readLine()) != null) {
        result.add(line);
      }
    }
    return result;
  }

  // Execute buildozer command for the given target
  public List<String> execute(Path bazelBuildFile, String command, String target)
      throws IOException {
    List<String> args = new ArrayList<String>();
    args.add(command);
    args.add(String.format("%s:%s", bazelBuildFile, target));
    return execute(args, null);
  }

  // Get the value of the given attribute of the given target
  public String getAttribute(Path bazelBuildFile, String target, String attribute)
      throws IOException {
    List<String> executeResult;
    try {
      executeResult = execute(bazelBuildFile, String.format("print %s", attribute), target);
      String value = executeResult.get(0);
      if (value.equals("(missing)")) {
        return null;
      }
      // if value has spaces, `buildozer print` will return it in quotes. Remove the quotes
      if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
        value = value.substring(1, value.length() - 1);
      }
      return value;
    } catch (IndexOutOfBoundsException ignored) {
      return null;
    }
  }

  // Set the value to the given attribute of the given target. The value is assumed to be a string
  // and is quoted.
  //
  // The changes will be applied when the whole batch is committed with .commit().
  public void batchSetStringAttribute(Path bazelBuildFile, String target, String attribute, String value)
      throws IOException {
    batch.add(
        String.format(
            "set %s \"%s\"|%s:%s",
            attribute, value.replace(" ", "\\ "), bazelBuildFile.toString(), target));
  }

  // Set the value to the given attribute of the given target. The value is assumed to be a
  // non-string (and contain no spaces) and will not be quoted.
  //
  // The changes will be applied when the whole batch is committed with .commit().
  public void batchSetNonStringAttribute(Path bazelBuildFile, String target, String attribute, String value)
      throws IOException {
    batch.add(
        String.format(
            "set %s %s|%s:%s",
            attribute, value, bazelBuildFile.toString(), target));
  }

  // Remove the given attribute of the given target. Apply changes immediately.
  public void batchRemoveAttribute(Path bazelBuildFile, String target, String attribute)
      throws IOException {
    batch.add(String.format("remove %s|%s:%s", attribute, bazelBuildFile.toString(), target));
  }

  // Add the value to the given list attribute of the given target. The value is assumed to be a string
  // and is quoted.
  //
  // The changes will be applied when the whole batch is committed with .commit().
  public void batchAddStringAttribute(Path bazelBuildFile, String target, String attribute, String value)
      throws IOException {
    batch.add(
        String.format(
            "add %s \"%s\"|%s:%s",
            attribute, value.replace(" ", "\\ "), bazelBuildFile.toString(), target));
  }

  // Make all changes that are waiting in the batch.
  public void commit() throws IOException {
    if (batch.size() == 0) {
      return;
    }
    List<String> args = new ArrayList<>();
    args.add("-f");
    args.add("-");
    execute(args, batch);
    batch.clear();
  }

  // Get a singleton Buildozer instance
  public static synchronized Buildozer getInstance() throws IOException {
    if (instance == null) {
      instance = new Buildozer();
    }

    return instance;
  }

  public static synchronized void setBinaryPath(String path) {
    buildozerBinary = new File(path);
  }
}

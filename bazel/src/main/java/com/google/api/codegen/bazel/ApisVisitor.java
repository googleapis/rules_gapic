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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.TreeMap;

class ApisVisitor extends SimpleFileVisitor<Path> {
  @FunctionalInterface
  interface FileWriter {
    void write(Path dest, String fileBody) throws IOException;
  }

  private Map<String, ApiVersionedDir> bazelApiVerPackages = new TreeMap<>();
  private Map<String, ApiDir> bazelApiPackages = new TreeMap<>();
  private final BazelBuildFileTemplate gapicApiTempl;
  private final BazelBuildFileTemplate rootApiTempl;
  private final BazelBuildFileTemplate rawApiTempl;
  private final Path srcDir;
  private final Path destDir;
  private final boolean overwrite;
  private final String transport;
  private final boolean forceTransport;
  private final String numericEnums;
  private boolean writerMode;
  private final FileWriter fileWriter;

  ApisVisitor(
      Path srcDir,
      Path destDir,
      String gapicApiTempl,
      String rootApiTempl,
      String rawApiTempl,
      boolean overwrite,
      String transport,
      Boolean forceTransport,
      String numericEnums,
      FileWriter fileWriter) {
    this.gapicApiTempl = new BazelBuildFileTemplate(gapicApiTempl);
    this.rootApiTempl = new BazelBuildFileTemplate(rootApiTempl);
    this.rawApiTempl = new BazelBuildFileTemplate(rawApiTempl);
    this.srcDir = srcDir.normalize();
    this.destDir = destDir.normalize();
    this.overwrite = overwrite;
    // Indicates a transport was supplied via command line and it should be respected.
    // If false, any existing transport value pulled by buildozer will take precedent.
    this.forceTransport = forceTransport;
    this.transport = transport;
    this.numericEnums = numericEnums;
    this.writerMode = false;
    this.fileWriter =
        (fileWriter != null)
            ? fileWriter
            : (dest, fileBody) -> Files.write(dest, fileBody.getBytes(StandardCharsets.UTF_8));
  }

  public Path getSrcDir() {
    return srcDir;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
    if (writerMode) {
      return FileVisitResult.CONTINUE;
    }
    System.out.println("Scan Directory: " + dir.toString());

    String dirStr = dir.toString();

    ApiVersionedDir bp = new ApiVersionedDir(this.forceTransport);
    bazelApiVerPackages.put(dirStr, bp);
    ApiDir bap = new ApiDir();
    bazelApiPackages.put(dirStr, bap);

    int parentDirIndex = dirStr.lastIndexOf(File.separator);
    String parentDirStr = dirStr.substring(0, parentDirIndex);
    bp.setParent(bazelApiPackages.get(parentDirStr));

    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (writerMode) {
      return FileVisitResult.CONTINUE;
    }

    System.out.println("    Read File: " + file.toString());

    String packageLocation = file.getParent().toString();
    ApiVersionedDir bp = bazelApiVerPackages.get(packageLocation);
    ApiDir bap = bazelApiPackages.get(packageLocation);

    if (bp == null || bap == null) {
      return FileVisitResult.CONTINUE;
    }

    String fileName = file.getFileName().toString();
    if (fileName.endsWith(".yaml")) {
      if (!fileName.endsWith(".legacy.yaml")) {
        String fileBody = readFile(file);
        bp.parseYamlFile(fileName, fileBody);
        bap.parseYamlFile(fileName, fileBody);
      }
    } else if (fileName.endsWith(".proto")) {
      bp.parseProtoFile(fileName, readFile(file));
    } else if (fileName.endsWith(".bazel")) {
      // if --overwrite is given, we don't care what's in the existing BUILD.bazel file.
      if (!overwrite) {
        bp.parseBazelBuildFile(file);
      }
    } else if (fileName.endsWith(".json")) {
      bp.parseJsonFile(fileName, readFile(file));
    }

    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
    if (!writerMode) {
      return FileVisitResult.CONTINUE;
    }

    String dirStr = dir.toString();
    ApiVersionedDir bp = bazelApiVerPackages.get(dirStr);
    BazelBuildFileTemplate template = null;
    boolean preserveExisting = false;
    String tmplType = "";
    if (bp.getProtoPackage() != null) {
      boolean isGapicLibrary = !bp.getServices().isEmpty();;
      if (isGapicLibrary) {
        bp.injectFieldsFromTopLevel();
        template = this.gapicApiTempl;
        tmplType = "GAPIC_VERSIONED";
      } else if (!bp.getLangProtoPackages().isEmpty()) {
        template = this.rawApiTempl;
        tmplType = "RAW";
      }
    } else if (bp.getServiceYamlPath() != null) {
      template = this.rootApiTempl;
      tmplType = "API_ROOT";
      preserveExisting = !overwrite;
    }

    if (template == null) {
      return FileVisitResult.CONTINUE;
    }

    String rootDirStr = srcDir.toString();
    String outDirPath = destDir.toString() + dirStr.substring(rootDirStr.length());
    File outDir = new File(outDirPath);
    Path outFilePath = Paths.get(outDir.toString(), "BUILD.bazel");

    if (!outDir.exists()) {
      if (!outDir.mkdirs()) {
        System.out.println("WARNING: Could not create directory: " + outDir);
        return FileVisitResult.CONTINUE;
      }
    }

    // Currently we avoid overwriting existing root api build files. This is to
    // preserve such files that may contain manually-added rules for generating
    // Ruby wrappers, which we cannot generate yet. In the future, we should
    // expand this tool to generate those rules.
    if (preserveExisting && outFilePath.toFile().exists()) {
      return FileVisitResult.CONTINUE;
    }

    System.out.println(
        "Write File [" + tmplType + "]: " + outDir.toString() + File.separator + "BUILD.bazel");
    BazelBuildFileView bpv = new BazelBuildFileView(bp, transport, numericEnums);
    fileWriter.write(outFilePath, template.expand(bpv));

    return FileVisitResult.CONTINUE;
  }

  public static String readFile(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path).normalize()), StandardCharsets.UTF_8);
  }

  private static String readFile(Path path) throws IOException {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  public void setWriterMode(boolean writerMode) {
    this.writerMode = writerMode;
  }
}

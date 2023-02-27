// Copyright 2022 Google LLC
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
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

public class BazelBuildFileViewTest {
  @Test
  public void testAssembleGoImportPath() throws IOException {
    // Non cloud library
    String actual = BazelBuildFileView.assembleGoImportPath(false, "google.foo.v1", "google.golang.org/genproto/googleapis/foo/v1;foo");
    Assert.assertEquals("google.golang.org/google/foo/v1;foo", actual);

    // Old style cloud stubs import path
    actual = BazelBuildFileView.assembleGoImportPath(true, "google.cloud.foo.v1", "google.golang.org/genproto/googleapis/cloud/foo/v1;foo");
    Assert.assertEquals("cloud.google.com/go/foo/apiv1;foo", actual);

    // New style cloud stubs import path
    actual = BazelBuildFileView.assembleGoImportPath(true, "google.cloud.foo.v1", "cloud.google.com/go/foo/apiv1/foopb;foopb");
    Assert.assertEquals("cloud.google.com/go/foo/apiv1;foo", actual);
  }

  @Test
  public void testConvertPathToLabel() {
    // Proto imports aren't actually evaluated with a package while converting them.
    String emptyPkg = "";
    List<List<String>> tests = Arrays.asList(
      Arrays.asList(emptyPkg, "google/type/interval_proto", "//google/type:interval_proto"),
      // X-API dependency.
      Arrays.asList(emptyPkg, "google/cloud/foo/v1/bar_proto", "//google/cloud/foo/v1:foo_proto")
    );
    for (List<String> testCase : tests) {
      String pkg = testCase.get(0);
      String path = testCase.get(1);
      String want = testCase.get(2);

      String got = BazelBuildFileView.convertPathToLabel(pkg, path);
      Assert.assertEquals(want, got);
    }
  }

  @Test 
  public void testTypeOnlyAssemblyName() {
    List<List<String>> tests = Arrays.asList(
      Arrays.asList("type", "type"),  
      Arrays.asList("google.type", "google-type"),
      Arrays.asList("google.geo.type", "geo-type")
    );
    for (List<String> testCase : tests) {
      String pkg = testCase.get(0);
      String want = testCase.get(1);

      String got = BazelBuildFileView.typeOnlyAssemblyName(pkg);
      Assert.assertEquals(want, got);
    }
  }
}

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
import org.junit.Assert;
import org.junit.Test;

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
}

# Copyright 2021 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:utils.bzl", "maybe")

def rules_gapic_repositories():
    ## Dependencies for buildozer
    maybe(
        http_archive,
        name = "com_google_protobuf",
        urls = ["https://github.com/protocolbuffers/protobuf/archive/v3.14.0.zip"],
        strip_prefix = "protobuf-3.14.0",
    )

    maybe(
        http_archive,
        name = "io_bazel_rules_go",
        urls = [
            "https://github.com/bazelbuild/rules_go/archive/v0.23.3.zip",
        ],
        strip_prefix = "rules_go-0.23.3",
    )

    maybe(
        http_archive,
        name = "com_github_bazelbuild_buildtools",
        strip_prefix = "buildtools-3.2.1",
        urls = [
            "https://github.com/bazelbuild/buildtools/archive/3.2.1.tar.gz",
        ],
    )

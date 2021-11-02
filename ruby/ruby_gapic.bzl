# Copyright 2019 Google LLC
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

load("//:gapic.bzl", "gapic_srcjar", "proto_custom_library")

def ruby_proto_library(name, deps, **kwargs):
    srcjar_target_name = name
    proto_custom_library(
        name = srcjar_target_name,
        deps = deps,
        output_type = "ruby",
        output_suffix = ".srcjar",
        extra_args = [
            "--include_source_info",
        ],
        **kwargs
    )

def ruby_grpc_library(name, srcs, deps, **kwargs):
    srcjar_target_name = name

    # `deps` is not used now but may be used if ruby_grpc_library ever tries to "compile" its output
    proto_custom_library(
        name = srcjar_target_name,
        deps = srcs,
        plugin = Label("@com_github_grpc_grpc//src/compiler:grpc_ruby_plugin"),
        output_type = "grpc",
        output_suffix = ".srcjar",
        extra_args = [
            "--include_source_info",
        ],
        **kwargs
    )

## GAPIC Generator [Bazel](https://www.bazel.build/) Integration

**This is not an officially supported Google project.**

This repository contains Bazel rules for generating Google Cloud client
libraries using the corresponding generators:

- [C#](https://github.com/googleapis/gapic-generator-csharp)
- [Go](https://github.com/googleapis/gapic-generator-go)
- [Java](https://github.com/googleapis/gapic-generator-java)
- [PHP](https://github.com/googleapis/gapic-generator-php)
- [Python](https://github.com/googleapis/gapic-generator-python)
- [Ruby](https://github.com/googleapis/gapic-generator-ruby)
- [TypeScript](https://github.com/googleapis/gapic-generator-typescript)

The input for each generator is usually a set of
[proto files](https://developers.google.com/protocol-buffers) defining the given
API. The definitions for Google Cloud APIs can be found in the
[googleapis](https://github.com/googleapis/googleapis) repository.

Example of generating a client library for Language API:

```sh
git clone https://github.com/googleapis/googleapis
cd googleapis
bazel build //google/cloud/language/v1:google-cloud-language-v1-java
bazel build //google/cloud/language/v1:language_go_gapic
bazel build //google/cloud/language/v1:language-v1-py
bazel build //google/cloud/language/v1:google-cloud-language-v1-php
bazel build //google/cloud/language/v1:language-v1-nodejs
bazel build //google/cloud/language/v1:google-cloud-language-v1-ruby
bazel build //google/cloud/language/v1:google-cloud-language-v1-csharp
```
(note that the Bazel target names are different for different languages for
legacy reasons)

### Requirements

- Bazel version `3.0.0+`.
- Linux (may work on other platforms, but this haven't been tested).
- `gcc`, `make`, `autoconf`, `unzip` tools

### Usage

The rules will call `gapic-generator` and do all the necessary pre- and post- generation steps to generate a fully-functional client library for a specified service API in a specified language. The rules are expected to be used from within a Bazel workspace containing service interface definitions in one of the following formats:
- **`proto/grpc`** format, defined by `<service>.proto`, `<service>.yaml` and `<service>_gapic.yaml` files; see [googleapis](https://github.com/googleapis/googleapis) repository for an example.
- **`discovery/httpjson`** format, defined by `<service>.json` and `<service>_gapic.yaml`; see [discovery-artifact-manager](https://github.com/googleapis/discovery-artifact-manager) repository for an example.

### Rules and Macros

#### Cross-language
1. **`gapic_srcjar`** - cross-language (main) **rule** which calls gapic-generator, supplies necessary input files to it (like `gapic_<service>.yaml`, `<service>.yaml` or `discovery_doc.json` in case of discogapic generation). Notice that `artman_<service>.yaml` (googleapis), `dependencies.yaml` and `api_defaults.yaml` (gapic-generator) and `package_yaml2` (generated by artman from artman and service yamls) become obsolete (not required at any stage). The chosen name tries to follow Bazel best practices, in which a rule name must be a noun, which represents the output of the rule.
2. **`proto_custom_library`** - cross-language **rule**, which allows: 1) calling `protoc` with custom plugins; 2) specifying the output files format and file extension (essential for consuming (down the chain) rules); 3) accepting `proto_library` targets as inputs (to adhere to Bazel protobuf best practices and stay consistent with everything else). Unfortunately Bazel does not have native support for the features above and a similar rule in protobuf repo [proto_gen](https://github.com/protocolbuffers/protobuf/blob/master/protobuf.bzl#L173) satisfies the 1st requirement/feature but not the other two.  Notice that this rule does not replace `proto_library`, it is additive to it (it uses `proto_library`'s output as input, while `proto_library` has raw proto files as input).
3. **`proto_library_with_info`** - cross-language **macro**, which wraps `proto_custom_library` and provides "fat" proto descriptor. This is not supported by native `proto_library` rule but is required by gapic-generator to include documentation from protos and to overcome the limitation of "only one" desc file as an input in gapic-generator (dependencies' proto descriptors must be supplied somehow, here they are embedded in one big descriptor which then goes to gapic-gen as input).
4. **`moved_proto_library`** - A deprecated cross-language **macro** currently used only to generate Python clients. It is used to achieve backward compatibility with previous generator tools, which used to move all protos under google/cloud domain before running the generation.


#### Java
1. **`java_gapic_srcjar`** - Java **rule**, which does all the Java-specific post-processing of the Java gapic-generator output (produced by `gapic_srcjar` rule). This includes running formatter and splitting output into main and test packages (a design decision/assumption made, but it is essential (though not strictly required) for proper building of the artifacts within Bazel).
2. **`java_gapic_library`** - Java **macro**, which subsequently calls `gapic_srcjar` to generate sources, then `java_gapic_srcjar` to post-process them, then  native `java_library` (twice, one for main and one for tests) to build Java binary lib from the generated output within Bazel. This is **the rule**, which intends to put gapic-generator on same level of tools integration as protoc (`java_proto_library`) and gRPC (`java_grpc_library`).
3. **`java_discogapic_library`** - Java **macro**, which is very similar to `java_gapic_library` and does same, but for discogapic libraries.
4. **`java_gapic_test`** - Java **macro**, which creates `java_test` and groups them in a `test_suite`, accepting `java_gapic_library` test artifact and the generated test classes full names (with package).
5. **`java_gapic_assembly_gradle_pkg`** - Java **macro** which accepts the previously built `java_proto_library`, `java_grpc_library`, `java_gapic_library` and `proto_library` artifacts as arguments and packages them into an idiomatic (for PHP) package which is ready for opensourcing and is independent from Bazel.

#### Python
1. **`py_gapic_srcjar`** - Python **macro**, which first calls `gapic_srcjar` to generate the source code. Then, it calls an internal rule, which does all the Python-specific postprocessing of the code. This postprocessing includes formatting the code and splitting the code into main and test `.srcjar` archives (a zip format).
2. **`py_gapic_library`** - Python **macro**, which first calls `py_gapic_srcjar` to generate and post process the gapic library. Then, it calls the native `py_library` rule to build the generated code. It declares directories. Finally, it unpacks the sources for the main and test into those directories, so that they can be consumed by `py_library` and `py_test` (which do not accept `.srcjar` files as an input).
3. **`py_gapic_assembly_pkg`** - Python **macro** which accepts the previously built `py_gapic_library` (including the `-test.srcjar` and `-smoke-test.srcjar`), `py_proto_library` and `py_grpc_library` artifacts and packages them into an idiomatic (for Python) package which is ready for opensourcing and is independent from Bazel.

#### PHP
1. **`php_proto_library`** - PHP **macro**, which generates php protobuf stubs by calling protobuf compiler with `--php_out` parameter.

2. **`php_grpc_library`** - PHP **macro**, which generates php gRPC stubs by calling protobuf compiler with the php gRPC plugin.

3. **`php_gapic_srcjar`** - PHP **macro**, which first calls `gapic_srcjar` to generate the source code, then calls an internal rule which does all the php-specific postprocessing of the code (calling using `php-cs-fixer` and `phpcbf` tools, splitting the code into main and test and smoke test `.srcjar` files (zip format)).

4. **`php_gapic_library`** - PHP **macro**, which calls `php_gapic_srcjar` to generate and postprocess gapic library.

5. **`php_gapic_assembly_pkg`** - PHP **macro** which accepts the previously built `php_proto_library`, `php_grpc_library` and `php_gapic_library` artifacts as arguments and packages them into an idiomatic (for PHP) package which is ready for opensourcing and is independent from Bazel.

#### Ruby
1. **`ruby_proto_library`** - Ruby **macro**, which generates Ruby protobuf stubs by calling protobuf compiler with `--ruby_out` parameter.

2. **`ruby_grpc_library`** - Ruby **macro**, which generates Ruby gRPC stubs by calling protobuf compiler with the Ruby gRPC plugin.

3. **`ruby_gapic_srcjar`** - Ruby **macro**, which first calls `gapic_srcjar` to generate the source code, then calls an internal rule which does all the ruby-specific postprocessing of the code (currenly just splitting the code into main and test and smoke test `.srcjar` files (zip format)).

4. **`ruby_gapic_library`** - Ruby **macro**, which calls `ruby_gapic_srcjar` to generate and postprocess gapic library.

5. **`ruby_gapic_assembly_pkg`** - Ruby **macro** which accepts the previously built `ruby_proto_library`, `ruby_grpc_library` and `ruby_gapic_library` artifacts as arguments and packages them into an idiomatic (for Ruby) package which is ready for opensourcing and is independent from Bazel.

#### C#
1. **`csharp_proto_library`** - C# **macro**, which generates C# protobuf stubs by calling protobuf compiler with `--csharp_out` parameter.

2. **`csharp_grpc_library`** - C# **macro**, which generates C# gRPC stubs by calling protobuf compiler with the C# gRPC plugin.

3. **`csharp_gapic_srcjar`** - C# **macro**, which first calls `gapic_srcjar` to generate the source code, then calls an internal rule which does all the C#-specific postprocessing of the code (currently just splitting the code into main, test, smoke test and package `.srcjar` files (zip format)).

4. **`csharp_gapic_library`** - C# **macro**, which calls `csharp_gapic_srcjar` to generate and postprocess gapic library.

5. **`csharp_gapic_assembly_pkg`** - C# **macro** which accepts the previously built `csharp_proto_library`, `csharp_grpc_library` and `csharp_gapic_library` artifacts as arguments and packages them into an idiomatic (for C#) package which is ready for opensourcing and is independent from Bazel.

### Generated Artifacts Dependencies Resolution
#### Java
1. **`java/java_gapic_repositories.bzl`** - this file essentially replaces `artman_<service>.yaml`, `dependencies.yaml` and `api_defaults.yaml` by using `bazel` itself for dependencies resolution. Previously the dependencies were handled in a form of yaml config values, when they are not validated to: 1) be correct/exist; 2) match generated code; 3) be sufficient/redundant. To deal with dependencies versions mismatch, the `repo_mapping` feature of Bazel is supposed to be used (enabled by `--experimental_enable_repo_mapping` command line argument).

#### Python
1. **`python/py_gapic_repositories.bzl`** - this file declares the Python-specific dependencies of the generated output. It is intended to be included in the `WORKSPACE` file of the consuming workspace (for example, in `googleapis`).

#### PHP
1. **`php/php_gapic_repositories.bzl`** - this file declares the PHP-specific dependencies of the generated output and is supposed to be included in the WORKSPACE file of the consuming workspace (for example in `googleapis`). This file also declares the `php` repository rule, which downloads and builds from sources the PHP interpreter (by using `gcc`, `make` and `autoconf` tools, so they are expected to be installed on the system).

#### Ruby
There are not any specific to Ruby dependencies at this moment (they may be added in the future).

#### C#
There are not any specific to C# dependencies at this moment (they may be added in the future).

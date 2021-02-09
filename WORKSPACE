workspace(name = "rules_gapic")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_python",
    strip_prefix = "rules_python-0.1.0",
    url = "https://github.com/bazelbuild/rules_python/archive/0.1.0.tar.gz",
)

load("@rules_gapic//:repositories.bzl", "com_google_api_codegen_bazel_repositories")
com_google_api_codegen_bazel_repositories()

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")
protobuf_deps()

### Dependencies for buildozer
load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
go_rules_dependencies()
go_register_toolchains()

load("@rules_gapic//synth:synth_repositories.bzl", "synth")

synth()
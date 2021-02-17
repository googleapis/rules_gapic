workspace(name = "rules_gapic")

load("//:repositories.bzl", "rules_gapic_repositories")
rules_gapic_repositories()

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")
protobuf_deps()

### Dependencies for buildozer
load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
go_rules_dependencies()
go_register_toolchains()

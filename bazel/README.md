## `BUILD.bazel` file generator for Google APIs

We have moved the BUILD.bazel file generator to the internal code repository.
It is going to be refactored and replaced with an internal tool.

We suspect there is not much usage of this code externally, but if we are wrong
and you need the `BUILD.bazel` file generator in your workflows, you have
options!

1. Keep using the latest release
[v0.29.4](https://github.com/googleapis/rules_gapic/releases/tag/v0.29.4).
It works and will be available. You can fork this repository to keep developing
the tool if you wish.

2. File an [issue](https://github.com/googleapis/rules_gapic/issues/new) and let
us know that you use this code. We can setup a mirror to have the code published
in this case. Please be advised that the code will stop receiving updates soon
anyway, as we are moving to another solution for generating `BUILD.bazel` files
for client libraries.

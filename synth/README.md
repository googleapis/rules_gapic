## GAPIC Postprocessor [Bazel](https://www.bazel.build/) Integration

This directory contains Bazel rules for running `synth.py` postprocessing 
scripts.

Most of the Python scripts in this directory are modified versions of the 
scripts from [Synthtool](https://github.com/googleapis/synthtool).

### Usage
To run `synth.py` for Language API put the following in the `BUILD.bazel` file 
in the `//google/cloud/language` package:  

```bzl
load("@rules_gapic//synth:gapic_postprocessor.bzl", "java_synth_pkg")

java_synth_pkg(
    name = "java_language",
    synth_script = "synth.py",
    srcs = [
        "//google/cloud/language/v1:google-cloud-language-v1-java",
        "//google/cloud/language/v1beta2:google-cloud-language-v1beta2-java",
    ],
)
```

The `synth.py` script ran by the rule above is a regular `synth.py` script,
some parts of which (like templates generation) will result in no-op if they 
are specific to code publishing and are not about postprocessing/correcting the
code generated by the GAPIC generators.
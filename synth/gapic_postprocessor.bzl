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

def _postprocessed_gapic_pkg_impl(ctx):
    postprocessor = ctx.executable.postprocessor
    formatter = ctx.executable.formatter
    output_pkg = ctx.outputs.pkg
    synth_script = ctx.attr.synth_script.files.to_list()[0]

    output_dir_name = ctx.label.name
    tmp_dir_name = "%s_tmp" % output_dir_name;
    output_dir_path = "%s/%s" % (output_pkg.dirname, output_dir_name)
    tmp_dir_path = "%s/%s" % (output_pkg.dirname, tmp_dir_name)

    gapic_assemblies = []
    for gapic_assembly in ctx.attr.srcs:
        gapic_assemblies.extend(gapic_assembly.files.to_list())

    script = """
    set -e
    echo {formatter}
    mkdir -p {output_dir_path}
    mkdir -p {tmp_dir_path}
    for gapic_assembly in {gapic_assemblies}; do
        tar -xzpf $gapic_assembly -C {tmp_dir_path}
    done
    {postprocessor} {output_dir_path} {synth_script} {tmp_dir_path} {formatter}
    pushd {output_dir_path}/..
    tar -zchpf {output_pkg_basename} {output_dir_name}
    popd
    """.format(
        gapic_assemblies = " ".join(["'%s'" % d.path for d in gapic_assemblies]),
        output_dir_path = output_dir_path,
        output_pkg_basename = output_pkg.basename,
        postprocessor = postprocessor.path,
        output_dir_name = output_dir_name,
        tmp_dir_path = tmp_dir_path,
        tmp_dir_name = tmp_dir_name,
        synth_script = synth_script.path,
        formatter = formatter.path
    )

    ctx.actions.run_shell(
        inputs = gapic_assemblies + [synth_script],
        tools = [postprocessor, formatter],
        command = script,
        outputs = [output_pkg],
    )

postprocessed_gapic_pkg = rule(
    attrs = {
        "srcs": attr.label_list(
            allow_files = True,
            mandatory = False
        ),
        "synth_script": attr.label(
            mandatory = True,
            allow_single_file = True
        ),
        "postprocessor": attr.label(
            default = Label("//synth:gapic_postprocessor"),
            executable = True,
            cfg = "host",
        ),
        "formatter": attr.label(
            executable = True,
            cfg = "host",
        )

    },
    outputs = {"pkg": "%{name}.tar.gz"},
    implementation = _postprocessed_gapic_pkg_impl,
)

def java_synth_pkg(name, synth_script, srcs, visibility = None, **kwargs):
    postprocessed_gapic_pkg(
        name = name,
        synth_script = synth_script,
        srcs = srcs,
        formatter = Label("//synth:google_java_format_binary"),
        visibility = visibility,
    )

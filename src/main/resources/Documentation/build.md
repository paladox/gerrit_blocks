# Build

This plugin can be built with Bazel, and one build modes are supported:

* In Gerrit tree

## Build in Gerrit tree

Clone or link this plugin to the plugins directory of Gerrit's
source tree. From Gerrit source tree issue the command:

```
  bazel build plugins/@PLUGIN@
```

The output is created in

```
  bazel-genfiles/plugins/@PLUGIN@/@PLUGIN@.jar
```

This project can be imported into the Eclipse IDE:
Add the plugin name to the `CUSTOM_PLUGINS` in `tools/bzl/plugins.bzl`, and
execute:

```
  ./tools/eclipse/project.py
```


[Back to @PLUGIN@ documentation index][index]

[index]: index.html
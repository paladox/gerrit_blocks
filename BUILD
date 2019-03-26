load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "WikimediaBlocks",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: WikimediaBlocks",
        "Gerrit-Module: org.wikimedia.gerrit.plugins.wikimediablocks.Module",
        "Gerrit-HttpModule: org.wikimedia.gerrit.plugins.wikimediablocks.HttpModule",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

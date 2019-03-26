Configuration
=============

WikimediaBlocks
-----------

The @PLUGIN@ plugin is configured in the `$site_path/etc/@PLUGIN@.config` file:

File '@PLUGIN@.config'
----------------------

```
[WikimediaBlocks]
  message = You are blocked!
```

If the configuration is modified, the plugin must be reloaded for the changes to
be effective.


```readonly.message```
:   Message to be shown to clients when attempting to perform an opeation that
    is blocked due to the user being blocked. When not specified,
    the default is "You have been blocked!".

Block
-----------

The defined blocks are stored in the `blocks.config` file in the
`refs/meta/config` branch of the `All-Projects` root project. Blocks
are defined per user group.

Example:

```
  [block "buildserver"]
    blocks = true

  [block "Registered Users"]
    blocks = null
```

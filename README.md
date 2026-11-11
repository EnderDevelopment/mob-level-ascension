# Mob Level Ascension Minecraft Plugin

Mob Level Ascension is a Minecraft plugin for Paper servers that gives hostile and peaceful mobs persistent levels from 1 to 100, scaling their combat stats so higher-level mobs become stronger and lower-level mobs remain weaker.

## Install

1. Use a Paper Minecraft server running version 1.21.11 or a compatible 1.21 server.
2. Install Java 21 or newer.
3. Download the compiled plugin JAR.
4. Place the JAR file in your server's `plugins` folder.
5. Start or restart the server.
6. Confirm that the plugin is enabled in the server console.

Newly spawned mobs receive a level automatically. Existing mobs are initialized when the plugin starts or when their chunks load.

## Use

Look directly at a mob and run:

```text
/moblevel
```

The command displays the targeted mob's current level. The target can be up to 100 blocks away.

To set a mob's level manually, look at it and run:

```text
/moblevel <level>
```

Valid levels range from 1 to 100. Changing a mob's level requires the `moblevels.admin` permission, which is granted to server operators by default.

The command also has the alias:

```text
/ml
```

Mob levels are stored persistently, so they remain assigned when the mob or its chunk is loaded again. Mobs receive scaled maximum health, attack damage, armor, movement speed, and knockback resistance. Their display names include the mob type and level, with colors indicating lower, medium, or high level ranges.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/104b0f13-ddd5-46f6-acb1-dced6bd2f4e8?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=104b0f13-ddd5-46f6-acb1-dced6bd2f4e8

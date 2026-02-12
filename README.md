# FractalVoice

**FractalVoice** is a Minecraft mod that integrates with a Discord bot to manage voice channels dynamically based on in-game proximity. Players are automatically moved to the correct Discord voice channel when they see each other in-game — all without exposing coordinates.

---

## How It Works

* Detects players in your visual range and sends proximity data to a connected Discord bot.
* No exact coordinates are shared — only relative presence in view.
* Generates a unique linking code to associate your Minecraft account with your Discord account.
* The bot moves linked players to the same voice channel when they are near each other and returns them to the lobby channel when they separate.

---

## Installation

1. Download the mod from the [Releases](#) page **or** build it from source.
2. Place the `.jar` file in your `mods/` folder.
3. Join the official Discord server: [https://discord.gg/pnyBFBpWh6](https://discord.gg/pnyBFBpWh6).
4. Start Minecraft and log in.
5. Use the command in-game:

   ```
   /linkproximity
   ```
6. Copy the generated code and use it on Discord with the `/link` command.
7. Join the lobby voice channel. The bot will handle moving you automatically when you encounter other linked players.

---

## Building from Source

The mod uses **Gradle** with Fabric Loom. You can build it for specific Minecraft versions or all supported versions.

### Build a Specific Version

```bash
./gradlew clean build -PmcVersion=<version>
```

* Example:

```bash
./gradlew clean build -PmcVersion=1.21.4
```

* The resulting JAR will be located in `build/libs/` with the filename:

  ```
  fractal-voice-1.0.0-<mcVersion>.jar
  ```

### Build All Supported Versions

```bash
./gradlew buildAllVersions
```

* Each built version will be stored in `versions/<mcVersion>/` for easy organization.

### Custom Overrides

You can override default mappings and Fabric API dependencies:

```bash
./gradlew clean build \
  -PmcVersion=1.21.4 \
  -Pmappings="net.fabricmc:yarn:1.21.4+build.8:v2" \
  -PfabricApiDep="net.fabricmc.fabric-api:fabric-api:0.119.4+1.21.4"
```

---

## Requirements

* **Minecraft** versions supported - 1.21 to 1.21.8.
* **Fabric Loader** 0.15.6 or higher.
* **Fabric API** corresponding to the chosen Minecraft version.
* Discord account linked to the server for proximity voice channels.

---

## License

MIT License

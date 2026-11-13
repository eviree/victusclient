# Victus Client mod - multi-version (Fabric 1.21.1 -> 1.21.11)

One source tree, one jar per Minecraft range:

| jar                          | Minecraft          | code variant |
|------------------------------|--------------------|--------------|
| victusclient-1.21.1.jar      | 1.21 - 1.21.1      | legacy       |
| victusclient-1.21.4.jar      | 1.21.2 - 1.21.4    | legacy       |
| victusclient-1.21.5.jar      | 1.21.5             | legacy       |
| victusclient-1.21.8.jar      | 1.21.6 - 1.21.8    | mid          |
| victusclient-1.21.11.jar     | 1.21.9 - 1.21.11   | modern       |

Right Shift in game -> Victus Mod Menu.

## Build locally (one target)
    gradle wrapper --gradle-version 9.2.1      # once, JDK 21
    ./gradlew -Ptarget=1.21.4 build

## Build + release everything (GitHub Actions)
1. Push this folder to a GitHub repo.
2. `git tag v1.2.0 && git push origin v1.2.0`
3. The workflow builds every file in `versions/`, attaches all jars and `versions.json` to the release.

## Launcher
`launcher/victusModInstaller.js` reads
`https://github.com/<repo>/releases/latest/download/versions.json`, picks the jar for the profile's
Minecraft version, verifies SHA-256 and installs it (plus Fabric API) into the profile's mods folder.

## Add / split a version
Copy a file in `versions/`, change the 6 values (use https://fabricmc.net/develop for versions).
If a jar crashes on one Minecraft version of its range, give that version its own file.

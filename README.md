# WCNM Alpha — 阴森冥界

Fabric mod for Minecraft 26.3.  Adds the Chinese-inspired **Underworld** dimension, procedural
WFC + Perlin terrain, 50 randomly placed underworld buildings, glowing signs and a dark
Chinese-style resource pack.

## What is implemented

- `wcnm:underworld` dimension with dark red fog, no daylight cycle, an obsidian ceiling and
  black obsidian terrain.
- Custom `UnderworldChunkGenerator` (`wcnm:underworld` codec).  It never uses Minecraft's
  built-in noise router / density functions.
  - `WfcLayouts`: a deterministic Wave Function Collapse solver for roads, plazas, groves,
    blood pools and reserved building plots.
  - `PerlinNoise`: a private Perlin sampler for natural ground, spires, chasms, floating rock
    and the ceiling.
  - Coupling: WFC cells flatten the Perlin terrain into 32x32 terraces at y=72, so roads and
    buildings always connect while the surrounding landscape stays deliberately wrong.
- `BuildingCatalog`: 50 fixed underworld buildings with Chinese names (鬼门关, 奈何桥,
  阎罗殿, 轮回井, 十八层狱, 酆都城 …).  Buildings are selected by a seeded hash per WFC cell.
- Every building has at least one glowing sign on a soul-lamp pedestal reading
  `第 N 号冥筑 / 名称 / 阴司 · 酆都`.
- Blocks/items: 冥石, 冥石砖, 雕纹冥石, 幽冥灯, 彼岸花, 引魂灯.
- `引魂灯` teleports between the underworld and the overworld; `/wcnm` commands.
- Chinese underworld textures bundled directly as mod assets (always applied); a standalone
  texture-pack zip is also produced by the asset generator.

## Commands

| Command | Description |
| --- | --- |
| `/wcnm tp` | Enter the underworld |
| `/wcnm return` | Return to the overworld |
| `/wcnm find [radius]` | Find the nearest building, prints number/name/sign position |
| `/wcnm kit` | Give the lantern and underworld building materials |
| `/wcnm tud visibleDistance <8-512>` | Underworld fog distance (per player, server-synced) |
| `/wcnm tud visibleDistance reset` | Restore the dimension's default fog distance |
| `/wcnm tud effects off` | Remove the pink fog/dark filter (useful with night vision) |
| `/wcnm tud effects on` | Re-enable the underworld atmosphere |

## Build

```bat
gradlew.bat build
```

Output jar: `build/libs/WCNM-Alpha-1.0.0.jar`.

## Run

```bat
gradlew.bat runClient
gradlew.bat runServer
```

In a development server, a `WcnmSelfTest` runs on `SERVER_STARTED` and logs the generated
spawn plaza, the 鬼门关 sign, a real building and its glowing sign.

## Modpack

The `modpack/` directory contains the DeepUnderWorld pack builder:

```bat
python modpack\tools\generate_assets.py
gradlew.bat build
python modpack\tools\build_pack.py
python modpack\tools\deploy_to_pcl.py
```

Or simply run `build_and_deploy.bat`.

Output: `modpack/dist/DeepUnderWorld-2.1.2.mrpack`, the offline variants and the standalone
texture pack.  The deployment step copies everything to
`D:\PCL\.minecraft\DeepUnderWorld-2.1.2\`, installs the launchable version
`D:\PCL\.minecraft\versions\Deep Under World 2.1.2\`, and puts the `.mrpack` in the PCL2
root directory.

## Environment

- Java 25 (Gradle JVM pinned in `gradle.properties` via `org.gradle.java.home`)
- Gradle 9.7.1 (wrapper)
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+26.3
- Fabric Loom 1.18.4 (`net.fabricmc.fabric-loom`)

## License

CC0-1.0. Based on the Fabric example mod template.

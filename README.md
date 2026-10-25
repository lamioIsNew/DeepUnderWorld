# WCNM Alpha — 阴森冥界 / The Dark UnderWorld

Fabric mod for **Minecraft 26.3**. Adds a Chinese-inspired underworld dimension built on the
vanilla overworld noise generator, 50 procedural buildings with glowing signs, a varied
soul-soil surface, and two custom ambient spirits.

## Features

- `wcnm:underworld` dimension with a custom noise-setting profile, dark atmosphere and
  configurable fog.
- Terrain uses the vanilla `NoiseBasedChunkGenerator` and overworld density functions. There is
  no WFC or custom Perlin terrain.
- A single underworld biome; no oceans or vanilla river biomes. Sparse lakes are placed on a
  5000×5000 grid. One meandering river exists inside `|x|,|z| <= 1000` and fades out smoothly
  before the boundary.
- **Surface palette**: soul block, ashen soil, soul gravel, bone soil, blood moss and soul
  stone, plus soul grass and bianhua decorations.
- **Ambient entities**: `wcnm:wandering_soul` (游魂) and `wcnm:ghost_wisp` (鬼火). A server-side
  spawner keeps a small population around players in the underworld.
- **Hostile entities**: `wcnm:ghost_soldier` (阴兵) and `wcnm:underworld_hound` (冥犬) spawn
  further away and hunt players.
- **Brighter atmosphere**: ambient light is raised to 0.42, fog is lighter and reaches 192
  blocks, so the surface is readable without night vision.
- **Minimap**: JourneyMap 26.3-6.0.10 is bundled for the client.
- **50 buildings** selected deterministically on a 96-block grid. Every building has a glowing
  sign reading `第 N 号冥筑 / 名称 / 阴司 · 酆都`.
- **引魂灯**：右键空气或潜行时右键方块可在冥界/主世界之间往返；普通右键方块会放置为一盏发光的引魂灯。返回阳间前会强制生成目标区块并寻找安全落点，避免落入虚空。

## Commands

All lookup commands require gamemaster permission.

| Command | Description |
| --- | --- |
| `/wcnm tp` | Enter the underworld at the spawn gate |
| `/wcnm return` | Return to the overworld |
| `/wcnm find radius <radius>` | List every building in radius, nearest first; click a green coordinate to teleport, `[复制]` copies it |
| `/wcnm find structure <name>` | Locate the nearest matching structure; English IDs such as `ghost-gate`, `naihe-bridge`, `fengdu-city` |
| `/wcnm find biome <name>` | Locate an underworld biome, e.g. `underworld` or `wcnm:underworld` |
| `/wcnm find [radius]` | Legacy shorthand for the nearest building |
| `/wcnm ls` | Current chunk: biome, sampled surface height, river/lake flags and buildings |
| `/wcnm kit` | Give the lantern and building materials |
| `/wcnm tud visibleDistance <8-512>` | Per-player fog distance |
| `/wcnm tud visibleDistance reset` | Restore default fog distance |
| `/wcnm tud effects off/on` | Toggle the fog/dark filter |

Structure lookup also accepts English names (`Ghost Gate`), underscores (`ghost_gate`),
namespaced IDs (`wcnm:fengdu_city`) or catalog numbers (`12`).

## Blocks

| Block | Chinese | Notes |
| --- | --- | --- |
| `soul_block` | 灵魂块 | Base soul-soil surface |
| `ashen_soil` | 冥土 | Grey-black ashen patches |
| `soul_gravel` | 魂砂砾 | Purple-grey gravel patches |
| `bone_soil` | 骨壤 | Pale bone-flecked soil |
| `blood_moss` | 血苔 | Dark red moss |
| `soul_stone` | 魂石 | Glowing veins, small outcrops |
| `soul_grass` | 阴草 | Cross-model ground plant |
| `bianhua` | 彼岸花 | Existing glowing flower |
| `yin_stone` / bricks / chiseled | 冥石系列 | Building materials |
| `soul_lamp` | 幽冥灯 | Light 15 |

All textures live under the `wcnm` namespace; no vanilla textures are modified.

## Entities

- `wcnm:wandering_soul` — 游魂, passive floating spirit.
- `wcnm:ghost_wisp` — 鬼火, smaller and faster glowing wisp.
- `wcnm:ghost_soldier` — 阴兵, hostile melee soldier.
- `wcnm:underworld_hound` — 冥犬, fast hostile hound.

They can be summoned with `/summon wcnm:wandering_soul`, `/summon wcnm:ghost_wisp`,
`/summon wcnm:ghost_soldier` and `/summon wcnm:underworld_hound`.

## Minimap

The pack includes **JourneyMap 26.3-6.0.10** for Fabric 26.3. Open the full-screen map with
`J`; minimap and waypoint keys can be changed under `Options → Controls → Key Binds`.

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

In a development server, `WcnmSelfTest` runs on `SERVER_STARTED` and logs the spawn gate,
surface palette, a generated building and entity spawn checks.

## Modpack

```bat
python modpack\tools\generate_assets.py
gradlew.bat build
python modpack\tools\build_pack.py
python modpack\tools\deploy_to_pcl.py
```

Or run `build_and_deploy.bat`.

Current pack version: **2.4.1**. Deployment copies mods/config/shaderpacks to
`D:\PCL\.minecraft\DeepUnderWorld-2.4.1\`, installs the launchable PCL2 version
`D:\PCL\.minecraft\versions\Deep Under World 2.4.1\`, and places the `.mrpack` in the PCL2 root.

## Environment

- Java 25
- Gradle 9.7.1 (wrapper)
- Fabric Loader 0.19.5+ (deployed instance uses 0.20.1)
- Fabric API 0.160.5+26.3
- Fabric Loom 1.18.4

## License

CC0-1.0. Based on the Fabric example mod template.

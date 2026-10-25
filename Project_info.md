# 王程的模组

## 项目简介

- 内部代号：WCNM-Alpha
- 模组名称：阴森冥界（The Dark UnderWorld）
- 主要设定：国风冥界维度。地形使用原版主世界噪声生成器，不使用 WFC/自研 Perlin。
- 冥界内随机生成 50 种冥筑，每座建筑至少一块发光告示牌，标明编号与名称。

## 已实现

- 独立维度 `wcnm:underworld`，可配置暗色雾效、无海洋、单一冥界群系。
- 原版 `NoiseBasedChunkGenerator` + 自定义噪声/地表规则，灵魂块地表。
- 湖泊按 5000×5000 格稀疏生成；河流仅限 `|x|,|z| <= 1000`，边界平滑截断。
- 地表混合灵魂块、冥土、魂砂砾、骨壤、血苔、魂石，并生成阴草与彼岸花。
- 50 种冥筑、发光告示牌、引魂灯。
- 实体：游魂 `wcnm:wandering_soul`、鬼火 `wcnm:ghost_wisp`、阴兵 `wcnm:ghost_soldier`、冥犬 `wcnm:underworld_hound`。
- 亮度：环境光 0.42，雾距 192，地表无需夜视即可看清。
- 指令：`/wcnm tp|return|find|ls|kit|tud`，支持按半径/结构/群系定位。
- 小地图：内置 JourneyMap 26.3-6.0.10（全屏地图/路径点/死亡点）。
- 自定义材质全部位于 `wcnm` 命名空间，不修改原版贴图。

## 环境

- Minecraft 26.3 / Fabric Loader 0.19.5+（PCL2 部署实例 0.20.1）
- Java 25

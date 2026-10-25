# Deep UnderWorld

基于 **Fabric 0.20.1 + Minecraft 26.3** 的《阴森冥界》国风整合包。
`2.4.0` 已包含 WCNM 本体：一个独立的 **冥界维度**、原版主世界噪声地形、
**50 种随机冥筑**、发光告示牌、多样地表与生物、自绘材质和 Xaero's 小地图；同时保留完整的前置库和性能优化。

## 下载哪个文件？

| 文件 | 用途 |
| --- | --- |
| `DeepUnderWorld-2.4.0.mrpack` | **推荐**。标准 Modrinth 整合包，导入时联网下载模组，本体与材质已内置。 |
| `DeepUnderWorld-2.4.0-offline.mrpack` | 离线版，所有模组已打包在内，不需要联网下载。 |
| `DeepUnderWorld-2.4.0-offline.zip` | CurseForge 格式离线版，兼容性兜底方案（PCL2 也能直接导入）。 |

> `.mcpack` 是基岩版（Bedrock）格式，Java 版启动器无法使用，所以这里提供 `.mrpack` / `.zip`。

## PCL2 导入步骤

1. 安装 **Java 25**（Minecraft 26.3 需要）。PCL2 的「设置 → Java」里确认已选中 Java 25。
2. 打开 PCL2，进入「版本列表」。
3. 点击「导入整合包」，选择上面的 `.mrpack` 或 `.zip`；也可以直接拖进 PCL2 窗口。
4. 启动版本后，使用 **引魂灯** 或 `/wcnm tp` 进入冥界。

## 冥界维度

- 出生点是一座 **鬼门关**，位于冥界 (0, 81, 0) 的平坦广场。
- **引魂灯**：右键空气或潜行时右键方块可在冥界/主世界之间往返；普通右键方块会把它放置成一盏灯。
- 返回阳间时会先强制生成目标区块并寻找安全地表，不会再把玩家放进虚空。
- 管理员命令：
  - `/wcnm tp` 进入冥界，`/wcnm return` 返回主世界
  - `/wcnm find radius <半径>` 列出半径内全部冥筑，点击绿色坐标直接传送，`[复制]` 复制坐标
  - `/wcnm find structure <英文结构名>` 定位指定冥筑，例如 `ghost-gate`、`naihe-bridge`、`fengdu-city`
  - `/wcnm find biome <英文群系名>` 定位冥界群系，例如 `underworld` 或 `wcnm:underworld`
  - `/wcnm ls` 查看当前区块的群系、地表高度、河流/湖泊和区块内冥筑
  - `/wcnm kit` 发放引魂灯与冥界建材
  - `/wcnm tud visibleDistance <8-512>` 设置冥界雾距（只影响自己）
  - `/wcnm tud visibleDistance reset` 恢复维度默认雾距
  - `/wcnm tud effects off` 关闭冥界雾/暗色滤镜（夜视下不再一片粉红）
  - `/wcnm tud effects on` 重新开启冥界特效
- 冥界没有原版天气与日月，只有暗红雾（可关闭）和灵魂块地面。
- 使用 `/wcnm tp` 进入；出生点 y=81，没有岩顶，可以自由飞行。

### 地形与建筑

- 地形使用 **原版主世界 `NoiseBasedChunkGenerator` 噪声路由**，不再使用 WFC 或自研 Perlin。
- 只使用 `wcnm:underworld` 一个生物群系，不生成海洋或原版河流生物群系。
- 海平面设为最低，因此不会生成海洋；湖非常稀疏（约每 5000×5000 格一个候选）。
- 河流只允许在 |x|,|z| ≤ 1000 内生成，边界前 90 格平滑淡出，超出范围完全截断。
- 共 **50 种冥筑**，由 96 格网格确定性选点，建筑脚下是灵魂块/冥石台基。
- 每座建筑正面至少有一块 **发光告示牌**，写着「第 N 号冥筑 / 建筑名称 / 阴司 · 酆都」。
- 地面表层为自绘的 **灵魂块**，并混合 **冥土、魂砂砾、骨壤、血苔、魂石** 多种地表纹理。
- 地表会生成 **阴草** 与彼岸花，周围有 **游魂**、**鬼火** 两种漂浮实体；鬼火更小更亮。
- 冥界还会生成敌对的 **阴兵** 与 **冥犬**，以及亮度更高、雾更远的环境光。
- 新增六种地表方块与四种自绘实体，均只使用 `wcnm` 命名空间贴图。

## 国风材质

- 只新增 `wcnm` 命名空间的自定义贴图；**不修改任何原版贴图**。
- 包含灵魂块、冥石、冥石砖、雕纹冥石、幽冥灯、彼岸花与引魂灯。

## 光影

- Iris 已安装，`Complementary Reimagined r5.9.3` 已放入 `shaderpacks/`。
- 进入游戏后：`选项 → 视频设置 → 光影包`，选择并启用；低配可以不启用。

## 小地图

- 已内置 **Xaero's Minimap 26.6.1**（Modrinth 小地图类下载量最高），纯客户端模组。
- 默认按键 `Y` 打开小地图设置；`U` 创建路径点（以实际按键设置为准）。

## 已包含内容

- **本体**：WCNM / 阴森冥界（冥界维度、原版主世界噪声地形、50 冥筑、发光告示牌、自定义材质）。
- **前置库**：Fabric API、Cloth Config、YACL、Architectury、GeckoLib、Puzzles Lib、
  Forge Config API Port、Cardinal Components、MidnightLib、Fabric Language Kotlin、
  Balm、Resourceful Lib、SuperMartijn642 Lib、TerraBlender、Placeholder API、MezzConfig。
- **主题前置**：CodxLib、Deimos、GlitchCore、Lithostitched、Trinkets Updated。
- **优化**：Sodium、Sodium Extra、Lithium、FerriteCore、ImmediatelyFast、Entity Culling、
  BadOptimizations、Clumps、More Culling、Dynamic FPS、Krypton、Debugify、Iris。
- **体验**：Mod Menu、Jade、JEI、AppleSkin、Mouse Tweaks、BetterF3、
  LambDynamicLights、Sound Physics Remastered、Entity Model/Texture Features。
- **光影**：Complementary Reimagined。

完整清单（含版本与文件名）见 [MODLIST.md](MODLIST.md)。

## 重新构建

```bat
python modpack\tools\generate_assets.py
gradlew build
python modpack\tools\build_pack.py
python modpack\tools\deploy_to_pcl.py
```

`build_pack.py` 会按固定的 Modrinth 版本 ID 下载、校验 SHA-512，并把 `build/libs/`
中的 WCNM 本体一起放进 `overrides/mods/`，最后在 `modpack/dist/` 生成三个整合包。
`deploy_to_pcl.py` 会把最新版本复制到 `D:\PCL\.minecraft\DeepUnderWorld-<版本>\`
（含全部 mods、config、shaderpacks），并把 `.mrpack` 放到 PCL2 根目录。
也可以直接运行根目录的 `build_and_deploy.bat` 完成全部步骤。

## 后续添加内容模组

1. 在 `modpack/tools/build_pack.py` 的 `MODS` 列表里追加该模组的 Modrinth 版本 ID。
2. 运行 `python modpack/tools/check_deps.py` 检查前置是否齐全。
3. 重新运行构建脚本。

临时测试也可以直接把 `.jar` 拖进 PCL2 实例的 `mods/` 目录。添加前请确认模组支持
**Minecraft 26.3 + Fabric**。

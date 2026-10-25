# -*- coding: utf-8 -*-
"""
DeepUnderWorld modpack builder.

Pulls pinned Modrinth versions for Minecraft 26.3 / Fabric, verifies SHA-512,
then produces:

  dist/DeepUnderWorld-1.0.0.mrpack          (online, Modrinth format)
  dist/DeepUnderWorld-1.0.0-offline.mrpack  (all files bundled in overrides)
  dist/DeepUnderWorld-1.0.0-offline.zip     (CurseForge format, PCL2 friendly)

Run:  python modpack/tools/build_pack.py
"""

from __future__ import annotations

import concurrent.futures
import hashlib
import json
import os
import sys
import time
import urllib.request
import zipfile
from dataclasses import dataclass
from pathlib import Path

MC_VERSION = "26.3"
FABRIC_LOADER = "0.20.1"
PACK_NAME = "Deep UnderWorld"
PACK_VERSION = "2.4.1"
PACK_AUTHOR = "WCNM"
PACK_SUMMARY = (
    "基于 Fabric 26.3 的《阴森冥界》整合包："
    "原版噪声冥界地形、50 种随机冥筑、多样地表与生物、国内风材质、小地图、前置库与性能优化。"
)

ROOT = Path(__file__).resolve().parents[1]
CACHE = ROOT / "build" / "downloads"
META = ROOT / "build" / "metadata"
DIST = ROOT / "dist"
API = "https://api.modrinth.com/v2"
UA = "DeepUnderWorld-pack-builder/1.0 (WCNM)"


@dataclass(frozen=True)
class Mod:
    version_id: str
    category: str
    side: str = "both"          # both | client
    target: str = "mods"        # mods | shaderpacks
    note: str = ""


# Pinned Modrinth version IDs. Bump deliberately, then re-run the script.
MODS: list[Mod] = [
    # ---- 前置 / 库 ----
    Mod("EQKgpeKu", "前置", note="Fabric API 0.165.0+26.3"),
    Mod("fg2uyxOW", "前置", note="Cloth Config API"),
    Mod("s9SjoFu1", "前置", note="Yet Another Config Lib"),
    Mod("1pMaB6So", "前置", note="Architectury API"),
    Mod("kSxHvs99", "前置", note="GeckoLib"),
    Mod("68hLXNWE", "前置", note="Puzzles Lib"),
    Mod("JpKvrr9J", "前置", note="Forge Config API Port"),
    Mod("TPEXA0ew", "前置", note="Cardinal Components API"),
    Mod("G9RaNHAC", "前置", note="MidnightLib"),
    Mod("SKW1BTL3", "前置", note="Fabric Language Kotlin"),
    Mod("MYj31XNG", "前置", note="Balm"),
    Mod("P6Jq77km", "前置", note="Resourceful Lib"),
    Mod("alB9Au3G", "前置", note="SuperMartijn642's Core Lib"),
    Mod("eYcALOr1", "前置", note="SuperMartijn642's Config Lib"),
    Mod("xirJYe1V", "前置", note="TerraBlender"),
    Mod("lXytLqWj", "前置", note="Text Placeholder API"),
    Mod("GKiA7PV4", "前置", note="MezzConfig (JEI 前置)"),
    # 主题内容模组的常用前置（本包暂未装入内容模组，先备好）
    Mod("W7Cpzahs", "主题前置", note="CodxLib (Alex's Caves)"),
    Mod("DqsrgsEE", "主题前置", note="Deimos (Server-Side Horror)"),
    Mod("kfFeDnrp", "主题前置", note="GlitchCore (Biomes O' Plenty)"),
    Mod("pZgI6b2t", "主题前置", note="Lithostitched (Terralith/Tectonic)"),
    Mod("lVR1OKgV", "主题前置", note="Trinkets Updated (饰品 API)"),
    # ---- 优化 ----
    Mod("bAZQdGpg", "优化", side="client", note="Sodium 0.9.2"),
    Mod("te2y9qZn", "优化", side="client", note="Sodium Extra"),
    Mod("xS0Q8LSi", "优化", note="Lithium"),
    Mod("d5ddUdiB", "优化", note="FerriteCore"),
    Mod("3MP9UR23", "优化", side="client", note="ImmediatelyFast"),
    Mod("X1g3VhTx", "优化", side="client", note="Entity Culling"),
    Mod("Sp0ctspw", "优化", note="BadOptimizations"),
    Mod("J4I1wxJZ", "优化", note="Clumps"),
    Mod("t7vAlfgO", "优化", side="client", note="More Culling"),
    Mod("Jwq069rR", "优化", side="client", note="Dynamic FPS"),
    Mod("UugdIYJw", "优化", note="Krypton"),
    Mod("FMaS2nZn", "优化", note="Debugify"),
    Mod("vTN4NRGW", "优化", side="client", note="Iris 光影加载器"),
    # ---- 体验 ----
    Mod("kyy7dbrZ", "体验", side="client", note="Mod Menu"),
    Mod("71CTWqdE", "体验", side="client", note="Jade"),
    Mod("aSjVJwgD", "体验", side="client", note="JEI 物品管理器"),
    Mod("PHjDtQay", "体验", side="client", note="AppleSkin"),
    Mod("LeMuMhLv", "体验", side="client", note="Mouse Tweaks"),
    Mod("XB7u4eSf", "体验", side="client", note="BetterF3"),
    Mod("rylKnJ4M", "体验", side="client", note="LambDynamicLights"),
    Mod("gA7nKz5g", "体验", side="client", note="Sound Physics Remastered"),
    Mod("vF6qLXsA", "体验", side="client", note="Entity Model Features"),
    Mod("OOEycXyN", "体验", side="client", note="Entity Texture Features"),
    Mod("2zmj8ela", "体验", side="client", note="JourneyMap 26.3-6.0.10"),
    # ---- 光影包 ----
    Mod("Bqen1mJX", "光影", side="client", target="shaderpacks",
        note="Complementary Reimagined r5.9.3"),
]


# Files produced by this repository rather than downloaded from Modrinth.  They are placed in
# overrides/mods so they work in both the online and offline .mrpack variants.
LOCAL_FILES = [
    ("mods/WCNM-Alpha-1.0.0.jar", ROOT.parent / "build" / "libs" / "WCNM-Alpha-1.0.0.jar"),
]

# A friendlier starting fog distance for the first launch.  Players can change it with
# /wcnm tud visibleDistance <radius> or restore the dimension default with "reset".
CLIENT_OVERRIDES = {
    "config/wcnm-client.properties": b"underworld.visibleDistance=128\nunderworld.effects=false\n",
}


def api_get(path: str) -> dict:
    req = urllib.request.Request(API + path, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def download(url: str, dest: Path, expected_size: int, attempts: int = 6) -> None:
    """Download with HTTP-range resume and retries (the CDN can be slow)."""
    tmp = dest.with_suffix(dest.suffix + ".part")
    for attempt in range(1, attempts + 1):
        have = tmp.stat().st_size if tmp.exists() else 0
        if have > expected_size:
            tmp.unlink()
            have = 0
        if have == expected_size:
            break
        headers = {"User-Agent": UA}
        if have:
            headers["Range"] = f"bytes={have}-"
        req = urllib.request.Request(url, headers=headers)
        try:
            with urllib.request.urlopen(req, timeout=120) as resp:
                mode = "ab" if have and resp.status == 206 else "wb"
                with open(tmp, mode) as fh:
                    while True:
                        chunk = resp.read(1 << 20)
                        if not chunk:
                            break
                        fh.write(chunk)
        except Exception as exc:  # noqa: BLE001
            print(f"    retry {attempt}/{attempts} {dest.name}: {exc}", flush=True)
            time.sleep(min(2 * attempt, 10))
            continue
    if not tmp.exists() or tmp.stat().st_size != expected_size:
        raise RuntimeError(f"incomplete download: {dest.name}")
    tmp.replace(dest)


def download_many(jobs: list[tuple[str, Path, int]], workers: int = 8) -> None:
    with concurrent.futures.ThreadPoolExecutor(max_workers=workers) as pool:
        futures = {
            pool.submit(download, url, dest, size): dest for url, dest, size in jobs
        }
        for fut in concurrent.futures.as_completed(futures):
            dest = futures[fut]
            fut.result()
            print(f"  ok  {dest.name}", flush=True)


def sha512(path: Path) -> str:
    h = hashlib.sha512()
    with open(path, "rb") as fh:
        for chunk in iter(lambda: fh.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def fetch_meta(mod: Mod) -> dict:
    cache = META / f"{mod.version_id}.json"
    if cache.exists():
        return json.loads(cache.read_text(encoding="utf-8"))
    info = api_get(f"/version/{mod.version_id}")
    cache.write_text(json.dumps(info), encoding="utf-8")
    return info


def collect() -> list[dict]:
    """Resolve pinned versions, download and verify files."""
    CACHE.mkdir(parents=True, exist_ok=True)
    META.mkdir(parents=True, exist_ok=True)
    resolved: list[dict] = []
    jobs: list[tuple[str, Path, int]] = []

    print(f"Resolving metadata for {len(MODS)} pinned versions...", flush=True)
    infos: dict[str, dict] = {}
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        futures = {pool.submit(fetch_meta, mod): mod for mod in MODS}
        for fut in concurrent.futures.as_completed(futures):
            mod = futures[fut]
            info = fut.result()
            if MC_VERSION not in info.get("game_versions", []):
                raise RuntimeError(
                    f"{info.get('name')} is not tagged for Minecraft {MC_VERSION}"
                )
            infos[mod.version_id] = info
            print(f"  meta {info['files'][0]['filename']}", flush=True)

    for mod in MODS:
        info = infos[mod.version_id]
        f = info["files"][0]
        dest = CACHE / f["filename"]
        resolved.append(
            {
                "mod": mod,
                "version": info["version_number"],
                "path": f"{mod.target}/{f['filename']}",
                "filename": f["filename"],
                "url": f["url"],
                "size": f["size"],
                "sha1": f["hashes"]["sha1"],
                "sha512": f["hashes"]["sha512"],
                "local": dest,
            }
        )
        if not dest.exists() or sha512(dest) != f["hashes"]["sha512"]:
            jobs.append((f["url"], dest, f["size"]))

    if jobs:
        print(f"Downloading {len(jobs)} file(s) with 8 workers...", flush=True)
        download_many(jobs)
    else:
        print("All files already cached.", flush=True)

    for r in resolved:
        actual = sha512(r["local"])
        if actual != r["sha512"]:
            raise RuntimeError(f"hash mismatch: {r['filename']}")
        print(f"  verified {r['path']}  ({r['size'] / 1024 / 1024:.1f} MiB)", flush=True)
    return resolved


def mrpack_index(resolved: list[dict], offline: bool) -> dict:
    files = []
    if not offline:
        for r in resolved:
            env_server = (
                "unsupported" if r["mod"].side == "client" else "required"
            )
            files.append(
                {
                    "path": r["path"],
                    "hashes": {"sha1": r["sha1"], "sha512": r["sha512"]},
                    "env": {"client": "required", "server": env_server},
                    "downloads": [r["url"]],
                    "fileSize": r["size"],
                }
            )
    return {
        "formatVersion": 1,
        "game": "minecraft",
        "versionId": PACK_VERSION,
        "name": PACK_NAME,
        "summary": PACK_SUMMARY,
        "files": files,
        "dependencies": {
            "minecraft": MC_VERSION,
            "fabric-loader": FABRIC_LOADER,
        },
    }


def cf_manifest() -> dict:
    return {
        "minecraft": {
            "version": MC_VERSION,
            "modLoaders": [
                {"id": f"fabric-{FABRIC_LOADER}", "primary": True}
            ],
        },
        "manifestType": "minecraftModpack",
        "manifestVersion": 1,
        "name": PACK_NAME,
        "version": PACK_VERSION,
        "author": PACK_AUTHOR,
        "files": [],
        "overrides": "overrides",
    }


def write_zip(path: Path, entries: list[tuple[str, bytes | Path]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED, compresslevel=6) as z:
        for arcname, payload in entries:
            if isinstance(payload, Path):
                z.write(payload, arcname)
            else:
                z.writestr(arcname, payload)
    print(f"  -> {path.name}  ({path.stat().st_size / 1024 / 1024:.2f} MiB)")


def build() -> None:
    DIST.mkdir(parents=True, exist_ok=True)
    print("Resolving and downloading pinned files...")
    resolved = collect()
    manifest = [r["filename"] for r in resolved if r["mod"].target == "mods"]
    manifest += [Path(relative).name for relative, path in LOCAL_FILES if path.exists()]
    (ROOT / "build" / "pack_mods.txt").write_text("\n".join(manifest) + "\n", encoding="utf-8")

    index_online = json.dumps(mrpack_index(resolved, False), indent=2, ensure_ascii=False)
    index_offline = json.dumps(mrpack_index(resolved, True), indent=2, ensure_ascii=False)
    manifest = json.dumps(cf_manifest(), indent=2, ensure_ascii=False)

    missing_local = [str(path) for _, path in LOCAL_FILES if not path.exists()]

    if missing_local:
        raise RuntimeError("Missing local files, run 'gradlew build' first: " + ", ".join(missing_local))

    # 1) online .mrpack (local mod jar goes into overrides)
    entries: list[tuple[str, bytes | Path]] = [
        ("modrinth.index.json", index_online),
    ]
    for relative, path in LOCAL_FILES:
        entries.append((f"overrides/{relative}", path))
    for relative, payload in CLIENT_OVERRIDES.items():
        entries.append((f"overrides/{relative}", payload))
    write_zip(
        DIST / f"DeepUnderWorld-{PACK_VERSION}.mrpack", entries
    )

    # 2) offline .mrpack (override files bundled, no downloads)
    entries = [
        ("modrinth.index.json", index_offline),
    ]
    for r in resolved:
        entries.append((f"overrides/{r['path']}", r["local"]))
    for relative, path in LOCAL_FILES:
        entries.append((f"overrides/{relative}", path))
    for relative, payload in CLIENT_OVERRIDES.items():
        entries.append((f"overrides/{relative}", payload))
    entries.append(("overrides/README-DeepUnderWorld.txt", readme_short().encode("utf-8")))
    write_zip(
        DIST / f"DeepUnderWorld-{PACK_VERSION}-offline.mrpack", entries
    )

    # 3) offline CurseForge zip (all jars under overrides/)
    cf_entries: list[tuple[str, bytes | Path]] = [
        ("manifest.json", manifest),
    ]
    for r in resolved:
        cf_entries.append((f"overrides/{r['path']}", r["local"]))
    for relative, path in LOCAL_FILES:
        cf_entries.append((f"overrides/{relative}", path))
    for relative, payload in CLIENT_OVERRIDES.items():
        cf_entries.append((f"overrides/{relative}", payload))
    cf_entries.append(("overrides/README-DeepUnderWorld.txt", readme_short().encode("utf-8")))
    write_zip(
        DIST / f"DeepUnderWorld-{PACK_VERSION}-offline.zip", cf_entries
    )

    write_modlist(resolved)
    write_readme(resolved)
    total = sum(r["size"] for r in resolved)
    print(f"\nDone. {len(resolved)} files, {total / 1024 / 1024:.1f} MiB of content.")


def readme_short() -> str:
    return (
        "Deep UnderWorld - Fabric 26.3 整合包\n"
        "================================================\n\n"
        "需要 Java 25。\n"
        f"Fabric Loader {FABRIC_LOADER} / Minecraft {MC_VERSION}\n\n"
        "在 PCL2 中：版本列表 -> 导入整合包 -> 选择 .mrpack 或 .zip 文件。\n\n"
        "进入冥界：右键使用「引魂灯」，或输入 /wcnm tp（出生点 0, 81, 0）。\n"
        "查找冥筑：/wcnm find radius 300；指定结构：/wcnm find structure ghost-gate。\n"
        "查找群系：/wcnm find biome underworld；查看当前区块：/wcnm ls。\n"
        "返回阳间：/wcnm return。\n"
        "调节雾距：/wcnm tud visibleDistance 128（8-512 格）。\n"
        "关闭雾/粉红滤镜：/wcnm tud effects off。\n\n"
        "光影已放在 shaderpacks/ 目录，进入游戏后在 视频设置 -> 光影 中启用\n"
        "Complementary Reimagined。\n\n"
        "模组清单与更新记录请查看整合包发布页的 README / MODLIST。\n"
    )


def write_modlist(resolved: list[dict]) -> None:
    local_size = sum(path.stat().st_size for _, path in LOCAL_FILES if path.exists())
    lines = [
        "# Deep UnderWorld 模组清单",
        "",
        f"- Minecraft: {MC_VERSION}",
        f"- Fabric Loader: {FABRIC_LOADER}",
        f"- 打包版本: {PACK_VERSION}",
        f"- 文件数: {len(resolved) + len(LOCAL_FILES)}",
        "",
        "| 分类 | 模组 | 版本 | 文件 |",
        "| --- | --- | --- | --- |",
    ]
    for relative, path in LOCAL_FILES:
        lines.append(f"| 本体 | WCNM / 阴森冥界（冥界维度 + 50 冥筑） | 1.0.0 | `{Path(relative).name}` |")
    for r in resolved:
        lines.append(
            f"| {r['mod'].category} | {r['mod'].note} | {r['version']} | `{r['filename']}` |"
        )
    lines.append("")
    total = sum(r["size"] for r in resolved) + local_size
    lines.append(f"总内容体积：{total / 1024 / 1024:.1f} MiB")
    lines.append("")
    out = ROOT / "MODLIST.md"
    out.write_text("\n".join(lines), encoding="utf-8")
    print(f"  -> {out.relative_to(ROOT)}")


def write_readme(resolved: list[dict]) -> None:
    text = f"""# Deep UnderWorld

基于 **Fabric {FABRIC_LOADER} + Minecraft {MC_VERSION}** 的《阴森冥界》国风整合包。
`{PACK_VERSION}` 已包含 WCNM 本体：一个独立的 **冥界维度**、原版主世界噪声地形、
**50 种随机冥筑**、发光告示牌、多样地表与生物、自绘材质和 JourneyMap 小地图；同时保留完整的前置库和性能优化。

## 下载哪个文件？

| 文件 | 用途 |
| --- | --- |
| `DeepUnderWorld-{PACK_VERSION}.mrpack` | **推荐**。标准 Modrinth 整合包，导入时联网下载模组，本体与材质已内置。 |
| `DeepUnderWorld-{PACK_VERSION}-offline.mrpack` | 离线版，所有模组已打包在内，不需要联网下载。 |
| `DeepUnderWorld-{PACK_VERSION}-offline.zip` | CurseForge 格式离线版，兼容性兜底方案（PCL2 也能直接导入）。 |

> `.mcpack` 是基岩版（Bedrock）格式，Java 版启动器无法使用，所以这里提供 `.mrpack` / `.zip`。

## PCL2 导入步骤

1. 安装 **Java 25**（Minecraft {MC_VERSION} 需要）。PCL2 的「设置 → Java」里确认已选中 Java 25。
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

- 已内置 **JourneyMap 26.3-6.0.10**，纯客户端模组，支持全屏地图、路径点和死亡点。
- 默认按键 `J` 打开全屏地图；小地图和路径点按键可在「选项 → 控制 → 按键绑定」里调整。

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
python modpack\\tools\\generate_assets.py
gradlew build
python modpack\\tools\\build_pack.py
python modpack\\tools\\deploy_to_pcl.py
```

`build_pack.py` 会按固定的 Modrinth 版本 ID 下载、校验 SHA-512，并把 `build/libs/`
中的 WCNM 本体一起放进 `overrides/mods/`，最后在 `modpack/dist/` 生成三个整合包。
`deploy_to_pcl.py` 会把最新版本复制到 `D:\\PCL\\.minecraft\\DeepUnderWorld-<版本>\\`
（含全部 mods、config、shaderpacks），并把 `.mrpack` 放到 PCL2 根目录。
也可以直接运行根目录的 `build_and_deploy.bat` 完成全部步骤。

## 后续添加内容模组

1. 在 `modpack/tools/build_pack.py` 的 `MODS` 列表里追加该模组的 Modrinth 版本 ID。
2. 运行 `python modpack/tools/check_deps.py` 检查前置是否齐全。
3. 重新运行构建脚本。

临时测试也可以直接把 `.jar` 拖进 PCL2 实例的 `mods/` 目录。添加前请确认模组支持
**Minecraft {MC_VERSION} + Fabric**。
"""
    (ROOT / "README.md").write_text(text, encoding="utf-8")
    print("  -> README.md")


if __name__ == "__main__":
    try:
        build()
    except Exception as exc:  # noqa: BLE001
        print(f"BUILD FAILED: {exc}", file=sys.stderr)
        raise

# -*- coding: utf-8 -*-
"""Deploy the latest DeepUnderWorld build into the local PCL2 Minecraft directory.

Creates:
  D:\\PCL\\.minecraft\\DeepUnderWorld-<version>\\
      mods\\                 (all mods, including the compiled WCNM jar)
      config\\               (client fog preset)
      shaderpacks\\          (Complementary Reimagined)
      DeepUnderWorld-<version>*.mrpack / *.zip
      安装说明.txt

  D:\\PCL\\.minecraft\\DeepUnderWorld-<version>.mrpack   (drag into PCL2)

Existing versions and the global mods folder are never modified.
"""

from __future__ import annotations

import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DIST = ROOT / "modpack" / "dist"
DOWNLOAD_CACHE = ROOT / "modpack" / "build" / "downloads"
TARGET_ROOT = Path(r"D:\PCL\.minecraft")


def version_key(value: str) -> list[int]:
    match = re.search(r"(\d+(?:\.\d+)*)$", value)
    return [int(part) for part in match.group(1).split(".")] if match else []


def latest_version() -> str:
    versions = []

    for path in DIST.glob("DeepUnderWorld-*.mrpack"):
        match = re.fullmatch(r"DeepUnderWorld-(.+?)(?:-offline)?\.mrpack", path.name)

        if match:
            versions.append(match.group(1))

    if not versions:
        raise SystemExit("No DeepUnderWorld mrpack found in modpack/dist; run build_pack.py first.")

    return sorted(set(versions), key=version_key)[-1]


def copy_file(source: Path, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, destination)


def main() -> None:
    if not TARGET_ROOT.exists():
        raise SystemExit(f"PCL2 directory not found: {TARGET_ROOT}")

    version = latest_version()
    destination = TARGET_ROOT / f"DeepUnderWorld-{version}"
    destination.mkdir(parents=True, exist_ok=True)

    mods_dir = destination / "mods"
    mods_dir.mkdir(parents=True, exist_ok=True)

    # Clear only the jars from our previous deployment; leave unknown files alone.
    for old_jar in mods_dir.glob("*.jar"):
        old_jar.unlink()

    mod_count = 0
    manifest_path = DOWNLOAD_CACHE.parent / "pack_mods.txt"
    allowed = None

    if manifest_path.exists():
        allowed = {line.strip() for line in manifest_path.read_text(encoding="utf-8").splitlines() if line.strip()}

    for jar in sorted(DOWNLOAD_CACHE.glob("*.jar")):
        if allowed is not None and jar.name not in allowed:
            continue

        copy_file(jar, mods_dir / jar.name)
        mod_count += 1

    local_jar = ROOT / "build" / "libs" / "WCNM-Alpha-1.0.0.jar"

    if not local_jar.exists():
        raise SystemExit(f"Compiled mod jar missing: {local_jar}")

    copy_file(local_jar, mods_dir / local_jar.name)
    mod_count += 1

    for archive in DOWNLOAD_CACHE.glob("*.zip"):
        copy_file(archive, destination / "shaderpacks" / archive.name)

    # A friendlier default for development and exploration; /wcnm tud visibleDistance reset
    # restores the dimension's own fog.
    config_dir = destination / "config"
    config_dir.mkdir(parents=True, exist_ok=True)
    (config_dir / "wcnm-client.properties").write_text(
        "underworld.visibleDistance=128\nunderworld.effects=false\n", encoding="utf-8"
    )

    for pack in DIST.glob(f"DeepUnderWorld-{version}*"):
        copy_file(pack, destination / pack.name)

    # Root-level mrpack for easy drag-and-drop into PCL2.
    online_pack = DIST / f"DeepUnderWorld-{version}.mrpack"

    if online_pack.exists():
        copy_file(online_pack, TARGET_ROOT / online_pack.name)

    (destination / "安装说明.txt").write_text(
        f"""Deep UnderWorld {version}
==============================

需要 Java 25。Minecraft 26.3 / Fabric Loader 0.20.1。

方式 A（推荐）
1. 把 D:\\PCL\\.minecraft\\DeepUnderWorld-{version}.mrpack 拖进 PCL2 窗口。
2. PCL2 会自动安装 Minecraft 26.3 + Fabric，并把 mods 复制到实例。
3. 启动后使用 /wcnm tp 进入冥界。

方式 B（手动）
1. 在 PCL2 安装 Minecraft 26.3 + Fabric Loader 0.20.1。
2. 把本文件夹里的 mods、config、shaderpacks 复制到该版本目录。
3. 启动游戏。

冥界出生点坐标是 (0, 81, 0)。使用 /wcnm tp 进入。
/wcnm find radius 300 列出附近冥筑；/wcnm find structure ghost-gate 定位指定结构。
/wcnm find biome underworld 定位群系；/wcnm ls 查看当前区块信息。
雾效默认关闭；/wcnm tud effects on 可恢复，/wcnm tud effects off 可关闭。

本文件夹中的 mods 共 {mod_count} 个，已包含 WCNM 本体。
""",
        encoding="utf-8",
    )

    print(f"Deployed {version} -> {destination}")
    print(f"  mods: {mod_count}")
    print(f"  mrpack: {TARGET_ROOT / online_pack.name}")


if __name__ == "__main__":
    main()

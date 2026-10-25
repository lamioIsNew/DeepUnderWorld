# -*- coding: utf-8 -*-
"""Create a launchable PCL2 version: Minecraft 26.3 + Fabric Loader 0.20.1.

Vanilla 26.3 libraries and the asset index 34 are already present in the user's PCL2
directory; the official client jar is taken from the local Loom cache (SHA-1 verified).
The version JSON is merged (no inheritsFrom), matching the layout PCL2 already uses.
"""

from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PCL = Path(r"D:\PCL\.minecraft")
VERSION_NAME = "Deep Under World beta-2.5.3"
DEPLOY = PCL / f"DeepUnderWorld-beta-2.5.3"
VERSION_DIR = PCL / "versions" / VERSION_NAME
CACHE_JSON = ROOT / ".tmp3" / "26.3.json"
CACHE_FABRIC = ROOT / ".tmp3" / "fabric.json"
CLIENT_JAR_CACHE = Path(r"C:\Users\Lamio\.gradle\caches\fabric-loom\26.3\minecraft-client.jar")
CLIENT_SHA1 = "e877b6a07acd633fb3bb475002175cec036e7b87"


def load_json(path: Path, url: str) -> dict:
    if path.exists():
        return json.loads(path.read_text(encoding="utf-8"))

    import urllib.request

    path.parent.mkdir(parents=True, exist_ok=True)

    with urllib.request.urlopen(url, timeout=60) as response:
        data = response.read()

    path.write_bytes(data)
    return json.loads(data.decode("utf-8"))


def file_sha1(path: Path) -> str:
    digest = hashlib.sha1()

    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)

    return digest.hexdigest()


def library_key(entry: dict) -> str:
    parts = entry["name"].split(":")
    return f"{parts[0]}:{parts[1]}"


def merge_profiles(vanilla: dict, fabric: dict) -> dict:
    libraries = list(vanilla.get("libraries", []))
    index = {library_key(entry): i for i, entry in enumerate(libraries)}

    for entry in fabric.get("libraries", []):
        key = library_key(entry)

        if key in index:
            libraries[index[key]] = entry
        else:
            index[key] = len(libraries)
            libraries.append(entry)

    arguments = json.loads(json.dumps(vanilla.get("arguments", {})))
    arguments.setdefault("game", [])
    arguments.setdefault("jvm", [])
    arguments["game"] = list(fabric.get("arguments", {}).get("game", [])) + arguments["game"]
    arguments["jvm"] = arguments["jvm"] + list(fabric.get("arguments", {}).get("jvm", []))

    merged = {
        "id": VERSION_NAME,
        "type": vanilla.get("type", "release"),
        "releaseTime": vanilla.get("releaseTime"),
        "time": vanilla.get("time"),
        "mainClass": fabric["mainClass"],
        "minimumLauncherVersion": vanilla.get("minimumLauncherVersion", 21),
        "complianceLevel": vanilla.get("complianceLevel", 1),
        "arguments": arguments,
        "libraries": libraries,
        "assetIndex": vanilla["assetIndex"],
        "assets": vanilla["assets"],
        "downloads": vanilla.get("downloads", {}),
        "javaVersion": vanilla.get("javaVersion"),
        "logging": vanilla.get("logging"),
    }
    return merged


def copy_tree(source: Path, target: Path) -> None:
    if not source.exists():
        return

    target.mkdir(parents=True, exist_ok=True)

    for item in source.iterdir():
        destination = target / item.name

        if item.is_dir():
            shutil.copytree(item, destination, dirs_exist_ok=True)
        else:
            shutil.copy2(item, destination)


def main() -> None:
    if not DEPLOY.exists():
        raise SystemExit(f"Deployment folder missing: {DEPLOY} (run deploy_to_pcl.py first)")

    vanilla = load_json(
        CACHE_JSON,
        "https://piston-meta.mojang.com/v1/packages/61fff1316bd50312624a858bbb5eb18fa45208ed/26.3.json",
    )
    fabric = load_json(
        CACHE_FABRIC,
        "https://meta.fabricmc.net/v2/versions/loader/26.3/0.20.1/profile/json",
    )
    merged = merge_profiles(vanilla, fabric)

    VERSION_DIR.mkdir(parents=True, exist_ok=True)
    (VERSION_DIR / f"{VERSION_NAME}.json").write_text(
        json.dumps(merged, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )

    if not CLIENT_JAR_CACHE.exists():
        raise SystemExit(f"26.3 client jar not found in Loom cache: {CLIENT_JAR_CACHE}")

    digest = hashlib.sha1(CLIENT_JAR_CACHE.read_bytes()).hexdigest()

    if digest != CLIENT_SHA1:
        raise SystemExit(f"Unexpected client jar SHA1: {digest}")

    shutil.copy2(CLIENT_JAR_CACHE, VERSION_DIR / f"{VERSION_NAME}.jar")

    mods_dir = VERSION_DIR / "mods"
    mods_dir.mkdir(parents=True, exist_ok=True)
    source_jars = {path.name: path for path in (DEPLOY / "mods").glob("*.jar")}

    # Remove only jars that are no longer part of the pack.  If the game is open, locked
    # jars are reported instead of aborting the whole installation.
    for old_jar in mods_dir.glob("*.jar"):
        if old_jar.name in source_jars:
            continue

        try:
            old_jar.unlink()
        except PermissionError:
            print(f"  warning: locked stale mod {old_jar.name} (close Minecraft to remove it)")

    for name, source in source_jars.items():
        target = mods_dir / name

        if target.exists() and file_sha1(target) == file_sha1(source):
            continue

        try:
            shutil.copy2(source, target)
        except PermissionError as exc:
            raise SystemExit(
                "Minecraft/PCL2 正在运行，无法替换 " + name + "。\n"
                "请先退出游戏和 PCL2，然后重新运行 build_and_deploy.bat（或只运行第 5 步）。"
            ) from exc

    # Do not overwrite client settings that the player has changed in-game.
    config_source = DEPLOY / "config"
    config_target = VERSION_DIR / "config"

    if config_source.exists():
        config_target.mkdir(parents=True, exist_ok=True)

        for item in config_source.iterdir():
            target = config_target / item.name

            if target.exists():
                continue

            if item.is_dir():
                shutil.copytree(item, target, dirs_exist_ok=True)
            else:
                shutil.copy2(item, target)

    copy_tree(DEPLOY / "shaderpacks", VERSION_DIR / "shaderpacks")
    copy_tree(DEPLOY / "resourcepacks", VERSION_DIR / "resourcepacks")

    for name in ("options.txt", "servers.dat"):
        source = PCL / name
        target = VERSION_DIR / name

        if source.exists() and not target.exists():
            shutil.copy2(source, target)

    readme = DEPLOY / "安装说明.txt"

    if readme.exists():
        shutil.copy2(readme, VERSION_DIR / readme.name)

    pcl_dir = VERSION_DIR / "PCL"
    pcl_dir.mkdir(parents=True, exist_ok=True)
    (pcl_dir / "config.json").write_text('{\n  "InstanceMigratedJava": true\n}\n', encoding="utf-8")
    (pcl_dir / "Setup.ini").write_text(
        "VersionLiteLoader:False\n"
        "VersionFabric:0.20.1\n"
        "Info:正式版 26.3, Fabric 0.20.1\n"
        "VersionVanilla:26.3\n"
        "VersionVanillaName:26.3\n"
        "ReleaseTime:2026-09-15 11:23\n"
        "Logo:pack://application:,,,/Plain Craft Launcher 2;component/Images/Blocks/Fabric.png\n"
        "VersionArgumentIndieV2:True\n"
        "State:9\n",
        encoding="utf-8",
    )

    print(f"Installed PCL2 version: {VERSION_DIR}")
    print(f"  mods: {len(list((VERSION_DIR / 'mods').glob('*.jar')))}")
    print(f"  json: {VERSION_DIR / (VERSION_NAME + '.json')}")


if __name__ == "__main__":
    main()

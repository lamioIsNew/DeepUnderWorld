# -*- coding: utf-8 -*-
"""Launch the installed PCL2 version once with an offline profile, then stop it.

Used only for smoke-testing the generated version folder.  The game window will appear for
roughly a minute while the mod list loads.
"""

from __future__ import annotations

import json
import os
import subprocess
import time
import zipfile
from pathlib import Path

PCL = Path(r"D:\PCL\.minecraft")
NAME = "Deep Under World 2.1.0"
VERSION_DIR = PCL / "versions" / NAME
JAVA = Path(r"C:\Users\Lamio\AppData\Roaming\.minecraft\runtime\java-runtime-epsilon\bin\java.exe")


def allow_windows(entry: dict) -> bool:
    rules = entry.get("rules")

    if not rules:
        return True

    allowed = False

    for rule in rules:
        os_name = rule.get("os", {}).get("name")

        if os_name is None or os_name == "windows":
            allowed = rule.get("action") == "allow"

    return allowed


def library_path(entry: dict) -> str:
    artifact = entry.get("downloads", {}).get("artifact")

    if artifact:
        return artifact["path"]

    parts = entry["name"].split(":")
    group, artifact, version = parts[:3]
    classifier = parts[3] if len(parts) > 3 else None
    filename = f"{artifact}-{version}" + (f"-{classifier}" if classifier else "") + ".jar"
    return "/".join(group.split(".")) + f"/{artifact}/{version}/{filename}"


def resolve_arguments(entries, values):
    result = []

    for entry in entries:
        if isinstance(entry, str):
            result.append(entry)
            continue

        rules = entry.get("rules", [])
        features = any("features" in rule for rule in rules)

        if features:
            continue

        if rules and not allow_windows(entry):
            continue

        value = entry.get("value")
        result.extend(value if isinstance(value, list) else [value])

    for key, value in values.items():
        result = [item.replace("${" + key + "}", value) for item in result]

    return result


def main() -> None:
    profile = json.loads((VERSION_DIR / f"{NAME}.json").read_text(encoding="utf-8"))
    windows_libs = [entry for entry in profile["libraries"] if allow_windows(entry)]
    classpath = [str(PCL / "libraries" / library_path(entry)) for entry in windows_libs]
    classpath.append(str(VERSION_DIR / f"{NAME}.jar"))

    natives = VERSION_DIR / "natives"
    natives.mkdir(exist_ok=True)

    for entry in windows_libs:
        jar = PCL / "libraries" / library_path(entry)

        if not jar.exists() or not zipfile.is_zipfile(jar):
            continue

        with zipfile.ZipFile(jar) as archive:
            for member in archive.namelist():
                if member.endswith(".dll") and "/" not in member.strip("/"):
                    target = natives / Path(member).name
                    target.write_bytes(archive.read(member))

    values = {
        "natives_directory": str(natives),
        "classpath": os.pathsep.join(classpath),
        "launcher_name": "PCL2",
        "launcher_version": "2.1.0",
        "auth_player_name": "DevTest",
        "version_name": NAME,
        "game_directory": str(VERSION_DIR),
        "assets_root": str(PCL / "assets"),
        "assets_index_name": str(profile["assets"]),
        "auth_uuid": "00000000000000000000000000000000",
        "auth_access_token": "0",
        "clientid": "0",
        "auth_xuid": "0",
        "user_type": "legacy",
        "version_type": "release",
        "resolution_width": "854",
        "resolution_height": "480",
        "quickPlayPath": "",
        "quickPlaySingleplayer": "",
        "quickPlayMultiplayer": "",
        "quickPlayRealms": "",
    }

    jvm = ["-Xms1G", "-Xmx2G"] + resolve_arguments(profile["arguments"]["jvm"], values)
    game = resolve_arguments(profile["arguments"]["game"], values)
    command = [str(JAVA)] + jvm + [profile["mainClass"]] + game

    options = VERSION_DIR / "options.txt"

    if options.exists():
        text = options.read_text(encoding="utf-8", errors="replace")
        text = text.replace("fullscreen:true", "fullscreen:false")
        options.write_text(text, encoding="utf-8")

    log_path = VERSION_DIR / "launch-test.log"
    log = log_path.open("w", encoding="utf-8", errors="replace")
    process = subprocess.Popen(command, cwd=VERSION_DIR, stdout=log, stderr=subprocess.STDOUT)
    print(f"Started client pid={process.pid}; waiting 75s...")

    time.sleep(75)

    if process.poll() is None:
        process.kill()

    log.close()

    text = log_path.read_text(encoding="utf-8", errors="replace")
    interesting = [
        line for line in text.splitlines()
        if any(key in line for key in ("wcnm", "Fabric", "Loading", "ERROR", "Exception", "Missing", "Incompatible"))
    ]
    print("\n".join(interesting[-40:]))


if __name__ == "__main__":
    main()

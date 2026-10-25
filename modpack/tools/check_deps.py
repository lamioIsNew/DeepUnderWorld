# -*- coding: utf-8 -*-
"""Inspect bundled jars and report missing Fabric mod dependencies."""

from __future__ import annotations

import io
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "build" / "pack_mods.txt"
_ALLOWED = None
if MANIFEST.exists():
    _ALLOWED = {line.strip() for line in MANIFEST.read_text(encoding="utf-8").splitlines() if line.strip()}
JARS = [
    jar
    for jar in sorted((ROOT / "build" / "downloads").glob("*.jar"))
    if _ALLOWED is None or jar.name in _ALLOWED
]

# Dependencies provided by Minecraft / Fabric Loader / Java, not by mod jars.
BUILTIN = {
    "minecraft",
    "fabricloader",
    "fabric",
    "java",
    "mixinextras",  # shipped with Fabric Loader 0.16+
    "fabric-api",
    "fabric-api-base",
    "fabric-api-lookup-api-v1",
    "fabric-biome-api-v1",
    "fabric-block-api-v1",
    "fabric-block-view-api-v2",
    "fabric-command-api-v2",
    "fabric-content-registries-v0",
    "fabric-crash-report-info-v1",
    "fabric-data-attachment-api-v1",
    "fabric-data-generation-api-v1",
    "fabric-dimensions-v1",
    "fabric-entity-events-v1",
    "fabric-events-interaction-v0",
    "fabric-events-lifecycle-v0",
    "fabric-game-rule-api-v1",
    "fabric-gametest-api-v1",
    "fabric-item-api-v1",
    "fabric-item-group-api-v1",
    "fabric-key-binding-api-v1",
    "fabric-lifecycle-events-v1",
    "fabric-loot-api-v3",
    "fabric-message-api-v1",
    "fabric-model-loading-api-v1",
    "fabric-networking-api-v1",
    "fabric-object-builder-api-v1",
    "fabric-particles-v1",
    "fabric-recipe-api-v1",
    "fabric-registry-sync-v0",
    "fabric-renderer-api-v1",
    "fabric-renderer-indigo",
    "fabric-rendering-fluids-v1",
    "fabric-rendering-v1",
    "fabric-resource-loader-v1",
    "fabric-screen-api-v1",
    "fabric-screen-handler-api-v1",
    "fabric-sound-api-v1",
    "fabric-transfer-api-v1",
    "fabric-transitive-access-wideners-v1",
    "fabric-convention-tags-v2",
    "fabric-client-tags-api-v1",
}


def main() -> None:
    verbose = "-v" in sys.argv
    mods: dict[str, tuple[str, dict]] = {}
    nested_mods: set[str] = set()
    for jar in JARS:
        try:
            with zipfile.ZipFile(jar) as z:
                raw = z.read("fabric.mod.json")
                nested = [
                    n
                    for n in z.namelist()
                    if n.startswith("META-INF/jars/") and n.endswith(".jar")
                ]
                for n in nested:
                    try:
                        inner = zipfile.ZipFile(io.BytesIO(z.read(n)))
                        inner_meta = json.loads(inner.read("fabric.mod.json"), strict=False)
                        nested_mods.add(inner_meta["id"])
                    except (KeyError, zipfile.BadZipFile, json.JSONDecodeError):
                        pass
        except (KeyError, zipfile.BadZipFile):
            print(f"[skip] {jar.name}: no fabric.mod.json")
            continue
        try:
            meta = json.loads(raw)
        except json.JSONDecodeError:
            # A few mods ship literal control characters in JSON strings.
            meta = json.loads(raw, strict=False)
        mods[meta["id"]] = (jar.name, meta)

    provided = set(mods) | nested_mods | BUILTIN
    problems = 0
    for mod_id, (jar, meta) in sorted(mods.items()):
        deps = meta.get("depends", {})
        missing = [
            f"{k} {v}"
            for k, v in deps.items()
            if k not in provided and not k.startswith("fabric-")
        ]
        mc = deps.get("minecraft", "-")
        loader = deps.get("fabricloader", "-")
        java = deps.get("java", "-")
        flag = "!" if missing else " "
        print(f"{flag} {mod_id:<28} {meta.get('version','?'):<24} mc={mc} loader={loader} java={java}")
        if verbose:
            for k, v in deps.items():
                print(f"    depends {k}: {v}")
        for m in missing:
            problems += 1
            print(f"    MISSING: {m}  (from {jar})")

    print(f"\n{len(mods)} mods, {problems} unresolved dependency entries.")
    if problems:
        raise SystemExit(1)


if __name__ == "__main__":
    main()

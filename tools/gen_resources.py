#!/usr/bin/env python3
"""Generiert die Ressourcen-JSON (Blockstates, Modelle, Loot, Rezepte, Tags,
Lokalisierung) fuer alle Reaktor-Typen und Bausteine aus akw_data.

Format passend zu Minecraft 1.21.1 (Rezept-Result via "id", Ordnernamen singular:
loot_table / recipe).
"""
import os, json
from akw_data import REACTOR_TYPES, DECOR_BLOCKS

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "akw")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "akw")
MC_TAGS = os.path.join(ROOT, "src", "main", "resources", "data", "minecraft", "tags", "block")


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print("  ->", os.path.relpath(path, ROOT))


def loot_self(block_id):
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1.0,
            "bonus_rolls": 0.0,
            "entries": [{"type": "minecraft:item", "name": f"akw:{block_id}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    }


def recipe(rec, block_id, count):
    return {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "pattern": rec["pattern"],
        "key": {k: {"item": v} for k, v in rec["key"].items()},
        "result": {"id": f"akw:{block_id}", "count": count},
    }


def main():
    print("AKW-Ressourcen werden generiert...")

    for r in REACTOR_TYPES:
        rid = r["id"]
        variants = {}
        for facing, y in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]:
            for lit in (False, True):
                model = f"akw:block/{rid}_on" if lit else f"akw:block/{rid}"
                v = {"model": model}
                if y:
                    v["y"] = y
                variants[f"facing={facing},lit={'true' if lit else 'false'}"] = v
        w(os.path.join(ASSETS, "blockstates", rid + ".json"), {"variants": variants})

        w(os.path.join(ASSETS, "models", "block", rid + ".json"), {
            "parent": "minecraft:block/orientable",
            "textures": {"top": f"akw:block/{rid}_top",
                         "front": f"akw:block/{rid}_front",
                         "side": f"akw:block/{rid}_side"},
        })
        w(os.path.join(ASSETS, "models", "block", rid + "_on.json"), {
            "parent": "minecraft:block/orientable",
            "textures": {"top": f"akw:block/{rid}_top",
                         "front": f"akw:block/{rid}_front_on",
                         "side": f"akw:block/{rid}_side"},
        })
        w(os.path.join(ASSETS, "models", "item", rid + ".json"), {"parent": f"akw:block/{rid}"})
        w(os.path.join(DATA, "loot_table", "blocks", rid + ".json"), loot_self(rid))
        w(os.path.join(DATA, "recipe", rid + ".json"), recipe(r["recipe"], rid, r.get("count", 1)))

    for d in DECOR_BLOCKS:
        did = d["id"]
        w(os.path.join(ASSETS, "blockstates", did + ".json"),
          {"variants": {"": {"model": f"akw:block/{did}"}}})
        w(os.path.join(ASSETS, "models", "block", did + ".json"),
          {"parent": "minecraft:block/cube_all", "textures": {"all": f"akw:block/{did}"}})
        w(os.path.join(ASSETS, "models", "item", did + ".json"), {"parent": f"akw:block/{did}"})
        w(os.path.join(DATA, "loot_table", "blocks", did + ".json"), loot_self(did))
        w(os.path.join(DATA, "recipe", did + ".json"), recipe(d["recipe"], did, d.get("count", 1)))

    # mineable/pickaxe-Tag: alle AKW-Bloecke
    all_ids = (["uranium_ore", "deepslate_uranium_ore"]
               + [r["id"] for r in REACTOR_TYPES]
               + [d["id"] for d in DECOR_BLOCKS])
    w(os.path.join(MC_TAGS, "mineable", "pickaxe.json"),
      {"replace": False, "values": [f"akw:{i}" for i in all_ids]})

    # Lokalisierung mergen
    for lang_file, key in [("de_de", "de"), ("en_us", "en")]:
        path = os.path.join(ASSETS, "lang", lang_file + ".json")
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)
        for r in REACTOR_TYPES:
            data[f"block.akw.{r['id']}"] = r[key]
        for d in DECOR_BLOCKS:
            data[f"block.akw.{d['id']}"] = d[key]
        w(path, data)

    print("Fertig.")


if __name__ == "__main__":
    main()

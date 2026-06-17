"""Gemeinsame Datendefinition fuer Textur- und Ressourcen-Generator.

Eine einzige Quelle der Wahrheit fuer alle Reaktor-Typen und Bausteine.
Die Java-Seite spiegelt dieselben Werte (Kapazitaet/Leistung/Brenndauer) in
ch.danielt.akw.registry.ModBlocks wider.
"""

# id, de, en, metal-Grundfarbe, Akzentfarbe, Kapazitaet, FE/Tick, max. Abgabe,
# Brenndauer pro Brennstab (Ticks), Rezept
REACTOR_TYPES = [
    {
        "id": "nuclear_reactor", "de": "Reaktor", "en": "Nuclear Reactor",
        "metal": (120, 124, 130), "accent": (60, 230, 90),
        "capacity": 100_000, "gen": 40, "extract": 512, "burn": 1600,
        "recipe": {
            "pattern": ["III", "UFU", "IRI"],
            "key": {"I": "minecraft:iron_ingot", "U": "akw:uranium_ingot",
                    "F": "minecraft:furnace", "R": "minecraft:redstone"},
        },
    },
    {
        "id": "advanced_nuclear_reactor", "de": "Fortgeschrittener Reaktor",
        "en": "Advanced Nuclear Reactor",
        "metal": (90, 110, 140), "accent": (80, 200, 230),
        "capacity": 400_000, "gen": 120, "extract": 2048, "burn": 2000,
        "recipe": {
            "pattern": ["GCG", "CNC", "GCG"],
            "key": {"G": "minecraft:gold_ingot", "C": "akw:control_rod_block",
                    "N": "akw:nuclear_reactor"},
        },
    },
    {
        "id": "elite_nuclear_reactor", "de": "Elite-Reaktor",
        "en": "Elite Nuclear Reactor",
        "metal": (70, 74, 82), "accent": (240, 200, 60),
        "capacity": 1_600_000, "gen": 360, "extract": 8192, "burn": 2400,
        "recipe": {
            "pattern": ["DCD", "CAC", "DCD"],
            "key": {"D": "minecraft:diamond", "C": "akw:cooling_pipe",
                    "A": "akw:advanced_nuclear_reactor"},
        },
    },
    {
        "id": "breeder_reactor", "de": "Brutreaktor", "en": "Breeder Reactor",
        "metal": (150, 110, 80), "accent": (255, 150, 60),
        "capacity": 800_000, "gen": 240, "extract": 4096, "burn": 2200,
        "recipe": {
            "pattern": ["CCC", "UNU", "CCC"],
            "key": {"C": "minecraft:copper_ingot", "U": "akw:enriched_uranium_block",
                    "N": "akw:nuclear_reactor"},
        },
    },
    {
        "id": "thorium_reactor", "de": "Thorium-Reaktor", "en": "Thorium Reactor",
        "metal": (90, 130, 120), "accent": (60, 230, 180),
        "capacity": 600_000, "gen": 180, "extract": 3072, "burn": 2600,
        "recipe": {
            "pattern": ["MEM", "ENE", "MEM"],
            "key": {"M": "minecraft:emerald", "E": "akw:enriched_uranium_block",
                    "N": "akw:nuclear_reactor"},
        },
    },
    {
        "id": "fusion_reactor", "de": "Fusionsreaktor", "en": "Fusion Reactor",
        "metal": (170, 175, 185), "accent": (235, 90, 220),
        "capacity": 4_000_000, "gen": 1000, "extract": 32768, "burn": 1200,
        "recipe": {
            "pattern": ["NDN", "DED", "NDN"],
            "key": {"N": "minecraft:netherite_ingot", "D": "minecraft:diamond_block",
                    "E": "akw:elite_nuclear_reactor"},
        },
    },
]

# Einfache Bausteine (Vollwuerfel, eine Textur)
DECOR_BLOCKS = [
    {
        "id": "reactor_core", "de": "Reaktorkern", "en": "Reactor Core",
        "recipe": {"pattern": ["UUU", "UFU", "UUU"],
                   "key": {"U": "akw:uranium_ingot", "F": "akw:fuel_rod"}},
    },
    {
        "id": "control_rod_block", "de": "Steuerstab-Block", "en": "Control Rod Block",
        "recipe": {"pattern": ["IUI", "IUI", "IUI"],
                   "key": {"I": "minecraft:iron_ingot", "U": "akw:uranium_ingot"}},
    },
    {
        "id": "cooling_pipe", "de": "Kühlrohr", "en": "Cooling Pipe", "count": 2,
        "recipe": {"pattern": ["C C", "C C", "C C"],
                   "key": {"C": "minecraft:copper_ingot"}},
    },
    {
        "id": "lead_block", "de": "Blei-Block", "en": "Lead Block",
        "recipe": {"pattern": ["IUI", "UIU", "IUI"],
                   "key": {"I": "minecraft:iron_ingot", "U": "akw:uranium_ingot"}},
    },
    {
        "id": "waste_container", "de": "Abfallbehälter", "en": "Waste Container",
        "recipe": {"pattern": ["IFI", "IFI", "III"],
                   "key": {"I": "minecraft:iron_ingot", "F": "akw:fuel_rod"}},
    },
    {
        "id": "enriched_uranium_block", "de": "Angereicherter-Uran-Block",
        "en": "Enriched Uranium Block",
        "recipe": {"pattern": ["UUU", "UUU", "UUU"],
                   "key": {"U": "akw:uranium_ingot"}},
    },
]

#!/usr/bin/env python3
"""Generiert die AKW-Mod-Texturen (Pixel-Art) ohne externe Abhaengigkeiten.

Schreibt 16x16-RGBA-PNGs fuer Items/Bloecke und ein 512x512-Mod-Icon direkt
an ihre Zielorte im Ressourcen-Baum. Reiner Python-stdlib PNG-Encoder (zlib).
"""
import os, zlib, struct, math, random

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "akw")
ITEM_DIR = os.path.join(ASSETS, "textures", "item")
BLOCK_DIR = os.path.join(ASSETS, "textures", "block")


def write_png(path, pixels, w, h):
    """pixels: flache Liste von (r,g,b,a)-Tupeln, Zeile fuer Zeile."""
    raw = bytearray()
    for y in range(h):
        raw.append(0)  # Filter-Typ 0 (None) pro Scanline
        for x in range(w):
            r, g, b, a = pixels[y * w + x]
            raw += bytes((r & 255, g & 255, b & 255, a & 255))

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)  # 8-bit, RGBA
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", ihdr)
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)
    print("  ->", os.path.relpath(path, ROOT))


def blank(w, h):
    return [(0, 0, 0, 0)] * (w * h)


def jitter(c, amt, rnd):
    return tuple(max(0, min(255, ch + rnd.randint(-amt, amt))) for ch in c[:3]) + (255,)


# ---------------------------------------------------------------- Bloecke
def ore_texture(base, base_dark, seed):
    """Stein-/Tiefenschiefer-Hintergrund mit gruenen Erz-Einsprengseln."""
    rnd = random.Random(seed)
    px = []
    for _ in range(16 * 16):
        c = base if rnd.random() > 0.35 else base_dark
        px.append(jitter(c, 10, rnd))
    green = (70, 200, 85)
    green_hi = (170, 255, 150)
    green_lo = (40, 140, 60)
    spots = [(3, 4), (4, 4), (3, 5), (10, 3), (11, 3), (11, 4),
             (6, 9), (7, 9), (6, 10), (12, 11), (12, 12), (11, 12),
             (2, 11), (3, 11), (8, 6)]
    for (x, y) in spots:
        i = y * 16 + x
        px[i] = green + (255,)
        if x + 1 < 16:
            px[i + 1] = green_lo + (255,)
        if y > 0:
            px[(y - 1) * 16 + x] = green_hi + (255,)  # Glanzlicht oben
    return px


# ------------------------------------------------------------------ Items
def raw_uranium():
    """Unregelmaessiger grau-gruenlicher Klumpen mit leuchtenden Sprenkeln."""
    rnd = random.Random(7)
    px = blank(16, 16)
    base = (96, 112, 88)
    shade = (66, 80, 60)
    hi = (140, 158, 128)
    mask = [
        "................",
        "................",
        "....######......",
        "...########.....",
        "..##########....",
        "..###########...",
        ".############...",
        ".############...",
        ".############...",
        "..###########...",
        "..##########....",
        "...########.....",
        "...#######......",
        "....#####.......",
        "................",
        "................",
    ]
    for y in range(16):
        for x in range(16):
            if mask[y][x] == "#":
                c = base
                if x <= 3 or y <= 3:
                    c = hi
                if x >= 11 or y >= 11:
                    c = shade
                px[y * 16 + x] = jitter(c, 12, rnd)
    for (x, y) in [(5, 5), (8, 7), (6, 9), (9, 10), (4, 8), (10, 6)]:
        px[y * 16 + x] = (150, 255, 130, 255)
    return px


def uranium_ingot():
    """Klassische Barren-Form, matt grau-gruen-metallisch."""
    px = blank(16, 16)
    body = (104, 124, 96)
    top = (150, 170, 138)
    bot = (70, 86, 64)
    edge = (54, 66, 50)
    rows = {
        5: (4, 12), 6: (3, 13), 7: (3, 13),
        8: (3, 13), 9: (3, 13), 10: (2, 13), 11: (3, 12),
    }
    for y, (x0, x1) in rows.items():
        for x in range(x0, x1):
            c = body
            if y == 5:
                c = top
            elif y >= 10:
                c = bot
            if x == x0 or x == x1 - 1:
                c = edge
            px[y * 16 + x] = c + (255,)
    for x in range(5, 11):
        px[6 * 16 + x] = (130, 190, 120, 255)
    return px


def fuel_rod():
    """Schlanker vertikaler Stab, metallische Huelle, Kappen, gruener Glueh-Akzent."""
    px = blank(16, 16)
    casing = (152, 158, 165)
    casing_hi = (190, 196, 202)
    casing_lo = (96, 102, 110)
    cap = (74, 78, 84)
    glow = (90, 235, 120)
    glow_hi = (170, 255, 180)
    x0, x1 = 6, 10
    for y in range(2, 14):
        for x in range(x0, x1):
            c = casing
            if x == x0:
                c = casing_hi
            elif x == x1 - 1:
                c = casing_lo
            px[y * 16 + x] = c + (255,)
    for y in (2, 3, 12, 13):
        for x in range(x0, x1):
            px[y * 16 + x] = cap + (255,)
    for y in range(5, 11):
        px[y * 16 + 7] = glow + (255,)
        px[y * 16 + 8] = glow_hi + (255,)
    return px


# ------------------------------------------------------------------- Icon
def icon():
    """512x512 Mod-Logo: gruener Halo + Radioaktiv-Symbol, gruen/gelb."""
    W = H = 512
    px = [(0, 0, 0, 0)] * (W * H)
    cx, cy = W / 2, H / 2
    bg_top = (26, 40, 32)
    bg_bot = (12, 18, 16)
    for y in range(H):
        t = y / H
        row = tuple(int(bg_top[i] * (1 - t) + bg_bot[i] * t) for i in range(3))
        for x in range(W):
            px[y * W + x] = row + (255,)

    for y in range(H):
        for x in range(W):
            d = math.hypot(x - cx, y - cy)
            if 196 < d < 232:
                a = 1 - abs(d - 214) / 18
                base = px[y * W + x]
                g = (60, 200, 90)
                px[y * W + x] = tuple(
                    int(base[i] * (1 - 0.5 * a) + g[i] * 0.5 * a) for i in range(3)
                ) + (255,)

    yellow = (235, 222, 48)
    r_in, r_out = 36, 150
    centers = [math.radians(a) for a in (90, 210, 330)]
    half = math.radians(30)
    for y in range(H):
        for x in range(W):
            dx, dy = x - cx, y - cy
            d = math.hypot(dx, dy)
            if d <= r_in + 6:
                if d <= r_in:
                    px[y * W + x] = yellow + (255,)
                continue
            if r_in < d <= r_out:
                ang = math.atan2(dy, dx)
                for c in centers:
                    da = abs((ang - c + math.pi) % (2 * math.pi) - math.pi)
                    if da <= half:
                        px[y * W + x] = yellow + (255,)
                        break
    return px, W, H


def main():
    print("AKW-Texturen werden generiert...")
    write_png(os.path.join(BLOCK_DIR, "uranium_ore.png"),
              ore_texture((128, 128, 128), (110, 110, 110), 11), 16, 16)
    write_png(os.path.join(BLOCK_DIR, "deepslate_uranium_ore.png"),
              ore_texture((78, 78, 84), (60, 60, 66), 23), 16, 16)
    write_png(os.path.join(ITEM_DIR, "raw_uranium.png"), raw_uranium(), 16, 16)
    write_png(os.path.join(ITEM_DIR, "uranium_ingot.png"), uranium_ingot(), 16, 16)
    write_png(os.path.join(ITEM_DIR, "fuel_rod.png"), fuel_rod(), 16, 16)
    ipx, w, h = icon()
    write_png(os.path.join(ASSETS, "icon.png"), ipx, w, h)
    print("Fertig.")


if __name__ == "__main__":
    main()

# -*- coding: utf-8 -*-
"""Generate DeepUnderWorld block/item assets and the built-in Chinese underworld texture pack."""

from __future__ import annotations

import json
import math
import random
import shutil
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "wcnm"
PACK = ROOT / "src" / "main" / "resources" / "resourcepacks" / "deep_underworld"

INK = (8, 5, 11, 255)
INK2 = (17, 10, 22, 255)
INK3 = (30, 17, 38, 255)
PURPLE = (58, 26, 66, 255)
RED = (105, 14, 28, 255)
RED2 = (190, 30, 48, 255)
CYAN = (86, 235, 214, 255)
CYAN2 = (24, 112, 108, 255)
GOLD = (176, 136, 74, 255)
BONE = (214, 202, 184, 255)
CLEAR = (0, 0, 0, 0)


def img(w: int, h: int, color=CLEAR) -> Image.Image:
    return Image.new("RGBA", (w, h), color)


def px(im: Image.Image, x: int, y: int, c) -> None:
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), c)


def rnd_color(rng: random.Random, choices):
    return rng.choice(choices)


def stone_base(rng: random.Random, tint=INK2) -> Image.Image:
    im = img(16, 16, INK)
    for _ in range(70):
        px(im, rng.randrange(16), rng.randrange(16), rnd_color(rng, [INK2, INK3, tint]))
    # Faint mineral veins.
    for _ in range(3):
        x, y = rng.randrange(16), rng.randrange(16)
        for _ in range(rng.randrange(3, 7)):
            px(im, x, y, PURPLE)
            x = (x + rng.choice([-1, 1])) % 16
            y = (y + rng.choice([-1, 0, 1])) % 16
    return im


def make_yin_stone() -> Image.Image:
    rng = random.Random(0x71A5)
    im = stone_base(rng, (20, 13, 26))
    for _ in range(5):
        px(im, rng.randrange(16), rng.randrange(16), (48, 22, 54, 255))
    return im


def make_obsidian() -> Image.Image:
    rng = random.Random(0x0B51D1A)
    im = stone_base(rng, (22, 12, 28))
    # Chinese cloud hook motif, kept subtle and tileable.
    for x, y in [(3, 4), (4, 3), (5, 3), (6, 4), (6, 5), (7, 5), (8, 4), (9, 3), (10, 3), (11, 4)]:
        px(im, x, y, PURPLE)
    for x, y in [(4, 12), (5, 11), (6, 11), (7, 12), (8, 13), (9, 12), (10, 11), (11, 11)]:
        px(im, x, y, (38, 20, 46, 255))
    return im


def make_crying() -> Image.Image:
    rng = random.Random(0xC91)
    im = make_obsidian()
    for i in range(5):
        x = rng.randrange(16)
        top = rng.randrange(1, 7)
        length = rng.randrange(4, 9)
        color = CYAN if i % 2 == 0 else RED2
        for y in range(top, min(16, top + length)):
            px(im, x, y, (*color[:3], 190))
        px(im, x, top, (230, 255, 250, 255) if i % 2 == 0 else (255, 190, 180, 255))
    return im


def make_bricks() -> Image.Image:
    rng = random.Random(0xB21C)
    im = img(16, 16, (11, 6, 13, 255))
    for row in range(2):
        y0 = row * 8
        offset = 0 if row == 0 else 4
        for bx in range(-1, 3):
            x0 = bx * 8 + offset
            for y in range(y0 + 1, y0 + 7):
                for x in range(x0 + 1, x0 + 7):
                    if 0 <= x < 16:
                        px(im, x, y, rnd_color(rng, [(19, 13, 24, 255), (23, 16, 29, 255), (15, 9, 19, 255)]))
            if 0 <= x0 + 1 < 16:
                px(im, x0 + 1, y0 + 1, (42, 24, 48, 255))
            if 0 <= x0 + 6 < 16:
                px(im, x0 + 6, y0 + 6, (7, 4, 9, 255))
    for _ in range(4):
        x, y = rng.randrange(16), rng.randrange(16)
        px(im, x, y, RED)
    return im


def make_chiseled() -> Image.Image:
    im = img(16, 16, (14, 9, 18, 255))
    # 回纹 border.
    for i in range(2, 14):
        for c, xy in [(PURPLE, (i, 1)), (PURPLE, (i, 14)), (PURPLE, (1, i)), (PURPLE, (14, i))]:
            px(im, xy[0], xy[1], c)
    for x, y in [(5, 4), (6, 4), (7, 4), (10, 4), (4, 5), (4, 6), (4, 7), (11, 5), (11, 6), (11, 7),
                 (5, 11), (6, 11), (7, 11), (10, 11), (4, 8), (4, 9), (11, 8), (11, 9)]:
        px(im, x, y, (70, 34, 78, 255))
    # Central seal.
    for x, y in [(7, 7), (8, 7), (7, 8), (8, 8), (6, 6), (9, 6), (6, 9), (9, 9)]:
        px(im, x, y, RED2)
    px(im, 7, 6, GOLD)
    px(im, 8, 9, GOLD)
    return im


def make_soul_block() -> Image.Image:
    """Soul-sand-like ground block, but with Chinese ghost-face motifs."""
    rng = random.Random(0x50F0B10C)
    im = img(16, 16, (48, 32, 46, 255))

    for _ in range(90):
        px(im, rng.randrange(16), rng.randrange(16),
           rnd_color(rng, [(58, 40, 54, 255), (38, 24, 38, 255), (66, 46, 62, 255), (30, 18, 30, 255)]))

    # Twisted soul-sand-like ridges.
    for _ in range(4):
        x, y = rng.randrange(16), rng.randrange(8)
        for _ in range(rng.randrange(4, 9)):
            px(im, x, y, (78, 54, 70, 255))
            px(im, x, y + 1, (32, 20, 32, 255))
            x = (x + rng.choice([-1, 1])) % 16
            y = (y + rng.choice([-1, 0, 1])) % 16

    # Two ghost faces with slanted eyes and tiny ghost-fire pupils.
    faces = [
        (2, 3, 5, 5),
        (9, 9, 4, 4),
    ]

    for fx, fy, fw, fh in faces:
        for x in range(fx, fx + fw):
            for y in range(fy, fy + fh):
                if (x == fx or x == fx + fw - 1) or (y == fy or y == fy + fh - 1):
                    px(im, x, y, (82, 58, 74, 255))
        # Slanted eyes.
        left_eye = [(fx + 1, fy + 1), (fx + 2, fy + 2), (fx + 1, fy + 2)]
        right_eye = [(fx + fw - 2, fy + 1), (fx + fw - 3, fy + 2), (fx + fw - 2, fy + 2)]

        for x, y in left_eye + right_eye:
            px(im, x, y, (12, 8, 12, 255))

        px(im, fx + 1, fy + 1, CYAN)
        px(im, fx + fw - 2, fy + 1, CYAN)
        # Small mouth.
        for x in range(fx + 2, fx + fw - 2):
            px(im, x, fy + fh - 2, (24, 14, 24, 255))

    return im


def make_ashen_soil() -> Image.Image:
    rng = random.Random(0xA5E17)
    im = img(16, 16, (32, 29, 30, 255))
    for _ in range(90):
        px(im, rng.randrange(16), rng.randrange(16),
           rnd_color(rng, [(40, 37, 38, 255), (26, 23, 24, 255), (48, 44, 43, 255), (22, 19, 21, 255)]))
    for _ in range(14):
        px(im, rng.randrange(16), rng.randrange(16), (96, 91, 84, 255))
    for _ in range(5):
        x, y = rng.randrange(16), rng.randrange(16)
        for _ in range(rng.randrange(2, 5)):
            px(im, x, y, (18, 15, 16, 255))
            x = (x + rng.choice([-1, 1])) % 16
            y = (y + 1) % 16
    return im


def make_soul_gravel() -> Image.Image:
    rng = random.Random(0x6A4E1)
    im = img(16, 16, (31, 26, 35, 255))
    for _ in range(75):
        px(im, rng.randrange(16), rng.randrange(16),
           rnd_color(rng, [(38, 32, 43, 255), (25, 21, 29, 255), (47, 39, 53, 255), (20, 17, 24, 255)]))
    for _ in range(12):
        x, y = rng.randrange(15), rng.randrange(15)
        c = rnd_color(rng, [(62, 52, 71, 255), (46, 39, 54, 255), (74, 62, 84, 255)])
        px(im, x, y, c)
        px(im, x + 1, y, c)
        px(im, x, y + 1, (22, 18, 27, 255))
    for _ in range(4):
        px(im, rng.randrange(16), rng.randrange(16), (92, 180, 170, 255))
    return im


def make_bone_soil() -> Image.Image:
    rng = random.Random(0xB0E5)
    im = img(16, 16, (37, 33, 31, 255))
    for _ in range(80):
        px(im, rng.randrange(16), rng.randrange(16),
           rnd_color(rng, [(44, 39, 36, 255), (30, 27, 26, 255), (52, 46, 41, 255)]))
    for _ in range(18):
        x, y = rng.randrange(16), rng.randrange(16)
        length = rng.randrange(1, 4)
        horizontal = rng.random() < 0.5
        for i in range(length):
            px(im, x + (i if horizontal else 0), y + (0 if horizontal else i), rnd_color(rng, [BONE, (172, 160, 140, 255)]))
    for _ in range(5):
        px(im, rng.randrange(16), rng.randrange(16), (72, 64, 55, 255))
    return im


def make_blood_moss() -> Image.Image:
    rng = random.Random(0xB100D)
    im = img(16, 16, (39, 12, 17, 255))
    for _ in range(85):
        px(im, rng.randrange(16), rng.randrange(16),
           rnd_color(rng, [(52, 15, 21, 255), (31, 8, 13, 255), (68, 18, 26, 255), (24, 7, 11, 255)]))
    for _ in range(8):
        x, y = rng.randrange(16), rng.randrange(16)
        for _ in range(rng.randrange(3, 7)):
            px(im, x, y, rnd_color(rng, [(96, 19, 30, 255), (126, 27, 39, 255), (73, 14, 23, 255)]))
            x = (x + rng.choice([-1, 0, 1])) % 16
            y = (y + rng.choice([-1, 0, 1])) % 16
    for _ in range(3):
        px(im, rng.randrange(16), rng.randrange(16), (96, 220, 190, 255))
    return im


def make_soul_stone() -> Image.Image:
    rng = random.Random(0x50A15)
    im = stone_base(rng, (24, 24, 34))
    for _ in range(5):
        x, y = rng.randrange(16), rng.randrange(16)
        for _ in range(rng.randrange(4, 8)):
            px(im, x, y, rnd_color(rng, [CYAN2, (46, 150, 140, 255), (102, 238, 220, 255)]))
            x = (x + rng.choice([-1, 0, 1])) % 16
            y = (y + rng.choice([-1, 0, 1])) % 16
    return im


def make_soul_grass() -> Image.Image:
    rng = random.Random(0x6A55)
    im = img(16, 16, CLEAR)
    blades = [(3, 12), (5, 10), (7, 7), (9, 8), (11, 11), (13, 13), (8, 5)]
    for x, top in blades:
        lean = rng.choice([-1, 0, 1])
        for y in range(15, top - 1, -1):
            shade = (40 + (15 - y) * 3, 90 + (15 - y) * 5, 78 + (15 - y) * 4, 255)
            px(im, x, y, shade)
            if y % 3 == 0:
                px(im, (x + lean) % 16, y, (26, 62, 56, 255))
        if top < 10:
            px(im, (x + lean) % 16, top - 1, (120, 230, 210, 255))
    return im


def make_ghost_soldier_texture() -> Image.Image:
    rng = random.Random(0x501D1E)
    base = (48, 64, 74, 255)
    im = img(64, 32, base)
    for _ in range(700):
        px(im, rng.randrange(64), rng.randrange(32),
           rnd_color(rng, [(58, 78, 86, 255), (34, 48, 56, 255), (76, 98, 104, 255), (26, 36, 44, 255)]))
    # Armour plates and bone highlights.
    for _ in range(26):
        x, y = rng.randrange(64), rng.randrange(32)
        for i in range(rng.randrange(2, 5)):
            px(im, (x + i) % 64, y, (110, 128, 126, 255))
            px(im, (x + i) % 64, (y + 1) % 32, (26, 34, 40, 255))
    # Skull front: dark sockets with red pupils.
    for x in range(8, 16):
        for y in range(8, 16):
            px(im, x, y, (154, 170, 158, 255))
    for x, y in [(9, 11), (10, 11), (13, 11), (14, 11)]:
        px(im, x, y, (20, 8, 10, 255))
    px(im, 9, 11, (230, 42, 52, 255))
    px(im, 14, 11, (230, 42, 52, 255))
    for x in range(11, 14):
        px(im, x, 14, (38, 28, 30, 255))
    return im


def make_underworld_hound_texture() -> Image.Image:
    rng = random.Random(0x40D0)
    base = (36, 28, 27, 255)
    im = img(64, 32, base)
    for _ in range(700):
        px(im, rng.randrange(64), rng.randrange(32),
           rnd_color(rng, [(48, 36, 33, 255), (25, 19, 19, 255), (60, 44, 38, 255), (18, 13, 14, 255)]))
    # Spine ridge and ribs.
    for x in range(16, 44):
        px(im, x, 14, (82, 62, 52, 255))
        if x % 4 == 0:
            px(im, x, 15, (20, 14, 14, 255))
    # Red eyes on the head texture region.
    for x, y in [(5, 6), (6, 6), (9, 6), (10, 6)]:
        px(im, x, y, (220, 32, 30, 255))
    for x in range(5, 11):
        px(im, x, 10, (96, 16, 18, 255))
    return im


def make_wandering_soul_texture() -> Image.Image:
    rng = random.Random(0x50F1)
    base = (82, 108, 112, 255)
    im = img(64, 64, base)
    for _ in range(900):
        px(im, rng.randrange(64), rng.randrange(64),
           rnd_color(rng, [(96, 132, 134, 255), (62, 82, 89, 255), (108, 152, 150, 255), (48, 66, 73, 255)]))
    for _ in range(34):
        x, y = rng.randrange(64), rng.randrange(64)
        width = rng.randrange(1, 4)
        for i in range(rng.randrange(2, 6)):
            px(im, x + i, y, (36, 50, 57, 255))
            px(im, x + i, (y + 1) % 64, (46, 64, 69, 255))
    # Partial hood and face on the top-left head region of the vex UV layout.
    for x in range(8, 16):
        for y in range(8, 16):
            if 8 < x < 15 and 8 < y < 15:
                px(im, x, y, (28, 40, 46, 255))
    for x, y in [(9, 11), (10, 11), (13, 11), (14, 11), (9, 12), (14, 12)]:
        px(im, x, y, (150, 255, 240, 255))
    px(im, 11, 14, (18, 26, 31, 255))
    px(im, 12, 14, (18, 26, 31, 255))
    return im


def make_ghost_wisp_texture() -> Image.Image:
    rng = random.Random(0x1A5E)
    base = (28, 96, 94, 255)
    im = img(64, 64, base)
    for y in range(64):
        for x in range(64):
            d = math.sqrt((x - 32) ** 2 + (y - 32) ** 2)
            glow = max(0.0, 1.0 - d / 52.0)
            c = (24 + int(28 * glow), 86 + int(130 * glow), 82 + int(120 * glow), 255)
            px(im, x, y, c)
    for _ in range(420):
        px(im, rng.randrange(64), rng.randrange(64),
           rnd_color(rng, [(40, 150, 140, 255), (80, 230, 210, 255), (18, 70, 72, 255), (210, 255, 240, 255)]))
    for x, y in [(31, 30), (32, 30), (31, 31), (32, 31)]:
        px(im, x, y, (235, 255, 248, 255))
    return im


def make_soul_lamp() -> Image.Image:
    im = img(16, 16, (10, 7, 13, 255))
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = math.sqrt(dx * dx + dy * dy)
            glow = max(0.0, 1.0 - d / 7.0)
            if d < 2.0:
                c = CYAN
            elif d < 4.2:
                c = (40 + int(70 * glow), 90 + int(120 * glow), 86 + int(100 * glow), 255)
            else:
                c = (10 + int(16 * glow), 30 + int(30 * glow), 27 + int(26 * glow), 255)
            px(im, x, y, c)
    # Frame and talisman.
    for i in range(2, 14):
        px(im, i, 2, GOLD)
        px(im, i, 13, GOLD)
        px(im, 2, i, (90, 66, 38, 255))
        px(im, 13, i, (90, 66, 38, 255))
    for x, y in [(6, 4), (7, 4), (8, 4), (9, 4), (6, 5), (9, 5)]:
        px(im, x, y, (20, 6, 8, 255))
    for y in range(9, 13):
        px(im, 7, y, (212, 190, 140, 255))
        px(im, 8, y, (212, 190, 140, 255))
    px(im, 7, 10, RED2)
    return im


def make_bianhua() -> Image.Image:
    im = img(16, 16, CLEAR)
    # Stem and leaves.
    for y in range(9, 16):
        px(im, 8, y, (20, 70, 32, 255))
    px(im, 7, 12, (30, 100, 42, 255))
    px(im, 6, 11, (30, 100, 42, 255))
    px(im, 9, 13, (30, 100, 42, 255))
    px(im, 10, 12, (30, 100, 42, 255))
    # Six petals.
    petals = [(0, -1), (1, -1), (-1, -1), (1, 0), (-1, 0), (0, 1)]
    for dx, dy in petals:
        for step in range(1, 4):
            x = 8 + dx * step
            y = 7 + dy * step
            px(im, x, y, (170 + step * 12, 18, 36, 255))
        x = 8 + dx * 3
        y = 7 + dy * 3
        px(im, x, y, RED2)
    px(im, 8, 7, (255, 214, 90, 255))
    px(im, 7, 7, (255, 180, 60, 255))
    px(im, 9, 7, (255, 180, 60, 255))
    return im


def make_guiding_lantern() -> Image.Image:
    im = img(16, 16, CLEAR)
    # Handle.
    for x in range(5, 11):
        px(im, x, 1, GOLD)
    px(im, 4, 2, GOLD)
    px(im, 11, 2, GOLD)
    # Cap and body.
    for x in range(3, 13):
        px(im, x, 3, (70, 48, 28, 255))
        px(im, x, 4, (30, 20, 14, 255))
        px(im, x, 12, (30, 20, 14, 255))
    for y in range(5, 12):
        px(im, 3, y, (70, 48, 28, 255))
        px(im, 12, y, (70, 48, 28, 255))
        for x in range(4, 12):
            px(im, x, y, (12, 18, 20, 255))
    # Cyan ghost flame.
    for x, y in [(7, 9), (8, 9), (7, 8), (8, 8), (6, 10), (9, 10), (7, 7), (8, 7)]:
        px(im, x, y, CYAN)
    px(im, 7, 6, (180, 255, 245, 255))
    px(im, 8, 6, (180, 255, 245, 255))
    # Talisman.
    for y in range(6, 11):
        px(im, 6, y, (210, 190, 140, 255))
    px(im, 6, 7, RED2)
    px(im, 6, 9, RED2)
    return im


def make_crimson_sign() -> Image.Image:
    im = img(32, 32, (38, 6, 12, 255))
    # Lacquer grain.
    for y in range(32):
        for x in range(32):
            if (x * 7 + y * 13) % 11 == 0:
                px(im, x, y, (54, 9, 16, 255))
    for i in range(2, 30):
        px(im, i, 1, GOLD)
        px(im, i, 30, GOLD)
        px(im, 1, i, (100, 70, 32, 255))
        px(im, 30, i, (100, 70, 32, 255))
    # Paper talisman.
    for y in range(9, 23):
        for x in range(12, 20):
            px(im, x, y, (208, 190, 146, 255))
    for x, y in [(15, 11), (16, 11), (15, 12), (16, 12), (14, 13), (17, 13), (14, 14), (17, 14),
                 (15, 15), (16, 15), (15, 16), (16, 16), (14, 17), (17, 17), (15, 19), (16, 19),
                 (15, 20), (16, 20)]:
        px(im, x, y, (28, 8, 10, 255))
    for x, y in [(13, 20), (18, 20), (13, 21), (18, 21)]:
        px(im, x, y, RED2)
    return im


def make_soul_lantern() -> Image.Image:
    im = img(16, 48, CLEAR)
    for frame in range(3):
        base_y = frame * 16
        # Caps.
        for x in range(4, 12):
            px(im, x, base_y + 2, (56, 38, 22, 255))
            px(im, x, base_y + 13, (56, 38, 22, 255))
        for y in range(3, 13):
            px(im, 4, base_y + y, (70, 48, 28, 255))
            px(im, 11, base_y + y, (70, 48, 28, 255))
        # Glass and flame.
        for y in range(4, 12):
            for x in range(5, 11):
                px(im, x, base_y + y, (10, 26, 28, 255))
        flicker = 1 if frame == 1 else 0
        for x, y in [(7, 9), (8, 9), (7, 8), (8, 8), (6, 10), (9, 10), (7, 7 - flicker), (8, 7 - flicker)]:
            px(im, x, base_y + y, CYAN)
        px(im, 7, base_y + 6 - flicker, (200, 255, 248, 255))
        px(im, 8, base_y + 6 - flicker, (200, 255, 248, 255))
    return im


def make_soul_torch() -> Image.Image:
    im = img(16, 16, CLEAR)
    for y in range(8, 16):
        px(im, 7, y, (48, 32, 20, 255))
        px(im, 8, y, (66, 44, 26, 255))
    for x, y in [(6, 5), (7, 4), (8, 4), (9, 5), (6, 6), (9, 6), (7, 7), (8, 7), (7, 6), (8, 6)]:
        px(im, x, y, CYAN if (x + y) % 3 else (180, 255, 245, 255))
    px(im, 7, 3, (200, 255, 248, 255))
    px(im, 8, 3, (200, 255, 248, 255))
    return im


def flame_frame(w: int, h: int, phase: float, salt: int) -> Image.Image:
    im = img(w, h, CLEAR)
    rng = random.Random(salt)
    base = h - 1
    for x in range(w):
        nx = (x - w / 2) / (w / 2)
        height = (1.0 - abs(nx) ** 1.7) * (h * 0.78)
        height += math.sin(phase + x * 0.6) * 1.6
        top = int(base - max(0, height))
        for y in range(top, h):
            t = (y - top) / max(1, h - 1 - top)
            if abs(nx) > 0.85 and rng.random() < 0.55:
                continue
            if t > 0.58:
                c = (200, 255, 246, 235)
            elif t > 0.28:
                c = (80, 232, 210, 220)
            else:
                c = (24, 122, 116, 170)
            px(im, x, y, c)
    return im


def make_soul_fire(salt: int) -> Image.Image:
    frames = 32
    im = img(16, frames * 16, CLEAR)
    for i in range(frames):
        frame = flame_frame(16, 16, i * 0.7, salt + i)
        im.paste(frame, (0, i * 16))
    return im


def make_gui_sign() -> Image.Image:
    im = img(24, 26, (36, 7, 12, 255))
    for x in range(24):
        px(im, x, 0, GOLD)
        px(im, x, 25, GOLD)
    for y in range(26):
        px(im, 0, y, GOLD)
        px(im, 23, y, GOLD)
    return im


def make_pack_icon() -> Image.Image:
    im = img(128, 128, (7, 3, 10, 255))
    d = ImageDraw.Draw(im)
    # Red moon.
    d.ellipse((78, 12, 118, 52), fill=(120, 14, 28, 255), outline=(190, 40, 52, 255), width=2)
    # Gate silhouette.
    d.rectangle((26, 58, 102, 64), fill=(26, 14, 30, 255))
    d.rectangle((34, 64, 44, 112), fill=(20, 10, 24, 255))
    d.rectangle((84, 64, 94, 112), fill=(20, 10, 24, 255))
    d.rectangle((30, 52, 98, 58), fill=(44, 22, 46, 255))
    d.rectangle((38, 40, 90, 52), fill=(30, 15, 34, 255))
    d.rectangle((34, 34, 94, 40), fill=(58, 28, 58, 255))
    # Ghost fire.
    d.ellipse((59, 82, 69, 96), fill=(40, 180, 168, 255))
    d.ellipse((62, 80, 66, 90), fill=(150, 255, 240, 255))
    # Text.
    font_big = None
    font_small = None
    for path in (r"C:\Windows\Fonts\msyh.ttc", r"C:\Windows\Fonts\simhei.ttf", r"C:\Windows\Fonts\simsun.ttc"):
        if Path(path).exists():
            try:
                font_big = ImageFont.truetype(path, 30)
                font_small = ImageFont.truetype(path, 10)
                break
            except OSError:
                pass
    if font_big:
        d.text((64, 14), "冥界", font=font_big, fill=(220, 206, 184, 255), anchor="mm")
        d.text((64, 121), "DEEP UNDERWORLD", font=font_small, fill=(150, 40, 50, 255), anchor="mm")
    return im


# ---------------------------------------------------------------------------
# JSON assets
# ---------------------------------------------------------------------------

def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_models() -> None:
    simple = [
        "soul_block",
        "ashen_soil",
        "soul_gravel",
        "bone_soil",
        "blood_moss",
        "soul_stone",
        "yin_stone",
        "yin_stone_bricks",
        "chiseled_yin_stone",
        "soul_lamp",
    ]

    # Minecraft 26.3 uses the item-model-definition system: an item must have
    # assets/<namespace>/items/<id>.json, otherwise it has no inventory/hand model.
    item_definitions = {
        "soul_block": "wcnm:block/soul_block",
        "ashen_soil": "wcnm:block/ashen_soil",
        "soul_gravel": "wcnm:block/soul_gravel",
        "bone_soil": "wcnm:block/bone_soil",
        "blood_moss": "wcnm:block/blood_moss",
        "soul_stone": "wcnm:block/soul_stone",
        "yin_stone": "wcnm:block/yin_stone",
        "yin_stone_bricks": "wcnm:block/yin_stone_bricks",
        "chiseled_yin_stone": "wcnm:block/chiseled_yin_stone",
        "soul_lamp": "wcnm:block/soul_lamp",
        "bianhua": "wcnm:item/bianhua",
        "soul_grass": "wcnm:item/soul_grass",
        "guiding_lantern": "wcnm:item/guiding_lantern",
    }

    for name, model in item_definitions.items():
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {
                "type": "minecraft:model",
                "model": model,
            }
        })

    for name in simple:
        write_json(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"wcnm:block/{name}"}}})
        write_json(ASSETS / "models" / "block" / f"{name}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"wcnm:block/{name}"},
        })
        write_json(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"wcnm:block/{name}"})

    write_json(ASSETS / "blockstates" / "bianhua.json", {"variants": {"": {"model": "wcnm:block/bianhua"}}})
    write_json(ASSETS / "models" / "block" / "bianhua.json", {
        "parent": "minecraft:block/cross",
        "textures": {"cross": "wcnm:block/bianhua"},
    })
    write_json(ASSETS / "models" / "item" / "bianhua.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "wcnm:block/bianhua"},
    })
    write_json(ASSETS / "blockstates" / "soul_grass.json", {"variants": {"": {"model": "wcnm:block/soul_grass"}}})
    write_json(ASSETS / "models" / "block" / "soul_grass.json", {
        "parent": "minecraft:block/cross",
        "textures": {"cross": "wcnm:block/soul_grass"},
    })
    write_json(ASSETS / "models" / "item" / "soul_grass.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "wcnm:block/soul_grass"},
    })
    write_json(ASSETS / "blockstates" / "guiding_lantern.json", {
        "variants": {
            "hanging=false": {"model": "wcnm:block/guiding_lantern"},
            "hanging=true": {"model": "wcnm:block/guiding_lantern_hanging"},
        }
    })
    write_json(ASSETS / "models" / "block" / "guiding_lantern.json", {
        "parent": "minecraft:block/template_lantern",
        "textures": {"lantern": "wcnm:block/guiding_lantern"},
    })
    write_json(ASSETS / "models" / "block" / "guiding_lantern_hanging.json", {
        "parent": "minecraft:block/template_hanging_lantern",
        "textures": {"lantern": "wcnm:block/guiding_lantern"},
    })
    write_json(ASSETS / "models" / "item" / "guiding_lantern.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "wcnm:item/guiding_lantern"},
    })

    write_json(ASSETS / "lang" / "zh_cn.json", {
        "block.wcnm.soul_block": "灵魂块",
        "block.wcnm.ashen_soil": "冥土",
        "block.wcnm.soul_gravel": "魂砂砾",
        "block.wcnm.bone_soil": "骨壤",
        "block.wcnm.blood_moss": "血苔",
        "block.wcnm.soul_stone": "魂石",
        "block.wcnm.soul_grass": "阴草",
        "block.wcnm.yin_stone": "冥石",
        "block.wcnm.yin_stone_bricks": "冥石砖",
        "block.wcnm.chiseled_yin_stone": "雕纹冥石",
        "block.wcnm.soul_lamp": "幽冥灯",
        "block.wcnm.guiding_lantern": "引魂灯",
        "block.wcnm.bianhua": "彼岸花",
        "entity.wcnm.wandering_soul": "游魂",
        "entity.wcnm.ghost_wisp": "鬼火",
        "entity.wcnm.ghost_soldier": "阴兵",
        "entity.wcnm.underworld_hound": "冥犬",
        "item.wcnm.guiding_lantern": "引魂灯",
        "item.wcnm.soul_block": "灵魂块",
        "item.wcnm.ashen_soil": "冥土",
        "item.wcnm.soul_gravel": "魂砂砾",
        "item.wcnm.bone_soil": "骨壤",
        "item.wcnm.blood_moss": "血苔",
        "item.wcnm.soul_stone": "魂石",
        "item.wcnm.soul_grass": "阴草",
        "item.wcnm.yin_stone": "冥石",
        "item.wcnm.yin_stone_bricks": "冥石砖",
        "item.wcnm.chiseled_yin_stone": "雕纹冥石",
        "item.wcnm.soul_lamp": "幽冥灯",
        "item.wcnm.bianhua": "彼岸花",
    })
    write_json(ASSETS / "lang" / "en_us.json", {
        "block.wcnm.soul_block": "Soul Block",
        "block.wcnm.ashen_soil": "Ashen Soil",
        "block.wcnm.soul_gravel": "Soul Gravel",
        "block.wcnm.bone_soil": "Bone Soil",
        "block.wcnm.blood_moss": "Blood Moss",
        "block.wcnm.soul_stone": "Soul Stone",
        "block.wcnm.soul_grass": "Soul Grass",
        "block.wcnm.yin_stone": "Yin Stone",
        "block.wcnm.yin_stone_bricks": "Yin Stone Bricks",
        "block.wcnm.chiseled_yin_stone": "Chiseled Yin Stone",
        "block.wcnm.soul_lamp": "Soul Lamp",
        "block.wcnm.guiding_lantern": "Guiding Lantern",
        "block.wcnm.bianhua": "Bianhua",
        "entity.wcnm.wandering_soul": "Wandering Soul",
        "entity.wcnm.ghost_wisp": "Ghost Wisp",
        "entity.wcnm.ghost_soldier": "Ghost Soldier",
        "entity.wcnm.underworld_hound": "Underworld Hound",
        "item.wcnm.guiding_lantern": "Guiding Lantern",
        "item.wcnm.soul_block": "Soul Block",
        "item.wcnm.ashen_soil": "Ashen Soil",
        "item.wcnm.soul_gravel": "Soul Gravel",
        "item.wcnm.bone_soil": "Bone Soil",
        "item.wcnm.blood_moss": "Blood Moss",
        "item.wcnm.soul_stone": "Soul Stone",
        "item.wcnm.soul_grass": "Soul Grass",
        "item.wcnm.yin_stone": "Yin Stone",
        "item.wcnm.yin_stone_bricks": "Yin Stone Bricks",
        "item.wcnm.chiseled_yin_stone": "Chiseled Yin Stone",
        "item.wcnm.soul_lamp": "Soul Lamp",
        "item.wcnm.bianhua": "Bianhua",
    })


def write_pack_meta() -> None:
    # 26.3 uses the new pack format fields.  "supported_formats" is deprecated from
    # format 65 onwards and makes the pack incompatible, so only min/max_format are used.
    write_json(PACK / "pack.mcmeta", {
        "pack": {
            "description": "Deep UnderWorld · 国风冥界材质",
            "min_format": 97,
            "max_format": 97,
        }
    })


def save(im: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    im.save(path, "PNG")


def main() -> None:
    write_models()

    block_tex = ASSETS / "textures" / "block"
    item_tex = ASSETS / "textures" / "item"
    entity_tex = ASSETS / "textures" / "entity"

    # Only custom namespace assets are generated.  Vanilla textures are never modified.
    save(make_soul_block(), block_tex / "soul_block.png")
    save(make_ashen_soil(), block_tex / "ashen_soil.png")
    save(make_soul_gravel(), block_tex / "soul_gravel.png")
    save(make_bone_soil(), block_tex / "bone_soil.png")
    save(make_blood_moss(), block_tex / "blood_moss.png")
    save(make_soul_stone(), block_tex / "soul_stone.png")
    save(make_soul_grass(), block_tex / "soul_grass.png")
    save(make_yin_stone(), block_tex / "yin_stone.png")
    save(make_bricks(), block_tex / "yin_stone_bricks.png")
    save(make_chiseled(), block_tex / "chiseled_yin_stone.png")
    save(make_soul_lamp(), block_tex / "soul_lamp.png")
    save(make_bianhua(), block_tex / "bianhua.png")
    save(make_guiding_lantern(), item_tex / "guiding_lantern.png")
    save(make_guiding_lantern(), block_tex / "guiding_lantern.png")
    save(make_wandering_soul_texture(), entity_tex / "wandering_soul.png")
    save(make_ghost_wisp_texture(), entity_tex / "ghost_wisp.png")
    save(make_ghost_soldier_texture(), entity_tex / "ghost_soldier.png")
    save(make_underworld_hound_texture(), entity_tex / "underworld_hound.png")

    print(f"assets  -> {ASSETS}")


if __name__ == "__main__":
    main()

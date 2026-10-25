package com.wcnm.world;

import com.wcnm.registry.WcnmBlocks;
import java.util.SplittableRandom;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Procedural construction of the fifty underworld buildings.  Every building is generated from
 * its cell coordinates alone, so chunks on opposite sides of a building always agree.
 */
public final class UnderworldBuildings {
	private static final BlockState MAIN = WcnmBlocks.YIN_STONE.defaultBlockState();
	private static final BlockState BRICK = WcnmBlocks.YIN_STONE_BRICKS.defaultBlockState();
	private static final BlockState TRIM = WcnmBlocks.CHISELED_YIN_STONE.defaultBlockState();
	private static final BlockState ACCENT = Blocks.CRYING_OBSIDIAN.defaultBlockState();
	private static final BlockState LAMP = WcnmBlocks.SOUL_LAMP.defaultBlockState();
	private static final BlockState LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();
	private static final BlockState TORCH = Blocks.SOUL_TORCH.defaultBlockState();
	private static final BlockState CHAIN = Blocks.IRON_CHAIN.defaultBlockState();
	private static final BlockState WATER = Blocks.WATER.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState BIANHUA = WcnmBlocks.BIANHUA.defaultBlockState();

	private UnderworldBuildings() {
	}

	public static void placeAt(ChunkEditor editor, int ox, int oz, int number, int baseY, long seed) {
		BuildingCatalog.Building building = BuildingCatalog.get(number);
		int y = baseY + 1;
		SplittableRandom random = new SplittableRandom(mix(seed, ox, oz, building.number()));

		prepareFootprint(editor, ox, oz, building, baseY);

		switch (building.archetype()) {
			case GATE -> gate(editor, ox, y, oz, building);
			case HALL -> hall(editor, ox, y, oz, building, random);
			case PAVILION -> pavilion(editor, ox, y, oz, building, random);
			case TOWER -> tower(editor, ox, y, oz, building, random);
			case WELL -> well(editor, ox, y, oz, building);
			case BRIDGE -> bridge(editor, ox, y, oz, building);
			case SHRINE -> shrine(editor, ox, y, oz, building, random);
			case PRISON -> prison(editor, ox, y, oz, building, random);
			case MARKET -> market(editor, ox, y, oz, building, random);
			case POND -> pond(editor, ox, y, oz, building, random);
		}

		placeSigns(editor, ox, y, oz, building, baseY);
		decorate(editor, ox, y, oz, building, random);
	}

	// ------------------------------------------------------------------
	// Archetypes
	// ------------------------------------------------------------------

	private static void gate(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b) {
		int half = b.width() / 2;

		for (int side : new int[]{-half, half}) {
			box(e, ox + side - 1, y, oz - 1, ox + side + 1, y + b.height() - 1, oz + 1, MAIN);

			for (int by = y; by < y + b.height(); by += 3) {
				box(e, ox + side - 1, by, oz - 1, ox + side + 1, by, oz + 1, ACCENT);
			}
		}

		box(e, ox - half - 1, y + b.height(), oz - 1, ox + half + 1, y + b.height(), oz + 1, TRIM);
		e.set(ox, y + b.height() - 1, oz + 1, LAMP);
		e.set(ox, y + b.height(), oz + 1, ACCENT);
		roof(e, ox, y + b.height() + 1, oz, b.width() + 2, b.depth() + 2, 3);
	}

	private static void hall(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int halfW = b.width() / 2;
		int halfD = b.depth() / 2;
		shell(e, ox - halfW, y, oz - halfD, ox + halfW, y + b.height() - 1, oz + halfD, MAIN);

		// Door.
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				e.set(ox + dx, y + dy, oz + halfD, AIR);
			}
		}

		// Windows.
		for (int dx = -halfW + 2; dx <= halfW - 2; dx += 3) {
			e.set(ox + dx, y + 2, oz + halfD, ACCENT);
			e.set(ox + dx, y + 2, oz - halfD, ACCENT);
		}

		for (int dz = -halfD + 2; dz <= halfD - 2; dz += 3) {
			e.set(ox - halfW, y + 2, oz + dz, ACCENT);
			e.set(ox + halfW, y + 2, oz + dz, ACCENT);
		}

		pillar(e, ox - halfW, y, oz - halfD, b.height(), TRIM);
		pillar(e, ox + halfW, y, oz - halfD, b.height(), TRIM);
		pillar(e, ox - halfW, y, oz + halfD, b.height(), TRIM);
		pillar(e, ox + halfW, y, oz + halfD, b.height(), TRIM);

		e.set(ox, y, oz, LAMP);
		e.set(ox - 3, y, oz - 2, LANTERN);
		e.set(ox + 3, y, oz - 2, LANTERN);
		chain(e, ox - 2, y + b.height() - 1, oz, 3);
		chain(e, ox + 2, y + b.height() - 1, oz, 3);

		// Altar.
		box(e, ox - 1, y, oz - halfD + 2, ox + 1, y, oz - halfD + 2, TRIM);
		e.set(ox, y + 1, oz - halfD + 2, ACCENT);

		roof(e, ox, y + b.height(), oz, b.width() + 2, b.depth() + 2, 3);
	}

	private static void pavilion(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int halfW = b.width() / 2;
		int halfD = b.depth() / 2;

		pillar(e, ox - halfW, y, oz - halfD, b.height(), TRIM);
		pillar(e, ox + halfW, y, oz - halfD, b.height(), TRIM);
		pillar(e, ox - halfW, y, oz + halfD, b.height(), TRIM);
		pillar(e, ox + halfW, y, oz + halfD, b.height(), TRIM);

		// Low railings, open at the front.
		for (int dx = -halfW + 1; dx <= halfW - 1; dx++) {
			e.set(ox + dx, y, oz - halfD, ACCENT);
			e.set(ox + dx, y, oz + halfD, ACCENT);
		}

		for (int dz = -halfD + 1; dz <= halfD - 1; dz++) {
			e.set(ox - halfW, y, oz + dz, ACCENT);
			e.set(ox + halfW, y, oz + dz, ACCENT);
		}

		box(e, ox - 1, y, oz - 1, ox + 1, y, oz + 1, MAIN);
		e.set(ox, y + 1, oz, LAMP);

		e.set(ox - halfW, y + b.height() - 1, oz - halfD, LANTERN);
		e.set(ox + halfW, y + b.height() - 1, oz - halfD, LANTERN);
		e.set(ox - halfW, y + b.height() - 1, oz + halfD, LANTERN);
		e.set(ox + halfW, y + b.height() - 1, oz + halfD, LANTERN);

		roof(e, ox, y + b.height(), oz, b.width() + 2, b.depth() + 2, 3);
	}

	private static void tower(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int sections = 3;
		int sectionHeight = Math.max(3, b.height() / sections);

		for (int section = 0; section < sections; section++) {
			int width = ((b.width() - section * 2) / 2) * 2 + 1;
			int depth = ((b.depth() - section * 2) / 2) * 2 + 1;
			int sy = y + section * sectionHeight;

			if (width < 5 || depth < 5) {
				break;
			}

			shell(e, ox - width / 2, sy, oz - depth / 2, ox + width / 2, sy + sectionHeight - 1, oz + depth / 2, MAIN);

			for (int dx = -width / 2 + 1; dx <= width / 2 - 1; dx += 2) {
				e.set(ox + dx, sy + 1, oz + depth / 2, ACCENT);
				e.set(ox + dx, sy + 1, oz - depth / 2, ACCENT);
			}

			for (int dz = -depth / 2 + 1; dz <= depth / 2 - 1; dz += 2) {
				e.set(ox - width / 2, sy + 1, oz + dz, ACCENT);
				e.set(ox + width / 2, sy + 1, oz + dz, ACCENT);
			}

			e.set(ox, sy, oz, LAMP);
			e.set(ox - width / 2, sy, oz - depth / 2, LANTERN);
			e.set(ox + width / 2, sy, oz - depth / 2, LANTERN);
			box(e, ox - width / 2, sy + sectionHeight - 1, oz - depth / 2,
				ox + width / 2, sy + sectionHeight - 1, oz + depth / 2, TRIM);
		}

		roof(e, ox, y + b.height(), oz, 7, 7, 4);
	}

	private static void well(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b) {
		// The well shaft sits one block below the platform.
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				e.set(ox + dx, y - 2, oz + dz, WATER);
				e.set(ox + dx, y - 1, oz + dz, AIR);
				e.set(ox + dx, y, oz + dz, AIR);
			}
		}

		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				if (Math.abs(dx) == 3 || Math.abs(dz) == 3) {
					e.set(ox + dx, y - 1, oz + dz, BRICK);
				}
			}
		}

		pillar(e, ox - 3, y, oz - 3, b.height(), TRIM);
		pillar(e, ox + 3, y, oz - 3, b.height(), TRIM);
		pillar(e, ox - 3, y, oz + 3, b.height(), TRIM);
		pillar(e, ox + 3, y, oz + 3, b.height(), TRIM);

		e.set(ox, y + b.height(), oz, LAMP);
		roof(e, ox, y + b.height(), oz, 9, 9, 2);
	}

	private static void bridge(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b) {
		int halfW = b.width() / 2;

		box(e, ox - halfW, y - 1, oz - 2, ox + halfW, y - 1, oz + 2, BRICK);

		for (int dx = -halfW; dx <= halfW; dx++) {
			e.set(ox + dx, y, oz - 2, (dx & 1) == 0 ? MAIN : ACCENT);
			e.set(ox + dx, y, oz + 2, (dx & 1) == 0 ? MAIN : ACCENT);
		}

		for (int dx = -halfW; dx <= halfW; dx += 5) {
			e.set(ox + dx, y + 1, oz - 2, LANTERN);
			e.set(ox + dx, y + 1, oz + 2, LANTERN);
		}

		// Ceremonial arch at both ends.
		for (int end : new int[]{-halfW, halfW}) {
			e.set(ox + end, y, oz - 2, TRIM);
			e.set(ox + end, y + 1, oz - 2, TRIM);
			e.set(ox + end, y + 2, oz - 2, TRIM);
			e.set(ox + end, y + 3, oz - 2, LAMP);
			e.set(ox + end, y, oz + 2, TRIM);
			e.set(ox + end, y + 1, oz + 2, TRIM);
			e.set(ox + end, y + 2, oz + 2, TRIM);
			e.set(ox + end, y + 3, oz + 2, LAMP);
		}
	}

	private static void shrine(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int halfW = b.width() / 2;
		int halfD = b.depth() / 2;

		// Three walls, open front.
		for (int dx = -halfW; dx <= halfW; dx++) {
			for (int dy = 0; dy < b.height(); dy++) {
				e.set(ox + dx, y + dy, oz - halfD, MAIN);
			}
		}

		for (int dz = -halfD; dz <= halfD; dz++) {
			for (int dy = 0; dy < b.height(); dy++) {
				e.set(ox - halfW, y + dy, oz + dz, MAIN);
				e.set(ox + halfW, y + dy, oz + dz, MAIN);
			}
		}

		for (int dx = -halfW; dx <= halfW; dx += 2) {
			e.set(ox + dx, y + 1, oz - halfD, ACCENT);
			e.set(ox + dx, y + 2, oz - halfD, ACCENT);
		}

		// Altar and statue.
		box(e, ox - 1, y, oz - 1, ox + 1, y, oz - 1, TRIM);
		e.set(ox, y + 1, oz - 1, LAMP);
		e.set(ox, y + 2, oz - 1, ACCENT);
		e.set(ox, y + 3, oz - 1, LAMP);

		e.set(ox - halfW + 1, y, oz + halfD, LANTERN);
		e.set(ox + halfW - 1, y, oz + halfD, LANTERN);

		roof(e, ox, y + b.height(), oz, b.width() + 2, b.depth() + 2, 3);
	}

	private static void prison(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int halfW = b.width() / 2;
		int halfD = b.depth() / 2;
		shell(e, ox - halfW, y, oz - halfD, ox + halfW, y + b.height() - 1, oz + halfD, MAIN);

		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				e.set(ox + dx, y + dy, oz + halfD, AIR);
			}
		}

		// Cells along both long walls, separated by chains.
		for (int side : new int[]{-1, 1}) {
			int wallX = ox + side * (halfW - 3);

			for (int dz = -halfD + 2; dz <= halfD - 2; dz += 4) {
				box(e, wallX - 2, y, oz + dz, wallX + 2, y + 2, oz + dz, CHAIN);
				e.set(ox + side * (halfW - 1), y, oz + dz, LANTERN);
			}

			for (int dz = -halfD + 1; dz <= halfD - 1; dz += 4) {
				e.set(ox + side * (halfW - 4), y, oz + dz, ACCENT);
			}
		}

		e.set(ox, y, oz, LAMP);
		roof(e, ox, y + b.height(), oz, b.width() + 2, b.depth() + 2, 3);
	}

	private static void market(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int halfW = b.width() / 2;
		int halfD = b.depth() / 2;

		for (int side : new int[]{-1, 1}) {
			for (int dz = -halfD + 2; dz <= halfD - 2; dz += 4) {
				int sx = ox + side * (halfW - 2);
				box(e, sx - 1, y, oz + dz - 1, sx + 1, y, oz + dz + 1, BRICK);
				e.set(sx, y + 1, oz + dz, LAMP);
				e.set(sx - 1, y + 2, oz + dz, ACCENT);
				e.set(sx + 1, y + 2, oz + dz, ACCENT);
				e.set(sx, y + 2, oz + dz - 1, ACCENT);
			}
		}

		for (int dz = -halfD; dz <= halfD; dz += 3) {
			e.set(ox, y, oz + dz, BRICK);
		}

		roof(e, ox, y + b.height(), oz, b.width() + 2, b.depth() + 2, 2);
	}

	private static void pond(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int half = b.width() / 2;

		for (int dx = -half; dx <= half; dx++) {
			for (int dz = -half; dz <= half; dz++) {
				boolean rim = Math.abs(dx) == half || Math.abs(dz) == half;
				e.set(ox + dx, y - 1, oz + dz, rim ? BRICK : (Math.abs(dx) <= 3 && Math.abs(dz) <= 3 ? WATER : AIR));
			}
		}

		for (int dx = -half + 1; dx <= half - 1; dx += 2) {
			e.set(ox + dx, y, oz - half, LANTERN);
			e.set(ox + dx, y, oz + half, LANTERN);
		}

		e.set(ox, y - 1, oz, TRIM);
		e.set(ox, y, oz, LAMP);
		e.set(ox, y + 1, oz, ACCENT);
	}

	// ------------------------------------------------------------------
	// Shared pieces
	// ------------------------------------------------------------------

	private static void prepareFootprint(ChunkEditor e, int ox, int oz, BuildingCatalog.Building b, int baseY) {
		int halfW = b.width() / 2 + 1;
		int halfD = b.depth() / 2 + 1;

		for (int dx = -halfW; dx <= halfW; dx++) {
			for (int dz = -halfD; dz <= halfD; dz++) {
				boolean edge = Math.abs(dx) == halfW || Math.abs(dz) == halfD;
				e.set(ox + dx, baseY, oz + dz, edge ? TRIM : BRICK);
			}
		}

		// A ring of crying obsidian marks the reserved building plot.
		for (int dx = -halfW - 1; dx <= halfW + 1; dx++) {
			e.set(ox + dx, baseY, oz - halfD - 1, ACCENT);
			e.set(ox + dx, baseY, oz + halfD + 1, ACCENT);
		}

		for (int dz = -halfD - 1; dz <= halfD + 1; dz++) {
			e.set(ox - halfW - 1, baseY, oz + dz, ACCENT);
			e.set(ox + halfW + 1, baseY, oz + dz, ACCENT);
		}
	}

	private static void placeSigns(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, int baseY) {
		var lines = ChunkEditor.signLines(b.number(), b.name());
		int signZ = oz + b.depth() / 2 + 3;

		// Standing sign on a soul-lamp pedestal: the lamp itself lights the plaque.
		e.set(ox, baseY, signZ, LAMP);
		e.set(ox, y, signZ, AIR);
		e.setSign(ox, y, signZ, Direction.SOUTH, false, 8, lines);
		e.set(ox - 2, baseY, signZ, LAMP);
		e.set(ox + 2, baseY, signZ, LAMP);
		e.set(ox - 2, y, signZ, TORCH);
		e.set(ox + 2, y, signZ, TORCH);

		if (isWalled(b)) {
			int wallZ = oz + b.depth() / 2;
			int signY = y + 3;

			// Guarantee the supporting wall block exists, then attach a second glowing sign
			// directly to the structure surface above the door.
			e.set(ox, signY, wallZ, MAIN);
			e.setSign(ox, signY, wallZ + 1, Direction.SOUTH, true, 0, lines);
		}
	}

	private static boolean isWalled(BuildingCatalog.Building b) {
		return switch (b.archetype()) {
			case HALL, TOWER, PRISON -> true;
			default -> false;
		};
	}

	private static void decorate(ChunkEditor e, int ox, int y, int oz, BuildingCatalog.Building b, SplittableRandom random) {
		int halfW = b.width() / 2 + 3;
		int halfD = b.depth() / 2 + 3;

		for (int attempt = 0; attempt < 18; attempt++) {
			int dx = random.nextInt(halfW * 2 + 1) - halfW;
			int dz = random.nextInt(halfD * 2 + 1) - halfD;
			int x = ox + dx;
			int z = oz + dz;
			e.setIfAir(x, y, z, BIANHUA);
		}

		for (int dx = -halfW; dx <= halfW; dx += 4) {
			e.set(dx + ox, y, oz - halfD, TORCH);
		}
	}

	private static void roof(ChunkEditor e, int ox, int y, int oz, int width, int depth, int layers) {
		int w = width;
		int d = depth;

		for (int layer = 0; layer < layers; layer++) {
			if (w < 1 || d < 1) {
				break;
			}

			box(e, ox - w / 2, y + layer, oz - d / 2, ox + w / 2, y + layer, oz + d / 2,
				layer == 0 ? TRIM : MAIN);

			e.set(ox - w / 2, y + layer, oz - d / 2, ACCENT);
			e.set(ox + w / 2, y + layer, oz - d / 2, ACCENT);
			e.set(ox - w / 2, y + layer, oz + d / 2, ACCENT);
			e.set(ox + w / 2, y + layer, oz + d / 2, ACCENT);
			w -= 2;
			d -= 2;
		}

		e.set(ox, y + layers, oz, LAMP);
		e.set(ox, y + layers + 1, oz, ACCENT);
	}

	private static void shell(ChunkEditor e, int x0, int y0, int z0, int x1, int y1, int z1, BlockState wall) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					if (x == x0 || x == x1 || z == z0 || z == z1) {
						e.set(x, y, z, wall);
					}
				}
			}
		}
	}

	private static void box(ChunkEditor e, int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				for (int z = z0; z <= z1; z++) {
					e.set(x, y, z, state);
				}
			}
		}
	}

	private static void pillar(ChunkEditor e, int x, int y, int z, int height, BlockState state) {
		box(e, x, y, z, x, y + height - 1, z, state);
	}

	private static void chain(ChunkEditor e, int x, int y, int z, int length) {
		for (int i = 0; i < length; i++) {
			e.set(x, y - i, z, CHAIN);
		}
	}

	private static long mix(long seed, int x, int z, int salt) {
		long h = seed;
		h ^= (long) x * 0x9E3779B97F4A7C15L;
		h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
		h ^= (long) salt * 0x94D049BB133111EBL;
		h ^= h >>> 30;
		h *= 0xBF58476D1CE4E5B9L;
		h ^= h >>> 27;
		h *= 0x94D049BB133111EBL;
		h ^= h >>> 31;
		return h;
	}
}

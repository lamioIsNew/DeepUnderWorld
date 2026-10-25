package com.wcnm.world;

import com.wcnm.registry.WcnmBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Post-processing on top of the vanilla overworld noise terrain:
 *
 * <ul>
 *   <li>soul-block surface (the vanilla material rule handles the top layer);</li>
 *   <li>sparse lakes, one candidate per 5000x5000 block cell;</li>
 *   <li>one meandering river inside |x|,|z| &lt;= 1000, smoothly fading out at the border;</li>
 *   <li>deterministic building sites on a 96-block grid;</li>
 *   <li>the spawn gate and a wayfinding sign.</li>
 * </ul>
 */
public final class UnderworldPostProcess {
	public static final int SPAWN_Y = 80;
	public static final int SITE_SPACING = 96;
	public static final int SITE_RADIUS = 19;
	private static final int SITE_CHANCE_PERCENT = 30;
	private static final int LAKE_SPACING = 5000;
	private static final int LAKE_RADIUS = 15;
	private static final int RIVER_LIMIT = 1000;
	private static final int RIVER_FADE = 90;
	private static final int RIVER_WIDTH = 11;

	private static final BlockState SOUL = WcnmBlocks.SOUL_BLOCK.defaultBlockState();
	private static final BlockState MAIN = WcnmBlocks.YIN_STONE.defaultBlockState();
	private static final BlockState TRIM = WcnmBlocks.CHISELED_YIN_STONE.defaultBlockState();
	private static final BlockState LAMP = WcnmBlocks.SOUL_LAMP.defaultBlockState();
	private static final BlockState LANTERN = Blocks.SOUL_LANTERN.defaultBlockState();
	private static final BlockState WATER = Blocks.WATER.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();

	private UnderworldPostProcess() {
	}

	public static void process(ChunkAccess chunk, RandomState randomState, ChunkGenerator terrain, long seed) {
		ChunkEditor editor = new ChunkEditor(chunk);
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();

		carveRivers(editor, chunk, minX, minZ, randomState, terrain);
		carveLakes(editor, chunk, minX, minZ, randomState, terrain, seed);
		buildSites(editor, chunk, minX, minZ, randomState, terrain, seed);
		spawnPad(editor, chunk, randomState, terrain, seed);
	}

	public record Site(int cellX, int cellZ, int number, int centerX, int centerZ, int baseY) {
	}

	@FunctionalInterface
	public interface HeightProvider {
		int height(int x, int z);
	}

	public static Site findNearestSite(long seed, int centerX, int centerZ, int radiusBlocks, HeightProvider heights) {
		int cellRadius = Math.max(1, radiusBlocks / SITE_SPACING + 2);
		int baseCellX = Math.floorDiv(centerX, SITE_SPACING);
		int baseCellZ = Math.floorDiv(centerZ, SITE_SPACING);
		Site best = null;
		long bestDistance = Long.MAX_VALUE;

		for (int dx = -cellRadius; dx <= cellRadius; dx++) {
			for (int dz = -cellRadius; dz <= cellRadius; dz++) {
				int cellX = baseCellX + dx;
				int cellZ = baseCellZ + dz;

				if (!siteExists(seed, cellX, cellZ)) {
					continue;
				}

				int sx = siteCenterX(cellX);
				int sz = siteCenterZ(cellZ);

				if (siteIsSpawnReserved(sx, sz)) {
					continue;
				}

				long distance = (long) (sx - centerX) * (sx - centerX) + (long) (sz - centerZ) * (sz - centerZ);

				if (distance <= (long) radiusBlocks * radiusBlocks && distance < bestDistance) {
					bestDistance = distance;
					int baseY = clampBase(heights.height(sx, sz) - 1);
					best = new Site(cellX, cellZ, siteNumber(seed, cellX, cellZ), sx, sz, baseY);
				}
			}
		}

		return best;
	}

	/** All generated building sites inside a square radius, nearest first. */
	public static List<Site> findSitesInRadius(long seed, int centerX, int centerZ, int radiusBlocks,
											   HeightProvider heights) {
		List<Site> sites = new ArrayList<>();
		int cellRadius = Math.max(1, radiusBlocks / SITE_SPACING + 2);
		int baseCellX = Math.floorDiv(centerX, SITE_SPACING);
		int baseCellZ = Math.floorDiv(centerZ, SITE_SPACING);
		long radiusSquared = (long) radiusBlocks * radiusBlocks;

		for (int dx = -cellRadius; dx <= cellRadius; dx++) {
			for (int dz = -cellRadius; dz <= cellRadius; dz++) {
				int cellX = baseCellX + dx;
				int cellZ = baseCellZ + dz;

				if (!siteExists(seed, cellX, cellZ)) {
					continue;
				}

				int sx = siteCenterX(cellX);
				int sz = siteCenterZ(cellZ);

				if (siteIsSpawnReserved(sx, sz)) {
					continue;
				}

				long distance = (long) (sx - centerX) * (sx - centerX) + (long) (sz - centerZ) * (sz - centerZ);

				if (distance > radiusSquared) {
					continue;
				}

				int baseY = clampBase(heights.height(sx, sz) - 1);
				sites.add(new Site(cellX, cellZ, siteNumber(seed, cellX, cellZ), sx, sz, baseY));
			}
		}

		sites.sort(Comparator.comparingLong(site ->
			(long) (site.centerX() - centerX) * (site.centerX() - centerX)
				+ (long) (site.centerZ() - centerZ) * (site.centerZ() - centerZ)));
		return sites;
	}

	/** Nearest site whose catalog number matches, e.g. every Ghost Gate. */
	public static Site findNearestSite(long seed, int centerX, int centerZ, int number, int radiusBlocks,
									   HeightProvider heights) {
		int cellRadius = Math.max(1, radiusBlocks / SITE_SPACING + 2);
		int baseCellX = Math.floorDiv(centerX, SITE_SPACING);
		int baseCellZ = Math.floorDiv(centerZ, SITE_SPACING);
		long radiusSquared = (long) radiusBlocks * radiusBlocks;
		Site best = null;
		long bestDistance = Long.MAX_VALUE;

		for (int dx = -cellRadius; dx <= cellRadius; dx++) {
			for (int dz = -cellRadius; dz <= cellRadius; dz++) {
				int cellX = baseCellX + dx;
				int cellZ = baseCellZ + dz;

				if (!siteExists(seed, cellX, cellZ)
					|| siteNumber(seed, cellX, cellZ) != number) {
					continue;
				}

				int sx = siteCenterX(cellX);
				int sz = siteCenterZ(cellZ);

				if (siteIsSpawnReserved(sx, sz)) {
					continue;
				}

				long distance = (long) (sx - centerX) * (sx - centerX) + (long) (sz - centerZ) * (sz - centerZ);

				if (distance <= radiusSquared && distance < bestDistance) {
					bestDistance = distance;
					int baseY = clampBase(heights.height(sx, sz) - 1);
					best = new Site(cellX, cellZ, number, sx, sz, baseY);
				}
			}
		}

		return best;
	}

	/** Sites whose centre lies inside the given chunk. */
	public static List<Site> findSitesInChunk(long seed, int chunkX, int chunkZ, HeightProvider heights) {
		int minX = chunkX << 4;
		int minZ = chunkZ << 4;
		List<Site> sites = new ArrayList<>();
		int cellMinX = Math.floorDiv(minX, SITE_SPACING);
		int cellMaxX = Math.floorDiv(minX + 15, SITE_SPACING);
		int cellMinZ = Math.floorDiv(minZ, SITE_SPACING);
		int cellMaxZ = Math.floorDiv(minZ + 15, SITE_SPACING);

		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (!siteExists(seed, cellX, cellZ)) {
					continue;
				}

				int sx = siteCenterX(cellX);
				int sz = siteCenterZ(cellZ);

				if (siteIsSpawnReserved(sx, sz)) {
					continue;
				}

				if (sx < minX || sx > minX + 15 || sz < minZ || sz > minZ + 15) {
					continue;
				}

				int baseY = clampBase(heights.height(sx, sz) - 1);
				sites.add(new Site(cellX, cellZ, siteNumber(seed, cellX, cellZ), sx, sz, baseY));
			}
		}

		sites.sort(Comparator.comparingInt(Site::centerX).thenComparingInt(Site::centerZ));
		return sites;
	}

	/** True when the meandering river band crosses this chunk. */
	public static boolean chunkIntersectsRiver(int minX, int minZ) {
		for (int z = minZ; z <= minZ + 15; z++) {
			double centerX = riverCenterX(z);
			double edge = Math.max(Math.abs(centerX), Math.abs(z));

			if (edge >= RIVER_LIMIT) {
				continue;
			}

			double mask = 1.0 - smoothstep(RIVER_LIMIT - RIVER_FADE, RIVER_LIMIT, edge);

			if (mask <= 0.02) {
				continue;
			}

			int bank = Math.max(1, (int) Math.ceil(RIVER_WIDTH * mask));

			if (centerX + bank >= minX && centerX - bank <= minX + 15) {
				return true;
			}
		}

		return false;
	}

	/** True when a sparse lake overlaps this chunk. */
	public static boolean chunkIntersectsLake(long seed, int minX, int minZ) {
		int cellMinX = Math.floorDiv(minX - LAKE_RADIUS, LAKE_SPACING);
		int cellMaxX = Math.floorDiv(minX + 15 + LAKE_RADIUS, LAKE_SPACING);
		int cellMinZ = Math.floorDiv(minZ - LAKE_RADIUS, LAKE_SPACING);
		int cellMaxZ = Math.floorDiv(minZ + 15 + LAKE_RADIUS, LAKE_SPACING);

		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				long hash = mix(seed, cellX, cellZ, 0x1A4E);
				int sx = cellX * LAKE_SPACING + (int) Math.floorMod(hash, LAKE_SPACING);
				int sz = cellZ * LAKE_SPACING + (int) Math.floorMod(hash >>> 32, LAKE_SPACING);
				int closestX = Math.max(minX, Math.min(sx, minX + 15));
				int closestZ = Math.max(minZ, Math.min(sz, minZ + 15));
				long distanceSquared = (long) (closestX - sx) * (closestX - sx)
					+ (long) (closestZ - sz) * (closestZ - sz);

				if (distanceSquared <= (long) LAKE_RADIUS * LAKE_RADIUS) {
					return true;
				}
			}
		}

		return false;
	}

	// ------------------------------------------------------------------
	// Buildings
	// ------------------------------------------------------------------

	private static void buildSites(ChunkEditor editor, ChunkAccess chunk, int minX, int minZ,
									RandomState randomState, ChunkGenerator terrain, long seed) {
		int cellMinX = Math.floorDiv(minX - SITE_RADIUS, SITE_SPACING);
		int cellMaxX = Math.floorDiv(minX + 15 + SITE_RADIUS, SITE_SPACING);
		int cellMinZ = Math.floorDiv(minZ - SITE_RADIUS, SITE_SPACING);
		int cellMaxZ = Math.floorDiv(minZ + 15 + SITE_RADIUS, SITE_SPACING);

		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				if (!siteExists(seed, cellX, cellZ)) {
					continue;
				}

				int sx = siteCenterX(cellX);
				int sz = siteCenterZ(cellZ);

				// Keep the spawn gate and its surroundings clear.
				if (siteIsSpawnReserved(sx, sz)) {
					continue;
				}

				if (sx + SITE_RADIUS < minX || sx - SITE_RADIUS > minX + 15
					|| sz + SITE_RADIUS < minZ || sz - SITE_RADIUS > minZ + 15) {
					continue;
				}

				int baseY = clampBase(terrain.getBaseHeight(sx, sz, Heightmap.Types.WORLD_SURFACE, chunk, randomState) - 1);
				flattenSite(editor, chunk, minX, minZ, randomState, terrain, sx, sz, baseY);
				UnderworldBuildings.placeAt(editor, sx, sz, siteNumber(seed, cellX, cellZ), baseY, seed);
			}
		}
	}

	private static void flattenSite(ChunkEditor editor, ChunkAccess chunk, int minX, int minZ,
									RandomState randomState, ChunkGenerator terrain,
									int sx, int sz, int baseY) {
		for (int x = sx - SITE_RADIUS; x <= sx + SITE_RADIUS; x++) {
			if (x < minX || x > minX + 15) {
				continue;
			}

			for (int z = sz - SITE_RADIUS; z <= sz + SITE_RADIUS; z++) {
				if (z < minZ || z > minZ + 15) {
					continue;
				}

				int top = terrain.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE, chunk, randomState) - 1;

				if (top < baseY) {
					for (int y = top + 1; y <= baseY; y++) {
						editor.set(x, y, z, SOUL);
					}
				} else if (top > baseY) {
					for (int y = baseY + 1; y <= top; y++) {
						editor.set(x, y, z, AIR);
					}
				}

				editor.set(x, baseY, z, SOUL);

				for (int y = baseY + 1; y <= baseY + 8; y++) {
					editor.set(x, y, z, AIR);
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// Lakes
	// ------------------------------------------------------------------

	private static void carveLakes(ChunkEditor editor, ChunkAccess chunk, int minX, int minZ,
								   RandomState randomState, ChunkGenerator terrain, long seed) {
		int cellMinX = Math.floorDiv(minX - LAKE_RADIUS, LAKE_SPACING);
		int cellMaxX = Math.floorDiv(minX + 15 + LAKE_RADIUS, LAKE_SPACING);
		int cellMinZ = Math.floorDiv(minZ - LAKE_RADIUS, LAKE_SPACING);
		int cellMaxZ = Math.floorDiv(minZ + 15 + LAKE_RADIUS, LAKE_SPACING);

		for (int cellX = cellMinX; cellX <= cellMaxX; cellX++) {
			for (int cellZ = cellMinZ; cellZ <= cellMaxZ; cellZ++) {
				long hash = mix(seed, cellX, cellZ, 0x1A4E);
				int sx = cellX * LAKE_SPACING + (int) Math.floorMod(hash, LAKE_SPACING);
				int sz = cellZ * LAKE_SPACING + (int) Math.floorMod(hash >>> 32, LAKE_SPACING);

				if (sx + LAKE_RADIUS < minX || sx - LAKE_RADIUS > minX + 15
					|| sz + LAKE_RADIUS < minZ || sz - LAKE_RADIUS > minZ + 15) {
					continue;
				}

				int centerTop = terrain.getBaseHeight(sx, sz, Heightmap.Types.WORLD_SURFACE, chunk, randomState) - 1;
				int waterLevel = centerTop - 3;

				for (int x = Math.max(minX, sx - LAKE_RADIUS); x <= Math.min(minX + 15, sx + LAKE_RADIUS); x++) {
					for (int z = Math.max(minZ, sz - LAKE_RADIUS); z <= Math.min(minZ + 15, sz + LAKE_RADIUS); z++) {
						double distance = Math.sqrt((double) (x - sx) * (x - sx) + (double) (z - sz) * (z - sz));

						if (distance > LAKE_RADIUS) {
							continue;
						}

						double factor = 1.0 - distance / LAKE_RADIUS;
						int depth = 2 + (int) (factor * 5.0);
						int top = terrain.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE, chunk, randomState) - 1;

						for (int y = waterLevel + 1; y <= top; y++) {
							editor.set(x, y, z, AIR);
						}

						for (int y = waterLevel - depth + 1; y <= waterLevel; y++) {
							editor.set(x, y, z, WATER);
						}

						editor.set(x, waterLevel - depth, z, SOUL);
					}
				}
			}
		}
	}

	// ------------------------------------------------------------------
	// River: one meandering river, only inside |x|,|z| <= 1000
	// ------------------------------------------------------------------

	private static void carveRivers(ChunkEditor editor, ChunkAccess chunk, int minX, int minZ,
									RandomState randomState, ChunkGenerator terrain) {
		for (int z = minZ; z <= minZ + 15; z++) {
			double centerX = riverCenterX(z);
			double edge = Math.max(Math.abs(centerX), Math.abs(z));

			if (edge >= RIVER_LIMIT) {
				continue;
			}

			double mask = 1.0 - smoothstep(RIVER_LIMIT - RIVER_FADE, RIVER_LIMIT, edge);

			if (mask <= 0.02) {
				continue;
			}

			int bank = Math.max(1, (int) Math.ceil(RIVER_WIDTH * mask));
			int centerTop = terrain.getBaseHeight((int) Math.round(centerX), z, Heightmap.Types.WORLD_SURFACE, chunk, randomState) - 1;
			int waterLevel = centerTop - 2;

			for (int x = Math.max(minX, (int) Math.floor(centerX) - bank - 1);
				 x <= Math.min(minX + 15, (int) Math.ceil(centerX) + bank + 1); x++) {
				double distance = Math.abs(x - centerX);

				if (distance > bank) {
					continue;
				}

				double factor = 1.0 - distance / bank;
				int depth = 1 + (int) (factor * 3.0 * mask);
				int top = terrain.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE, chunk, randomState) - 1;

				for (int y = waterLevel + 1; y <= top; y++) {
					editor.set(x, y, z, AIR);
				}

				for (int y = waterLevel - depth + 1; y <= waterLevel; y++) {
					editor.set(x, y, z, WATER);
				}

				editor.set(x, waterLevel - depth, z, SOUL);
			}
		}
	}

	public static double riverCenterX(int z) {
		return 250.0 + 160.0 * Math.sin(z / 240.0) + 60.0 * Math.sin(z / 71.0);
	}

	// ------------------------------------------------------------------
	// Spawn
	// ------------------------------------------------------------------

	private static void spawnPad(ChunkEditor editor, ChunkAccess chunk, RandomState randomState,
								 ChunkGenerator terrain, long seed) {
		int minX = chunk.getPos().getMinBlockX();
		int minZ = chunk.getPos().getMinBlockZ();

		for (int x = -12; x <= 12; x++) {
			if (x < minX || x > minX + 15) {
				continue;
			}

			for (int z = -14; z <= 12; z++) {
				if (z < minZ || z > minZ + 15) {
					continue;
				}

				int top = terrain.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE, chunk, randomState) - 1;

				if (top < SPAWN_Y) {
					for (int y = top + 1; y <= SPAWN_Y; y++) {
						editor.set(x, y, z, SOUL);
					}
				} else if (top > SPAWN_Y) {
					for (int y = SPAWN_Y + 1; y <= top; y++) {
						editor.set(x, y, z, AIR);
					}
				}

				editor.set(x, SPAWN_Y, z, SOUL);

				for (int y = SPAWN_Y + 1; y <= SPAWN_Y + 7; y++) {
					editor.set(x, y, z, AIR);
				}
			}
		}

		if (minX <= 0 && minX + 15 >= 0 && minZ <= -14 && minZ + 15 >= -2) {
			spawnGate(editor, terrain, randomState, seed);
		}
	}

	private static void spawnGate(ChunkEditor editor, ChunkGenerator terrain, RandomState randomState, long seed) {
		int y = SPAWN_Y + 1;
		int oz = -10;

		for (int side : new int[]{-1, 1}) {
			for (int dx = 0; dx <= 1; dx++) {
				for (int dz = 0; dz <= 1; dz++) {
					for (int dy = 0; dy <= 8; dy++) {
						editor.set(0 + side * 5 - dx, y + dy, oz - dz, MAIN);
					}
				}
			}

			editor.set(0 + side * 5, y + 9, oz, LANTERN);
		}

		for (int dx = -6; dx <= 6; dx++) {
			editor.set(dx, y + 9, oz, TRIM);
			editor.set(dx, y + 9, oz - 1, TRIM);
		}

		for (int layer = 0; layer < 3; layer++) {
			int width = 15 - layer * 4;

			if (width < 1) {
				break;
			}

			for (int dx = -width / 2; dx <= width / 2; dx++) {
				editor.set(dx, y + 10 + layer, oz, layer == 0 ? TRIM : MAIN);
				editor.set(dx, y + 10 + layer, oz - 1, layer == 0 ? TRIM : MAIN);
			}
		}

		editor.set(0, y + 13, oz, LAMP);

		editor.set(0, SPAWN_Y, -5, LAMP);
		editor.set(0, y, -5, AIR);
		editor.setSign(0, y, -5, Direction.SOUTH, false, 8, ChunkEditor.signLines(0, "鬼门关"));

		Site nearest = findNearestSite(seed, 0, 0, 200,
			(bx, bz) -> terrain.getBaseHeight(bx, bz, Heightmap.Types.WORLD_SURFACE, editor.heightAccessor(), randomState));

		if (nearest != null) {
			BuildingCatalog.Building building = BuildingCatalog.get(nearest.number());
			editor.set(3, SPAWN_Y, -7, LAMP);
			editor.set(3, y, -7, AIR);
			editor.setSign(3, y, -7, Direction.SOUTH, false, 8, List.of(
				Component.literal("最近冥筑"),
				Component.literal("第" + building.number() + "号 " + building.name()),
				Component.literal("(" + nearest.centerX() + ", " + nearest.centerZ() + ")"),
				Component.literal("/wcnm find")
			));
		}
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	private static boolean siteExists(long seed, int cellX, int cellZ) {
		return Math.floorMod(mix(seed, cellX, cellZ, 0xB01D), 100) < SITE_CHANCE_PERCENT;
	}

	/** The four cells around spawn are reserved for the gate/plaza and never produce a building. */
	private static boolean siteIsSpawnReserved(int centerX, int centerZ) {
		return Math.abs(centerX) < 96 && Math.abs(centerZ) < 96;
	}

	private static int siteNumber(long seed, int cellX, int cellZ) {
		return 1 + (int) Math.floorMod(mix(seed, cellX, cellZ, 0x5EED), 50);
	}

	private static int siteCenterX(int cellX) {
		return cellX * SITE_SPACING + SITE_SPACING / 2;
	}

	private static int siteCenterZ(int cellZ) {
		return cellZ * SITE_SPACING + SITE_SPACING / 2;
	}

	private static int clampBase(int baseY) {
		return Math.max(-32, Math.min(200, baseY));
	}

	private static double smoothstep(double edge0, double edge1, double value) {
		double t = Math.max(0.0, Math.min(1.0, (value - edge0) / (edge1 - edge0)));
		return t * t * (3.0 - 2.0 * t);
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

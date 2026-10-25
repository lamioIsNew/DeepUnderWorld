package com.wcnm.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The fifty reserved underworld buildings.  Number, name and archetype are fixed; size varies
 * with the number so that repeated archetypes still feel different.
 */
public final class BuildingCatalog {
	public static final int COUNT = 50;
	private static final Building[] ALL = new Building[COUNT];

	/** English names used by {@code /wcnm find structure}.  Index 0 is building #1. */
	private static final String[] ENGLISH_NAMES = {
		"Ghost Gate",
		"Yellow Springs Road",
		"Naihe Bridge",
		"Wangxiang Terrace",
		"Mengpo Pavilion",
		"Wangchuan Crossing",
		"Three Lives Stone",
		"Spider Lily Sea",
		"Yama Hall",
		"Judge Office",
		"Reincarnation Well",
		"Fengdu City",
		"Eighteen Hells",
		"Knife Mountain Hell",
		"Oil Cauldron Hell",
		"Tongue Pulling Hell",
		"Sin Mirror Terrace",
		"Book of Life and Death",
		"Soul Hooking Bureau",
		"Soul Locking Tower",
		"Soul Guiding Lantern Tower",
		"White Bone Hall",
		"Nether Inn",
		"Paper Money Shop",
		"Underworld Garment Shop",
		"Soul Ferry Boat",
		"Black Water River",
		"Blood Pool",
		"Ice Hell",
		"Volcano Hell",
		"Stone Mill Hell",
		"Sawing Hell",
		"Copper Pillar Hell",
		"Iron Bed Hell",
		"Steamer Hell",
		"Dark Hell",
		"Heart Weighing Hall",
		"Karma Terrace",
		"Six Paths Gate",
		"Reincarnation Hall",
		"Zhong Kui Temple",
		"City God Temple",
		"Earth God Shrine",
		"Ghost Soldier Barracks",
		"Ghost Market",
		"Wuchang Pavilion",
		"Ox Head Hall",
		"Horse Face Hall",
		"Nether Bell Tower",
		"Wangchuan Bridge"
	};

	static {
		add(1, "鬼门关", Archetype.GATE);
		add(2, "黄泉路", Archetype.BRIDGE);
		add(3, "奈何桥", Archetype.BRIDGE);
		add(4, "望乡台", Archetype.TOWER);
		add(5, "孟婆亭", Archetype.PAVILION);
		add(6, "忘川渡", Archetype.BRIDGE);
		add(7, "三生石", Archetype.SHRINE);
		add(8, "彼岸花海", Archetype.POND);
		add(9, "阎罗殿", Archetype.HALL);
		add(10, "判官署", Archetype.HALL);
		add(11, "轮回井", Archetype.WELL);
		add(12, "酆都城", Archetype.GATE);
		add(13, "十八层狱", Archetype.PRISON);
		add(14, "刀山狱", Archetype.PRISON);
		add(15, "油锅狱", Archetype.PRISON);
		add(16, "拔舌狱", Archetype.PRISON);
		add(17, "孽镜台", Archetype.TOWER);
		add(18, "生死簿阁", Archetype.HALL);
		add(19, "勾魂司", Archetype.HALL);
		add(20, "锁魂塔", Archetype.TOWER);
		add(21, "引魂灯台", Archetype.TOWER);
		add(22, "白骨殿", Archetype.HALL);
		add(23, "幽冥客栈", Archetype.HALL);
		add(24, "纸钱铺", Archetype.MARKET);
		add(25, "冥衣坊", Archetype.MARKET);
		add(26, "渡魂舟", Archetype.BRIDGE);
		add(27, "黑水河", Archetype.POND);
		add(28, "血池", Archetype.POND);
		add(29, "寒冰狱", Archetype.PRISON);
		add(30, "火山狱", Archetype.PRISON);
		add(31, "石磨狱", Archetype.PRISON);
		add(32, "锯解狱", Archetype.PRISON);
		add(33, "铜柱狱", Archetype.PRISON);
		add(34, "铁床狱", Archetype.PRISON);
		add(35, "蒸笼狱", Archetype.PRISON);
		add(36, "黑暗狱", Archetype.PRISON);
		add(37, "秤心殿", Archetype.HALL);
		add(38, "因果台", Archetype.TOWER);
		add(39, "六道门", Archetype.GATE);
		add(40, "轮回殿", Archetype.HALL);
		add(41, "钟馗庙", Archetype.SHRINE);
		add(42, "城隍庙", Archetype.SHRINE);
		add(43, "土地祠", Archetype.SHRINE);
		add(44, "阴兵营", Archetype.PRISON);
		add(45, "鬼市", Archetype.MARKET);
		add(46, "无常亭", Archetype.PAVILION);
		add(47, "牛头殿", Archetype.HALL);
		add(48, "马面殿", Archetype.HALL);
		add(49, "幽冥钟楼", Archetype.TOWER);
		add(50, "忘川桥", Archetype.BRIDGE);
	}

	private BuildingCatalog() {
	}

	public static Building get(int number) {
		return ALL[Math.floorMod(number - 1, COUNT)];
	}

	public static String englishName(int number) {
		return ENGLISH_NAMES[Math.floorMod(number - 1, COUNT)];
	}

	/** Stable lower-case English id, e.g. {@code ghost-gate}. */
	public static String structureId(int number) {
		return slug(englishName(number));
	}

	/** Case-insensitive match against the English id, English name, Chinese name, or number. */
	public static Building find(String query) {
		if (query == null) {
			return null;
		}

		String trimmed = query.trim();

		if (trimmed.isEmpty()) {
			return null;
		}

		String normalized = slug(trimmed);

		if (normalized.startsWith("wcnm-")) {
			normalized = normalized.substring("wcnm-".length());
		}

		for (Building building : ALL) {
			if (normalized.equals(structureId(building.number()))
				|| normalized.equals(slug(building.name()))
				|| trimmed.equals(building.name())) {
				return building;
			}
		}

		try {
			int number = Integer.parseInt(trimmed);
			return number >= 1 && number <= COUNT ? ALL[number - 1] : null;
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	public static List<Building> all() {
		List<Building> buildings = new ArrayList<>(COUNT);

		for (Building building : ALL) {
			buildings.add(building);
		}

		return buildings;
	}

	private static String slug(String value) {
		return value.toLowerCase(Locale.ROOT)
			.replace("'", "")
			.replaceAll("[^a-z0-9]+", "-")
			.replaceAll("^-+|-+$", "");
	}

	private static void add(int number, String name, Archetype archetype) {
		int variant = (number - 1) / 10;
		int width;
		int depth;
		int height;

		switch (archetype) {
			case GATE -> {
				width = 11 + variant * 2;
				depth = 5 + variant * 2;
				height = 6 + variant;
			}
			case HALL -> {
				width = 11 + variant * 2;
				depth = 9 + variant * 2;
				height = 5 + variant;
			}
			case PAVILION -> {
				width = 9 + variant * 2;
				depth = 9 + variant * 2;
				height = 4 + variant;
			}
			case TOWER -> {
				width = 9;
				depth = 9;
				height = 10 + variant * 2;
			}
			case WELL -> {
				width = 7;
				depth = 7;
				height = 4;
			}
			case BRIDGE -> {
				width = 15 + variant * 2;
				depth = 5;
				height = 3;
			}
			case SHRINE -> {
				width = 9;
				depth = 7;
				height = 4;
			}
			case PRISON -> {
				width = 13;
				depth = 11;
				height = 5;
			}
			case MARKET -> {
				width = 13;
				depth = 9;
				height = 4;
			}
			case POND -> {
				width = 11;
				depth = 11;
				height = 2;
			}
			default -> throw new IllegalStateException("Unknown archetype " + archetype);
		}

		ALL[number - 1] = new Building(number, name, archetype,
			Math.min(24, width), Math.min(24, depth), height);
	}

	public enum Archetype {
		GATE,
		HALL,
		PAVILION,
		TOWER,
		WELL,
		BRIDGE,
		SHRINE,
		PRISON,
		MARKET,
		POND
	}

	public record Building(int number, String name, Archetype archetype, int width, int depth, int height) {
	}
}

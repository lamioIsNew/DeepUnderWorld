package com.wcnm.client;

import com.wcnm.WcnmMod;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Per-player client settings.  The visible distance is a client-side override for the
 * underworld's atmospheric fog; it never changes the server or other players.
 */
public final class WcnmClientConfig {
	public static final float DEFAULT = -1.0F;
	public static final float MIN_VISIBLE_DISTANCE = 8.0F;
	public static final float MAX_VISIBLE_DISTANCE = 512.0F;
	private static final String KEY = "underworld.visibleDistance";
	private static final String EFFECTS_KEY = "underworld.effects";
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("wcnm-client.properties");
	private static float visibleDistance = DEFAULT;
	private static boolean effectsEnabled = true;

	private WcnmClientConfig() {
	}

	public static void load() {
		if (!Files.exists(FILE)) {
			return;
		}

		Properties properties = new Properties();

		try (InputStream input = Files.newInputStream(FILE)) {
			properties.load(input);
			String value = properties.getProperty(KEY);

			if (value != null && !value.isBlank()) {
				float parsed = Float.parseFloat(value);
				visibleDistance = parsed <= 0.0F ? DEFAULT : clamp(parsed);
			}

			effectsEnabled = Boolean.parseBoolean(properties.getProperty(EFFECTS_KEY, "true"));
		} catch (Exception exception) {
			WcnmMod.LOGGER.warn("Failed to read {}", FILE, exception);
		}
	}

	public static void save() {
		Properties properties = new Properties();
		properties.setProperty(KEY, Float.toString(visibleDistance));
		properties.setProperty(EFFECTS_KEY, Boolean.toString(effectsEnabled));

		try {
			Files.createDirectories(FILE.getParent());

			try (OutputStream output = Files.newOutputStream(FILE)) {
				properties.store(output, "Deep UnderWorld client settings");
			}
		} catch (Exception exception) {
			WcnmMod.LOGGER.warn("Failed to write {}", FILE, exception);
		}
	}

	public static boolean isEnabled() {
		return visibleDistance > 0.0F;
	}

	public static boolean isEffectsEnabled() {
		return effectsEnabled;
	}

	public static void setEffectsEnabled(boolean enabled) {
		effectsEnabled = enabled;
		save();
	}

	public static float getVisibleDistance() {
		return visibleDistance;
	}

	public static void setVisibleDistance(float value) {
		visibleDistance = clamp(value);
		save();
	}

	public static void reset() {
		visibleDistance = DEFAULT;
		save();
	}

	private static float clamp(float value) {
		return Math.max(MIN_VISIBLE_DISTANCE, Math.min(MAX_VISIBLE_DISTANCE, value));
	}
}

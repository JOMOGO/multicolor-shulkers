package com.multicolorshulkers.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.multicolorshulkers.MultiColorShulkers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dual-dye-shulkers.json");

	private static ModConfig INSTANCE;

	// Config options
	public boolean showTooltip = true;

	// Top binding: Shift + Right Click
	public String topKey1 = "key.keyboard.left.shift";
	public String topKey2 = "key.mouse.right";

	// Bottom binding: Ctrl + Right Click
	public String bottomKey1 = "key.keyboard.left.control";
	public String bottomKey2 = "key.mouse.right";

	// Transient cached combos
	private transient KeyCombo cachedTopCombo;
	private transient KeyCombo cachedBottomCombo;

	// Default combos
	public static final KeyCombo DEFAULT_TOP_COMBO = new KeyCombo(
			InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_LEFT_SHIFT),
			InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
	);
	public static final KeyCombo DEFAULT_BOTTOM_COMBO = new KeyCombo(
			InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_LEFT_CONTROL),
			InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
	);

	public static ModConfig get() {
		if (INSTANCE == null) {
			INSTANCE = load();
		}
		return INSTANCE;
	}

	public KeyCombo getTopCombo() {
		if (cachedTopCombo == null) {
			try {
				InputUtil.Key k1 = InputUtil.fromTranslationKey(topKey1);
				InputUtil.Key k2 = InputUtil.fromTranslationKey(topKey2);
				cachedTopCombo = new KeyCombo(k1, k2);
			} catch (Exception e) {
				MultiColorShulkers.LOGGER.warn("Invalid top combo, using default");
				cachedTopCombo = DEFAULT_TOP_COMBO;
			}
		}
		return cachedTopCombo;
	}

	public void setTopCombo(KeyCombo combo) {
		this.cachedTopCombo = combo;
		this.topKey1 = combo.getKey1().getTranslationKey();
		this.topKey2 = combo.getKey2().getTranslationKey();
	}

	public KeyCombo getBottomCombo() {
		if (cachedBottomCombo == null) {
			try {
				InputUtil.Key k1 = InputUtil.fromTranslationKey(bottomKey1);
				InputUtil.Key k2 = InputUtil.fromTranslationKey(bottomKey2);
				cachedBottomCombo = new KeyCombo(k1, k2);
			} catch (Exception e) {
				MultiColorShulkers.LOGGER.warn("Invalid bottom combo, using default");
				cachedBottomCombo = DEFAULT_BOTTOM_COMBO;
			}
		}
		return cachedBottomCombo;
	}

	public void setBottomCombo(KeyCombo combo) {
		this.cachedBottomCombo = combo;
		this.bottomKey1 = combo.getKey1().getTranslationKey();
		this.bottomKey2 = combo.getKey2().getTranslationKey();
	}

	public static ModConfig load() {
		if (Files.exists(CONFIG_PATH)) {
			try {
				String json = Files.readString(CONFIG_PATH);
				ModConfig config = GSON.fromJson(json, ModConfig.class);
				if (config != null) {
					MultiColorShulkers.LOGGER.info("Loaded config from {}", CONFIG_PATH);
					return config;
				}
			} catch (IOException e) {
				MultiColorShulkers.LOGGER.error("Failed to load config", e);
			}
		}
		ModConfig config = new ModConfig();
		config.save();
		return config;
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
			MultiColorShulkers.LOGGER.info("Saved config to {}", CONFIG_PATH);
		} catch (IOException e) {
			MultiColorShulkers.LOGGER.error("Failed to save config", e);
		}
	}

	public static void reload() {
		INSTANCE = load();
	}
}

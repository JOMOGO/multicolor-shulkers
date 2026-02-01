package com.multicolorshulkers.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.multicolorshulkers.MultiColorShulkers;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dual-dye-shulkers.json");

	private static ModConfig INSTANCE;
	public boolean showTooltip = true;
	public boolean enableCrafting = true;
	public boolean enableKeybinds = true;

	// Top binding: Shift + Right Click
	public String topKey1 = "key.keyboard.left.shift";
	public String topKey2 = "key.mouse.right";

	// Bottom binding: Ctrl + Right Click
	public String bottomKey1 = "key.keyboard.left.control";
	public String bottomKey2 = "key.mouse.right";

	public static ModConfig get() {
		if (INSTANCE == null) {
			INSTANCE = load();
		}
		return INSTANCE;
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

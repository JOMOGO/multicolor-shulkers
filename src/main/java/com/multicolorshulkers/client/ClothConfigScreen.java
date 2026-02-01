package com.multicolorshulkers.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Cloth Config screen builder - isolated in separate class to avoid class loading issues
 * when Cloth Config is not installed.
 */
public class ClothConfigScreen {

	public static Screen create(Screen parent) {
		ModConfig config = ModConfig.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Text.translatable("config.dual-dye-shulkers.title"))
				.setSavingRunnable(config::save);

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();

		// General category
		ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.dual-dye-shulkers.category.general"));

		general.addEntry(entryBuilder.startBooleanToggle(
						Text.translatable("config.dual-dye-shulkers.showTooltip"),
						config.showTooltip)
				.setDefaultValue(true)
				.setTooltip(Text.translatable("config.dual-dye-shulkers.showTooltip.tooltip"))
				.setSaveConsumer(value -> config.showTooltip = value)
				.build());

		general.addEntry(entryBuilder.startBooleanToggle(
						Text.translatable("config.dual-dye-shulkers.enableCrafting"),
						config.enableCrafting)
				.setDefaultValue(true)
				.setTooltip(Text.translatable("config.dual-dye-shulkers.enableCrafting.tooltip"))
				.setSaveConsumer(value -> config.enableCrafting = value)
				.build());

		general.addEntry(entryBuilder.startBooleanToggle(
						Text.translatable("config.dual-dye-shulkers.enableKeybinds"),
						config.enableKeybinds)
				.setDefaultValue(true)
				.setTooltip(Text.translatable("config.dual-dye-shulkers.enableKeybinds.tooltip"))
				.setSaveConsumer(value -> config.enableKeybinds = value)
				.build());

		// Controls category
		ConfigCategory controls = builder.getOrCreateCategory(Text.translatable("config.dual-dye-shulkers.category.controls"));

		// Top binding - custom key combo entry
		controls.addEntry(new KeyComboEntry(
				Text.translatable("config.dual-dye-shulkers.topCombo"),
				ClientConfigHelper.getTopCombo(config),
				ClientConfigHelper.DEFAULT_TOP_COMBO,
				combo -> ClientConfigHelper.setTopCombo(config, combo),
				Text.translatable("config.dual-dye-shulkers.topCombo.tooltip")
		));

		// Bottom binding - custom key combo entry
		controls.addEntry(new KeyComboEntry(
				Text.translatable("config.dual-dye-shulkers.bottomCombo"),
				ClientConfigHelper.getBottomCombo(config),
				ClientConfigHelper.DEFAULT_BOTTOM_COMBO,
				combo -> ClientConfigHelper.setBottomCombo(config, combo),
				Text.translatable("config.dual-dye-shulkers.bottomCombo.tooltip")
		));

		return builder.build();
	}
}

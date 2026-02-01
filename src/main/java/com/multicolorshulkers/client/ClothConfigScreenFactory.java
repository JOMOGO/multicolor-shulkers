package com.multicolorshulkers.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;

/**
 * Factory class to isolate Cloth Config imports.
 * This class is only loaded when Cloth Config is confirmed present.
 */
public class ClothConfigScreenFactory {

	public static ConfigScreenFactory<?> getFactory() {
		return ClothConfigScreen::create;
	}
}

package com.multicolorshulkers.client;

import com.multicolorshulkers.MultiColorShulkers;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		// Check if Cloth Config is available (try both possible mod IDs)
		boolean clothConfigPresent = FabricLoader.getInstance().isModLoaded("cloth-config")
				|| FabricLoader.getInstance().isModLoaded("cloth-config2");

		MultiColorShulkers.LOGGER.info("[ModMenu] Cloth Config present: {}", clothConfigPresent);

		if (clothConfigPresent) {
			// Use a separate class to avoid loading Cloth Config classes when not present
			return ClothConfigScreenFactory.getFactory();
		}
		return parent -> null;
	}
}

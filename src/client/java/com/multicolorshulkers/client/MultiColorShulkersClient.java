package com.multicolorshulkers.client;

import com.multicolorshulkers.ColorSyncPayload;
import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiColorShulkersClient implements ClientModInitializer {

	// Client-side cache of shulker colors for placed blocks
	public static final Map<BlockPos, ShulkerColors> COLOR_CACHE = new ConcurrentHashMap<>();

	@Override
	public void onInitializeClient() {
		// Register packet receiver
		ClientPlayNetworking.registerGlobalReceiver(ColorSyncPayload.ID, (payload, context) -> {
			BlockPos pos = payload.pos();
			int topColor = payload.topColor();
			int bottomColor = payload.bottomColor();

			if (topColor == -1 && bottomColor == -1) {
				// Clear sync - remove from cache
				COLOR_CACHE.remove(pos);
				MultiColorShulkers.LOGGER.debug("[CLIENT] Cleared colors cache for {}", pos);
			} else {
				// Update cache with new colors
				ShulkerColors colors = new ShulkerColors(topColor, bottomColor);
				COLOR_CACHE.put(pos, colors);
				MultiColorShulkers.LOGGER.debug("[CLIENT] Received colors for {}: top={}, bottom={}",
					pos, topColor, bottomColor);
			}
		});

		// Register tooltip callback
		ItemTooltipCallback.EVENT.register(ShulkerBoxTooltipCallback::addTooltip);
	}

	public static ShulkerColors getColors(BlockPos pos) {
		return COLOR_CACHE.get(pos);
	}
}

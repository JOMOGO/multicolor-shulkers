package com.multicolorshulkers.client;

import com.multicolorshulkers.ColorSyncPayload;
import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiColorShulkersClient implements ClientModInitializer {

	// Client-side cache of shulker colors
	public static final Map<BlockPos, ShulkerColors> COLOR_CACHE = new ConcurrentHashMap<>();

	@Override
	public void onInitializeClient() {
		// Register packet receiver
		ClientPlayNetworking.registerGlobalReceiver(ColorSyncPayload.ID, (payload, context) -> {
			BlockPos pos = payload.pos();
			ShulkerColors colors = new ShulkerColors(payload.topColor(), payload.bottomColor());

			COLOR_CACHE.put(pos, colors);
			MultiColorShulkers.LOGGER.info("[CLIENT] Received colors for {}: top={}, bottom={}",
				pos, colors.topColor(), colors.bottomColor());
		});
	}

	public static ShulkerColors getColors(BlockPos pos) {
		return COLOR_CACHE.get(pos);
	}
}

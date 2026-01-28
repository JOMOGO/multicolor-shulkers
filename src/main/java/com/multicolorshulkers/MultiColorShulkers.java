package com.multicolorshulkers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MultiColorShulkers implements ModInitializer {
	public static final String MOD_ID = "multicolor-shulkers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public record ShulkerColors(int topColor, int bottomColor) {
		public static final Codec<ShulkerColors> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				Codec.INT.fieldOf("topColor").forGetter(ShulkerColors::topColor),
				Codec.INT.fieldOf("bottomColor").forGetter(ShulkerColors::bottomColor)
			).apply(instance, ShulkerColors::new)
		);

		public static final ShulkerColors DEFAULT = new ShulkerColors(-1, -1);

		public ShulkerColors withTopColor(int color) {
			return new ShulkerColors(color, this.bottomColor);
		}

		public ShulkerColors withBottomColor(int color) {
			return new ShulkerColors(this.topColor, color);
		}
	}

	public static final AttachmentType<ShulkerColors> SHULKER_COLORS = AttachmentRegistry.createPersistent(
		Identifier.of(MOD_ID, "colors"),
		ShulkerColors.CODEC
	);

	@Override
	public void onInitialize() {
		LOGGER.info("Multi-Color Shulker Boxes initialized!");

		// Register the sync packet
		PayloadTypeRegistry.playS2C().register(ColorSyncPayload.ID, ColorSyncPayload.CODEC);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			// Only process on server side
			if (world.isClient()) {
				return ActionResult.PASS;
			}

			var blockPos = hitResult.getBlockPos();
			var blockState = world.getBlockState(blockPos);
			var block = blockState.getBlock();

			// Check if it's a shulker box
			if (!(block instanceof ShulkerBoxBlock)) {
				return ActionResult.PASS;
			}

			ItemStack heldItem = player.getStackInHand(hand);

			// Check if holding a dye
			if (!(heldItem.getItem() instanceof DyeItem dyeItem)) {
				return ActionResult.PASS;
			}

			var blockEntity = world.getBlockEntity(blockPos);
			if (!(blockEntity instanceof ShulkerBoxBlockEntity shulkerBox)) {
				return ActionResult.PASS;
			}

			int dyeColor = dyeItem.getColor().getId();

			// Get current colors or create default
			ShulkerColors currentColors = shulkerBox.getAttachedOrCreate(SHULKER_COLORS, () -> ShulkerColors.DEFAULT);
			ShulkerColors newColors;

			if (player.isSneaking()) {
				newColors = currentColors.withBottomColor(dyeColor);
				LOGGER.info("Setting bottom color to {} ({})", dyeItem.getColor().getName(), dyeColor);
			} else {
				newColors = currentColors.withTopColor(dyeColor);
				LOGGER.info("Setting top color to {} ({})", dyeItem.getColor().getName(), dyeColor);
			}

			// Set the new colors
			shulkerBox.setAttached(SHULKER_COLORS, newColors);
			shulkerBox.markDirty();

			// Sync to all nearby players
			if (world instanceof ServerWorld serverWorld) {
				syncColorsToClients(serverWorld, blockPos, newColors);
			}

			// Consume dye in survival
			if (!player.getAbilities().creativeMode) {
				heldItem.decrement(1);
			}

			// Swing arm
			if (player instanceof ServerPlayerEntity serverPlayer) {
				serverPlayer.swingHand(hand, true);
			}

			LOGGER.info("Colors now: top={}, bottom={}", newColors.topColor(), newColors.bottomColor());

			return ActionResult.SUCCESS;
		});
	}

	private void syncColorsToClients(ServerWorld world, BlockPos pos, ShulkerColors colors) {
		ColorSyncPayload payload = new ColorSyncPayload(pos, colors.topColor(), colors.bottomColor());

		// Send to all players tracking this block
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (player.getBlockPos().isWithinDistance(pos, 64)) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}
}

package com.multicolorshulkers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
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

		// Register cauldron behavior to wash off custom colors from shulker box items
		registerCauldronBehavior();

		// Sync colors when a shulker box block entity is loaded (e.g., placed from item, chunk load)
		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
			if (!(world instanceof ServerWorld serverWorld)) return;
			if (!(blockEntity instanceof ShulkerBoxBlockEntity shulkerBox)) return;

			ShulkerColors colors = shulkerBox.getAttached(SHULKER_COLORS);
			BlockPos pos = shulkerBox.getPos();

			// Schedule sync for next tick to avoid issues during world load
			serverWorld.getServer().execute(() -> {
				if (serverWorld.getPlayers().isEmpty()) return;
				if (!(serverWorld.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity)) return;

				if (colors == null || (colors.topColor() == -1 && colors.bottomColor() == -1)) {
					// No custom colors - send a "clear" sync to remove any stale cache entries
					syncColorsToClients(serverWorld, pos, ShulkerColors.DEFAULT);
				} else {
					// Has custom colors - sync them
					syncColorsToClients(serverWorld, pos, colors);
				}
			});
		});

		// Clear cache when shulker box is unloaded/broken
		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
			if (!(world instanceof ServerWorld serverWorld)) return;
			if (!(blockEntity instanceof ShulkerBoxBlockEntity shulkerBox)) return;

			// Send a "clear" sync to remove cache entry
			BlockPos pos = shulkerBox.getPos();
			syncColorsToClients(serverWorld, pos, ShulkerColors.DEFAULT);
		});

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
			LOGGER.info("[DYE] Current colors before applying: top={}, bottom={}",
				currentColors.topColor(), currentColors.bottomColor());
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

	private void registerCauldronBehavior() {
		// Register for all shulker box items (undyed + 16 colors)
		Item[] shulkerItems = {
			Items.SHULKER_BOX,
			Items.WHITE_SHULKER_BOX, Items.ORANGE_SHULKER_BOX, Items.MAGENTA_SHULKER_BOX, Items.LIGHT_BLUE_SHULKER_BOX,
			Items.YELLOW_SHULKER_BOX, Items.LIME_SHULKER_BOX, Items.PINK_SHULKER_BOX, Items.GRAY_SHULKER_BOX,
			Items.LIGHT_GRAY_SHULKER_BOX, Items.CYAN_SHULKER_BOX, Items.PURPLE_SHULKER_BOX, Items.BLUE_SHULKER_BOX,
			Items.BROWN_SHULKER_BOX, Items.GREEN_SHULKER_BOX, Items.RED_SHULKER_BOX, Items.BLACK_SHULKER_BOX
		};

		for (Item shulkerItem : shulkerItems) {
			// Get the original vanilla behavior (if any) to chain to
			CauldronBehavior originalBehavior = CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().get(shulkerItem);

			CauldronBehavior washShulkerColors = (state, world, pos, player, hand, stack) -> {
				// Check if the item has our custom colors stored in block entity data
				ShulkerColors colors = getColorsFromItemStack(stack);

				if (colors != null && (colors.topColor() != -1 || colors.bottomColor() != -1)) {
					// We have custom colors - remove them
					if (!world.isClient()) {
						LOGGER.info("[CAULDRON] Removing custom colors from item");
						removeColorsFromItemStack(stack);

						// Decrement cauldron water level
						LeveledCauldronBlock.decrementFluidLevel(state, world, pos);

						// Stats
						player.incrementStat(Stats.CLEAN_SHULKER_BOX);
					}
					return ActionResult.SUCCESS;
				}

				// No custom colors - delegate to original vanilla behavior
				if (originalBehavior != null) {
					return originalBehavior.interact(state, world, pos, player, hand, stack);
				}
				return ActionResult.PASS;
			};

			CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(shulkerItem, washShulkerColors);
		}

		LOGGER.info("Registered cauldron behavior for washing shulker box colors");
	}

	/**
	 * Get colors from an ItemStack's stored block entity data.
	 * In 1.20.5+, block entity data is stored in item components.
	 */
	public static ShulkerColors getColorsFromItemStack(ItemStack stack) {
		var beData = stack.get(net.minecraft.component.DataComponentTypes.BLOCK_ENTITY_DATA);
		if (beData == null) {
			LOGGER.debug("[ITEM] No BLOCK_ENTITY_DATA component");
			return null;
		}

		var nbt = beData.copyNbt();
		LOGGER.debug("[ITEM] Block entity NBT keys: {}", nbt.getKeys());

		if (!nbt.contains("fabric:attachments", net.minecraft.nbt.NbtElement.COMPOUND_TYPE)) {
			LOGGER.debug("[ITEM] No fabric:attachments in NBT");
			return null;
		}

		var attachments = nbt.getCompound("fabric:attachments");
		LOGGER.debug("[ITEM] Attachments keys: {}", attachments.getKeys());

		String key = MOD_ID + ":colors";
		if (!attachments.contains(key, net.minecraft.nbt.NbtElement.COMPOUND_TYPE)) {
			LOGGER.debug("[ITEM] No {} key in attachments", key);
			return null;
		}

		var colorsNbt = attachments.getCompound(key);
		int topColor = colorsNbt.contains("topColor", net.minecraft.nbt.NbtElement.INT_TYPE) ? colorsNbt.getInt("topColor") : -1;
		int bottomColor = colorsNbt.contains("bottomColor", net.minecraft.nbt.NbtElement.INT_TYPE) ? colorsNbt.getInt("bottomColor") : -1;

		LOGGER.debug("[ITEM] Found colors: top={}, bottom={}", topColor, bottomColor);

		if (topColor == -1 && bottomColor == -1) return null;
		return new ShulkerColors(topColor, bottomColor);
	}

	/**
	 * Remove colors from an ItemStack's stored block entity data.
	 */
	private void removeColorsFromItemStack(ItemStack stack) {
		var beData = stack.get(net.minecraft.component.DataComponentTypes.BLOCK_ENTITY_DATA);
		if (beData == null) return;

		var nbt = beData.copyNbt();
		if (!nbt.contains("fabric:attachments", net.minecraft.nbt.NbtElement.COMPOUND_TYPE)) return;

		var attachments = nbt.getCompound("fabric:attachments");
		String key = MOD_ID + ":colors";
		if (attachments.contains(key)) {
			attachments.remove(key);
			// Put the modified attachments back into nbt
			if (attachments.isEmpty()) {
				nbt.remove("fabric:attachments");
			} else {
				nbt.put("fabric:attachments", attachments);
			}
			// Update the item's block entity data
			stack.set(net.minecraft.component.DataComponentTypes.BLOCK_ENTITY_DATA,
				net.minecraft.component.type.NbtComponent.of(nbt));
			LOGGER.info("[CAULDRON] Successfully removed colors from item NBT");
		}
	}
}

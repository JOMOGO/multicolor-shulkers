package com.multicolorshulkers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
//? if MC: <12102 {
/*import net.minecraft.util.ItemActionResult;
*///?}
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

	//? if MC: >=12102 {
	public static final net.minecraft.recipe.RecipeSerializer<com.multicolorshulkers.recipe.DualDyeShulkerRecipe> DUAL_DYE_SERIALIZER = new net.minecraft.recipe.SpecialCraftingRecipe.SpecialRecipeSerializer<>(com.multicolorshulkers.recipe.DualDyeShulkerRecipe::new);
	//?} else {
	/*public static final net.minecraft.recipe.RecipeSerializer<com.multicolorshulkers.recipe.DualDyeShulkerRecipe> DUAL_DYE_SERIALIZER = new net.minecraft.recipe.SpecialRecipeSerializer<>(com.multicolorshulkers.recipe.DualDyeShulkerRecipe::new);
	*///?}

	public static final AttachmentType<ShulkerColors> SHULKER_COLORS = AttachmentRegistry.createPersistent(
		Identifier.of(MOD_ID, "colors"),
		ShulkerColors.CODEC
	);

	// Server-side tracking of colored shulker positions per dimension
	private static final Map<RegistryKey<World>, Set<BlockPos>> COLORED_SHULKERS = new ConcurrentHashMap<>();

	// Players waiting for color sync (processed next tick to avoid blocking join)
	private static final Set<ServerPlayerEntity> PENDING_SYNCS = ConcurrentHashMap.newKeySet();

	private static Set<BlockPos> getColoredShulkers(RegistryKey<World> dimension) {
		return COLORED_SHULKERS.computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet());
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Dual-Dye Shulkers initialized!");
		LOGGER.info("Registering recipe serializer: {}:dual_dye", MOD_ID);
		net.minecraft.registry.Registry.register(net.minecraft.registry.Registries.RECIPE_SERIALIZER, Identifier.of(MOD_ID, "dual_dye"), DUAL_DYE_SERIALIZER);
		LOGGER.info("Recipe serializer registered successfully");

		// Register packets
		PayloadTypeRegistry.playS2C().register(ColorSyncPayload.ID, ColorSyncPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(DyeRequestPayload.ID, DyeRequestPayload.CODEC);

		// Handle dye request from client
		ServerPlayNetworking.registerGlobalReceiver(DyeRequestPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			ServerWorld world = player.getServerWorld();
			BlockPos pos = payload.pos();
			boolean colorBottom = payload.colorBottom();

			// Verify player is close enough to the block
			if (!player.getBlockPos().isWithinDistance(pos, 6)) {
				return;
			}

			// Verify block is a shulker box
			if (!(world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock)) {
				return;
			}

			if (!(world.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity shulkerBox)) {
				return;
			}

			// Get the dye from player's hand
			ItemStack mainHand = player.getMainHandStack();
			ItemStack offHand = player.getOffHandStack();
			ItemStack heldItem = null;
			net.minecraft.util.Hand hand = null;

			if (mainHand.getItem() instanceof DyeItem) {
				heldItem = mainHand;
				hand = net.minecraft.util.Hand.MAIN_HAND;
			} else if (offHand.getItem() instanceof DyeItem) {
				heldItem = offHand;
				hand = net.minecraft.util.Hand.OFF_HAND;
			}

			if (heldItem == null || !(heldItem.getItem() instanceof DyeItem dyeItem)) {
				return;
			}

			int dyeColor = dyeItem.getColor().getId();

			// Get current colors or create default
			ShulkerColors currentColors = shulkerBox.getAttachedOrCreate(SHULKER_COLORS, () -> ShulkerColors.DEFAULT);
			ShulkerColors newColors;

			if (colorBottom) {
				newColors = currentColors.withBottomColor(dyeColor);
				LOGGER.debug("[DYE] Setting bottom color to {} ({})", dyeItem.getColor().getName(), dyeColor);
			} else {
				newColors = currentColors.withTopColor(dyeColor);
				LOGGER.debug("[DYE] Setting top color to {} ({})", dyeItem.getColor().getName(), dyeColor);
			}

			// Set the new colors
			shulkerBox.setAttached(SHULKER_COLORS, newColors);
			shulkerBox.markDirty();

			// Track and sync to all nearby players
			getColoredShulkers(world.getRegistryKey()).add(pos);
			syncColorsToClients(world, pos, newColors);

			// Consume dye in survival
			if (!player.getAbilities().creativeMode) {
				heldItem.decrement(1);
			}

			// Swing arm
			player.swingHand(hand, true);
		});

		// Register cauldron behavior to wash off custom colors from shulker box items
		registerCauldronBehavior();

		// Track colored shulker positions when block entities load
		// We just track the position here - actual sync happens on player join or when colors change
		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
			if (!(world instanceof ServerWorld serverWorld)) return;
			if (!(blockEntity instanceof ShulkerBoxBlockEntity shulkerBox)) return;

			// Check colors directly - attachments are already loaded at this point
			ShulkerColors colors = shulkerBox.getAttached(SHULKER_COLORS);
			BlockPos pos = blockEntity.getPos();

			if (colors != null && (colors.topColor() != -1 || colors.bottomColor() != -1)) {
				// Has custom colors - track it
				getColoredShulkers(serverWorld.getRegistryKey()).add(pos);
			}
		});

		// Remove from tracking when shulker box is unloaded/broken
		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
			if (!(world instanceof ServerWorld serverWorld)) return;
			if (!(blockEntity instanceof ShulkerBoxBlockEntity)) return;

			// Just remove from tracking - client cache will be cleared naturally or on rejoin
			BlockPos pos = blockEntity.getPos();
			getColoredShulkers(serverWorld.getRegistryKey()).remove(pos);
		});

		// Sync all nearby colored shulkers when a player joins (delayed to avoid blocking join)
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			// Store player reference for delayed sync
			PENDING_SYNCS.add(player);
		});

		// Process pending syncs each tick (safer than during join)
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (PENDING_SYNCS.isEmpty()) return;

			for (ServerPlayerEntity player : PENDING_SYNCS) {
				if (player.isDisconnected()) continue;

				ServerWorld world = player.getServerWorld();
				BlockPos playerPos = player.getBlockPos();
				int radius = 64;

				Set<BlockPos> coloredPositions = getColoredShulkers(world.getRegistryKey());
				for (BlockPos pos : coloredPositions) {
					if (playerPos.isWithinDistance(pos, radius)) {
						if (world.getBlockEntity(pos) instanceof ShulkerBoxBlockEntity shulkerBox) {
							ShulkerColors colors = shulkerBox.getAttached(SHULKER_COLORS);
							if (colors != null && (colors.topColor() != -1 || colors.bottomColor() != -1)) {
								ColorSyncPayload payload = new ColorSyncPayload(pos, colors.topColor(), colors.bottomColor());
								ServerPlayNetworking.send(player, payload);
								LOGGER.debug("[JOIN] Synced colors for {} to {}", pos, player.getName().getString());
							}
						}
					}
				}
			}
			PENDING_SYNCS.clear();
		});

		// Note: The actual dye application is handled via DyeRequestPayload from client
		// This callback now just returns PASS to let the client handle the interaction
		// The client will check config settings and send the appropriate packet
	}


	public static void syncColorsToClients(ServerWorld world, BlockPos pos, ShulkerColors colors) {
		ColorSyncPayload payload = new ColorSyncPayload(pos, colors.topColor(), colors.bottomColor());

		// Send to all players tracking this block
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (player.getBlockPos().isWithinDistance(pos, 64)) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}

    public static void trackShulker(ServerWorld world, BlockPos pos) {
        getColoredShulkers(world.getRegistryKey()).add(pos);
    }

	public static void clearColors(ServerWorld world, BlockPos pos) {
		// Remove from tracking
		getColoredShulkers(world.getRegistryKey()).remove(pos);

		// Send clear packet (colors -1, -1) to nearby clients
		ColorSyncPayload payload = new ColorSyncPayload(pos, -1, -1);
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (player.getBlockPos().isWithinDistance(pos, 64)) {
				ServerPlayNetworking.send(player, payload);
			}
		}
		LOGGER.debug("[SERVER] Cleared colors for {}", pos);
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
						LOGGER.debug("[CAULDRON] Removing custom colors from item");
						removeColorsFromItemStack(stack);

						// Decrement cauldron water level
						LeveledCauldronBlock.decrementFluidLevel(state, world, pos);

						// Stats
						player.incrementStat(Stats.CLEAN_SHULKER_BOX);
					}
					//? if MC: >=12102 {
					return ActionResult.SUCCESS;
					//?} else {
					/*return ItemActionResult.SUCCESS;
					*///?}
				}

				// No custom colors - delegate to original vanilla behavior
				if (originalBehavior != null) {
					return originalBehavior.interact(state, world, pos, player, hand, stack);
				}
				//? if MC: >=12102 {
				return ActionResult.PASS;
				//?} else {
				/*return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				*///?}
			};

			CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().put(shulkerItem, washShulkerColors);
		}
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
	public static void removeColorsFromItemStack(ItemStack stack) {
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
			LOGGER.debug("[CAULDRON] Removed custom colors from item");
		}
	}
}

package com.multicolorshulkers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiColorShulkers implements ModInitializer {
	public static final String MOD_ID = "multicolor-shulkers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final String TOP_COLOR_KEY = "TopColor";
	public static final String BOTTOM_COLOR_KEY = "BottomColor";

	@Override
	public void onInitialize() {
		LOGGER.info("Multi-Color Shulker Boxes initialized!");

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClient) return ActionResult.PASS;

			var blockState = world.getBlockState(hitResult.getBlockPos());
			var block = blockState.getBlock();

			if (!(block instanceof ShulkerBoxBlock)) {
				return ActionResult.PASS;
			}

			ItemStack heldItem = player.getStackInHand(hand);

			if (!(heldItem.getItem() instanceof DyeItem dyeItem)) {
				return ActionResult.PASS;
			}

			var blockEntity = world.getBlockEntity(hitResult.getBlockPos());
			if (!(blockEntity instanceof ShulkerBoxBlockEntity)) return ActionResult.PASS;

			int dyeColor = dyeItem.getColor().getId();
			ShulkerBoxColorAccessor accessor = (ShulkerBoxColorAccessor) blockEntity;

			if (player.isSneaking()) {
				accessor.multiColorShulkers$setBottomColor(dyeColor);
			} else {
				accessor.multiColorShulkers$setTopColor(dyeColor);
			}

			blockEntity.markDirty();
			world.updateListeners(hitResult.getBlockPos(), blockState, blockState, 3);

			if (!player.getAbilities().creativeMode) {
				heldItem.decrement(1);
			}

			return ActionResult.SUCCESS;
		});
	}
}

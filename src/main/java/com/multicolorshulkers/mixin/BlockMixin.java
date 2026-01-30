package com.multicolorshulkers.mixin;

import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "onPlaced", at = @At("RETURN"))
    public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (world.isClient) return;

        Block block = state.getBlock();
        if (block instanceof ShulkerBoxBlock) {
            ShulkerColors colors = MultiColorShulkers.getColorsFromItemStack(itemStack);
            if (colors != null && (colors.topColor() != -1 || colors.bottomColor() != -1)) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBox) {
                    // Ensure colors are set on the block entity
                    shulkerBox.setAttached(MultiColorShulkers.SHULKER_COLORS, colors);
                    shulkerBox.markDirty();

                    if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                        // Track and sync
                        MultiColorShulkers.trackShulker(serverWorld, pos);
                        MultiColorShulkers.syncColorsToClients(serverWorld, pos, colors);
                        MultiColorShulkers.LOGGER.debug("[PLACE] Restored and synced colors via BlockMixin: top={}, bottom={}", colors.topColor(), colors.bottomColor());
                    }
                }
            }
        }
    }
}

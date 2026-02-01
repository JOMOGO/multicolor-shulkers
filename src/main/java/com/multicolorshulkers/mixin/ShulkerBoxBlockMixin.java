package com.multicolorshulkers.mixin;

import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {

    @Inject(method = "getDroppedStacks", at = @At("RETURN"))
    private void onGetDroppedStacks(BlockState state, LootWorldContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        // Get the block entity from the loot context
        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (!(blockEntity instanceof ShulkerBoxBlockEntity shulkerBox)) {
            return;
        }

        // Get the colors from the block entity
        ShulkerColors colors = shulkerBox.getAttached(MultiColorShulkers.SHULKER_COLORS);
        if (colors == null || (colors.topColor() == -1 && colors.bottomColor() == -1)) {
            return;
        }

        MultiColorShulkers.LOGGER.debug("[DROP] Found colors on block entity: top={}, bottom={}", colors.topColor(), colors.bottomColor());

        // Add the colors to each dropped item's block entity data
        List<ItemStack> drops = cir.getReturnValue();
        for (ItemStack stack : drops) {
            if (stack.getItem() instanceof net.minecraft.item.BlockItem blockItem) {
                if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    addColorsToItemStack(stack, colors);
                    MultiColorShulkers.LOGGER.debug("[DROP] Added colors to dropped item");
                }
            }
        }
    }

    private void addColorsToItemStack(ItemStack stack, ShulkerColors colors) {
        // Get or create the block entity data component
        NbtComponent beData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        NbtCompound nbt;
        if (beData != null) {
            nbt = beData.copyNbt();
        } else {
            nbt = new NbtCompound();
            // Need to set the block entity ID for the NBT to be valid
            nbt.putString("id", "minecraft:shulker_box");
        }

        // Get or create the fabric:attachments compound
        NbtCompound attachments;
        if (nbt.contains("fabric:attachments", NbtElement.COMPOUND_TYPE)) {
            attachments = nbt.getCompound("fabric:attachments");
        } else {
            attachments = new NbtCompound();
            nbt.put("fabric:attachments", attachments);
        }

        // Add our colors
        NbtCompound colorsNbt = new NbtCompound();
        colorsNbt.putInt("topColor", colors.topColor());
        colorsNbt.putInt("bottomColor", colors.bottomColor());
        attachments.put(MultiColorShulkers.MOD_ID + ":colors", colorsNbt);

        // Set the updated block entity data
        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
    }

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    private void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (state.isOf(newState.getBlock())) {
            return;
        }

        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBox) {
                ShulkerColors colors = shulkerBox.getAttached(MultiColorShulkers.SHULKER_COLORS);
                if (colors != null && (colors.topColor() != -1 || colors.bottomColor() != -1)) {
                     MultiColorShulkers.clearColors(serverWorld, pos);
                }
            }
        }
        }


    @Redirect(method = "onBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean onCreativeBreakDrop(World instance, Entity entity, World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // This redirects the world.spawnEntity call in onBreak (used for Creative mode drops)
        if (entity instanceof ItemEntity itemEntity) {
             BlockEntity blockEntity = world.getBlockEntity(pos);
             if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBox) {
                ShulkerColors colors = shulkerBox.getAttached(MultiColorShulkers.SHULKER_COLORS);
                if (colors != null && (colors.topColor() != -1 || colors.bottomColor() != -1)) {
                     addColorsToItemStack(itemEntity.getStack(), colors);
                     MultiColorShulkers.LOGGER.debug("[CREATIVE BREAK] Added colors to dropped filled shulker box");
                }
             }
        }
        return instance.spawnEntity(entity);
    }
}


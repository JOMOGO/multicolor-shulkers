//? if MC: >=12100 && MC: <12102 {
/*package com.multicolorshulkers.mixin;

import com.multicolorshulkers.MultiColorShulkers;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShulkerBoxColoringRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxColoringRecipe.class)
public class ShulkerBoxColoringRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z", at = @At("HEAD"), cancellable = true)
    private void blockVanillaWhenDyeInTopOrBottom(CraftingRecipeInput input, World world, CallbackInfoReturnable<Boolean> cir) {
        if (!com.multicolorshulkers.client.ModConfig.get().enableCrafting) return;

        int width = input.getWidth();
        int height = input.getHeight();

        MultiColorShulkers.LOGGER.debug("[MIXIN 1.21.1] Checking ShulkerBoxColoringRecipe: width={}, height={}", width, height);

        if (height < 3) return;

        int shulkerCount = 0;
        int dyeCount = 0;
        int dyeRow = -1;

        for (int i = 0; i < input.getSize(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) {
                shulkerCount++;
            } else if (stack.getItem() instanceof DyeItem) {
                dyeCount++;
                dyeRow = i / width;
            }
        }

        if (shulkerCount == 1 && dyeCount == 1) {
            // Block vanilla recipe if dye is in Top (0) or Bottom (height-1) row
            if (dyeRow == 0 || dyeRow == height - 1) {
                MultiColorShulkers.LOGGER.debug("[MIXIN 1.21.1] Blocking vanilla - dye in row {} (top=0, bottom={})", dyeRow, height - 1);
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void stripCustomColorsFromOutput(CraftingRecipeInput input, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty() && Block.getBlockFromItem(result.getItem()) instanceof ShulkerBoxBlock) {
             MultiColorShulkers.removeColorsFromItemStack(result);
        }
    }
}
*///?}

//? if MC: >=12102 {
package com.multicolorshulkers.mixin;

// In 1.21.2+, ShulkerBoxColoringRecipe was replaced by data-driven TransmuteRecipe.
// We block TransmuteRecipe for shulker+dye combinations so our custom recipe handles them.

import com.multicolorshulkers.MultiColorShulkers;
import net.minecraft.recipe.TransmuteRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransmuteRecipe.class)
public class ShulkerBoxColoringRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z", at = @At("HEAD"), cancellable = true)
    private void blockVanillaShulkerDye(CraftingRecipeInput input, World world, CallbackInfoReturnable<Boolean> cir) {
        if (!com.multicolorshulkers.client.ModConfig.get().enableCrafting) return;

        // TransmuteRecipe is used for shulker dyeing in 1.21.2+
        // We block it when the input is a shulker + dye combination
        // This allows our custom DualDyeShulkerRecipe to handle the crafting instead

        boolean hasShulker = false;
        boolean hasDye = false;

        for (int i = 0; i < input.size(); i++) {
            var stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (net.minecraft.block.Block.getBlockFromItem(stack.getItem()) instanceof net.minecraft.block.ShulkerBoxBlock) {
                hasShulker = true;
            } else if (stack.getItem() instanceof net.minecraft.item.DyeItem) {
                hasDye = true;
            }
        }

        // If this is a shulker + dye recipe, block it so our custom recipe handles it
        if (hasShulker && hasDye) {
            MultiColorShulkers.LOGGER.info("[MIXIN] Blocking TransmuteRecipe for shulker+dye combination");
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void stripCustomColorsFromOutput(CraftingRecipeInput input, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, CallbackInfoReturnable<net.minecraft.item.ItemStack> cir) {
        net.minecraft.item.ItemStack result = cir.getReturnValue();
        if (!result.isEmpty() && net.minecraft.block.Block.getBlockFromItem(result.getItem()) instanceof net.minecraft.block.ShulkerBoxBlock) {
             MultiColorShulkers.removeColorsFromItemStack(result);
        }
    }
}
//?}

//? if MC: <12100 {
/*package com.multicolorshulkers.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShulkerBoxColoringRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import net.minecraft.inventory.RecipeInputInventory;

@Mixin(ShulkerBoxColoringRecipe.class)
public class ShulkerBoxColoringRecipeMixin {

    @Inject(method = "matches(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/world/World;)Z", at = @At("HEAD"), cancellable = true, require = 0)
    private void blockVanillaWhenDyeInTopOrBottom(RecipeInputInventory input, World world, CallbackInfoReturnable<Boolean> cir) {
        int width = input.getWidth();
        int height = input.getHeight();

        if (height < 3) return;

        int shulkerCount = 0;
        int dyeCount = 0;
        int dyeRow = -1;

        List<ItemStack> stacks = new java.util.ArrayList<>();
        for(int i = 0; i < input.size(); i++) stacks.add(input.getStack(i));

        int size = stacks.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) continue;

            if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) {
                shulkerCount++;
            } else if (stack.getItem() instanceof DyeItem) {
                dyeCount++;
                dyeRow = i / width;
            }
        }

        if (shulkerCount == 1 && dyeCount == 1) {
            // Block vanilla recipe if dye is in Top (0) or Bottom (height-1) row
            if (dyeRow == 0 || dyeRow == height - 1) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "craft(Lnet/minecraft/inventory/RecipeInputInventory;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private void stripCustomColorsFromOutput(RecipeInputInventory input, net.minecraft.registry.RegistryWrapper.WrapperLookup lookup, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (!result.isEmpty() && Block.getBlockFromItem(result.getItem()) instanceof ShulkerBoxBlock) {
             MultiColorShulkers.removeColorsFromItemStack(result);
        }
    }
}
*///?}

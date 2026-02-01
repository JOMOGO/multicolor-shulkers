package com.multicolorshulkers.recipe;

import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;
import net.minecraft.util.DyeColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;

//? if MC: >=12100 {
import net.minecraft.recipe.input.CraftingRecipeInput;
//?} else {
/*import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.registry.DynamicRegistryManager;
*///?}

import java.util.List;

public class DualDyeShulkerRecipe extends SpecialCraftingRecipe {
    public DualDyeShulkerRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    //? if MC: >=12100 {

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        MultiColorShulkers.LOGGER.debug("[RECIPE] matches() called - width={}, height={}, stackCount={}",
            input.getWidth(), input.getHeight(), input.getStacks().size());
        boolean result = matchesShared(input.getWidth(), input.getHeight(), input.getStacks());
        MultiColorShulkers.LOGGER.debug("[RECIPE] matches() returning: {}", result);
        return result;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, WrapperLookup registries) {
        MultiColorShulkers.LOGGER.debug("[RECIPE] craft() called");
        ItemStack result = craftShared(input.getWidth(), input.getHeight(), input.getStacks());
        MultiColorShulkers.LOGGER.debug("[RECIPE] craft() returning: {}", result);
        return result;
    }

    //?} else {

    /*// Support both CraftingInventory and RecipeInputInventory for legacy (1.20.x)

    public boolean matches(RecipeInputInventory input, World world) {
        return matchesShared(input.getWidth(), input.getHeight(), toList(input));
    }

    public boolean matches(CraftingInventory input, World world) {
        return matchesShared(input.getWidth(), input.getHeight(), toList(input));
    }

    // craft might take WrapperLookup (1.20.5+) or DynamicRegistryManager (1.20.4-)
    public ItemStack craft(RecipeInputInventory input, WrapperLookup registries) {
        return craftShared(input.getWidth(), input.getHeight(), toList(input));
    }

    public ItemStack craft(CraftingInventory input, WrapperLookup registries) {
        return craftShared(input.getWidth(), input.getHeight(), toList(input));
    }

    public ItemStack craft(RecipeInputInventory input, DynamicRegistryManager registryManager) {
        return craftShared(input.getWidth(), input.getHeight(), toList(input));
    }

    public ItemStack craft(CraftingInventory input, DynamicRegistryManager registryManager) {
        return craftShared(input.getWidth(), input.getHeight(), toList(input));
    }

    private List<ItemStack> toList(RecipeInputInventory inv) {
        List<ItemStack> stacks = new java.util.ArrayList<>();
        for(int i = 0; i < inv.size(); i++) {
            stacks.add(inv.getStack(i));
        }
        return stacks;
    }
    *///?}

    //? if MC: <12102 {
    /*@Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }
    *///?}

    private boolean matchesShared(int width, int height, List<ItemStack> stacks) {
        ItemStack shulkerStack = ItemStack.EMPTY;
        int dyeCount = 0;

        MultiColorShulkers.LOGGER.debug("[RECIPE] matchesShared called: width={}, height={}, stacks={}", width, height, stacks.size());

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;

            if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) {
                if (!shulkerStack.isEmpty()) {
                    MultiColorShulkers.LOGGER.debug("[RECIPE] Multiple shulkers found, returning false");
                    return false;
                }
                shulkerStack = stack;
            } else if (stack.getItem() instanceof DyeItem) {
                dyeCount++;
            } else {
                MultiColorShulkers.LOGGER.debug("[RECIPE] Non-shulker/non-dye item found: {}, returning false", stack.getItem());
                return false;
            }
        }
        boolean result = !shulkerStack.isEmpty() && dyeCount > 0;
        if (result) {
            MultiColorShulkers.LOGGER.info("[RECIPE] DualDyeShulkerRecipe MATCHED: shulker found, dyeCount={}", dyeCount);
        }
        return result;
    }

    private ItemStack craftShared(int width, int height, List<ItemStack> stacks) {
         ItemStack shulkerStack = ItemStack.EMPTY;
         for (ItemStack stack : stacks) {
             if (!stack.isEmpty() && Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) {
                 shulkerStack = stack;
                 break;
             }
         }

         if (shulkerStack.isEmpty()) return ItemStack.EMPTY;

         ShulkerColors currentColors = MultiColorShulkers.getColorsFromItemStack(shulkerStack);
         if (currentColors == null) currentColors = ShulkerColors.DEFAULT;

         int baseColorId = getBaseColor(shulkerStack);
         int resolvedTop = currentColors.topColor();
         int resolvedBottom = currentColors.bottomColor();

         if (resolvedTop == -1) resolvedTop = baseColorId;
         if (resolvedBottom == -1) resolvedBottom = baseColorId;

         int newTopColor = -1;
         int newBottomColor = -1;
         int vanillaColor = -1;

         int size = stacks.size();

         if (height == 3) {
             // CRAFTING TABLE MODE - items span all 3 rows
             // Use absolute row positions - shulker position doesn't matter
             //   Dye in row 0 (top) → colors top only
             //   Dye in row 1 (middle) → colors whole box (vanilla)
             //   Dye in row 2 (bottom) → colors bottom only
             for (int i = 0; i < size; i++) {
                 ItemStack stack = stacks.get(i);
                 if (stack.isEmpty() || !(stack.getItem() instanceof DyeItem dye)) continue;

                 int dyeRow = i / width;
                 int colorId = dye.getColor().getId();

                 if (dyeRow == 0) {
                     newTopColor = colorId;
                 } else if (dyeRow == 2) {
                     newBottomColor = colorId;
                 } else {
                     vanillaColor = colorId;
                 }
             }
         } else {
             // RELATIVE MODE - hand crafting (2x2) or crafting table when items don't span 3 rows
             // Use position relative to shulker
             //   Dye above shulker → colors top only
             //   Dye below shulker → colors bottom only
             //   Dye same row as shulker → colors whole box (vanilla)

             // Find shulker row
             int shulkerRow = -1;
             for (int i = 0; i < size; i++) {
                 ItemStack stack = stacks.get(i);
                 if (!stack.isEmpty() && Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock) {
                     shulkerRow = i / width;
                     break;
                 }
             }

             // Determine dye colors based on position relative to shulker
             for (int i = 0; i < size; i++) {
                 ItemStack stack = stacks.get(i);
                 if (stack.isEmpty() || !(stack.getItem() instanceof DyeItem dye)) continue;

                 int dyeRow = i / width;
                 int colorId = dye.getColor().getId();

                 if (dyeRow < shulkerRow) {
                     newTopColor = colorId;
                 } else if (dyeRow > shulkerRow) {
                     newBottomColor = colorId;
                 } else {
                     vanillaColor = colorId;
                 }
             }
         }

         // Apply vanilla color first (colors whole box)
         if (vanillaColor != -1) {
             resolvedTop = vanillaColor;
             resolvedBottom = vanillaColor;
         }

         // Then apply position-specific colors (can override vanilla)
         if (newTopColor != -1) resolvedTop = newTopColor;
         if (newBottomColor != -1) resolvedBottom = newBottomColor;

         ItemStack resultStack;
         if (resolvedTop == resolvedBottom && resolvedTop != -1) {
             Item shulkerItem = ShulkerBoxBlock.getItemStack(DyeColor.byId(resolvedTop)).getItem();
             resultStack = new ItemStack(shulkerItem);
         } else {
             resultStack = shulkerStack.copy();
             resultStack.setCount(1);
         }

         if (resolvedTop != resolvedBottom) {
             ShulkerColors newColors = new ShulkerColors(resolvedTop, resolvedBottom);
             applyColorsToStack(resultStack, newColors);
         } else if (resultStack != shulkerStack) {
             removeColorsFromItemStack(resultStack);
         }

         return resultStack;
    }

    private void applyColorsToStack(ItemStack stack, ShulkerColors colors) {
        NbtComponent beData = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = beData.copyNbt();

        // Ensure the block entity ID is set (required for valid block entity data)
        if (!nbt.contains("id")) {
            nbt.putString("id", "minecraft:shulker_box");
        }

        NbtCompound attachments = nbt.contains("fabric:attachments") ? nbt.getCompound("fabric:attachments") : new NbtCompound();
        NbtCompound colorNbt = new NbtCompound();
        colorNbt.putInt("topColor", colors.topColor());
        colorNbt.putInt("bottomColor", colors.bottomColor());

        attachments.put(MultiColorShulkers.MOD_ID + ":colors", colorNbt);
        nbt.put("fabric:attachments", attachments);

        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
    }

    private void removeColorsFromItemStack(ItemStack stack) {
         NbtComponent beData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
         if (beData != null) {
              NbtCompound nbt = beData.copyNbt();
              if (nbt.contains("fabric:attachments")) {
                  NbtCompound attachments = nbt.getCompound("fabric:attachments");
                  attachments.remove(MultiColorShulkers.MOD_ID + ":colors");
                  if (attachments.isEmpty()) nbt.remove("fabric:attachments");
                  stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
              }
         }
    }

    private int getBaseColor(ItemStack stack) {
        if (Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock shulkerBlock) {
             if (shulkerBlock.getColor() != null) return shulkerBlock.getColor().getId();
        }
        return -1;
    }

    @Override
    //? if MC: >=12102 {
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
    //?} else {
    /*public RecipeSerializer<?> getSerializer() {
    *///?}
        return MultiColorShulkers.DUAL_DYE_SERIALIZER;
    }
}

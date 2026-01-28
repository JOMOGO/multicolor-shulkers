package com.multicolorshulkers.client;

import com.multicolorshulkers.MultiColorShulkers;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

import java.util.List;

public class ShulkerBoxTooltipCallback {

	public static void addTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> tooltip) {
		if (!(stack.getItem() instanceof BlockItem blockItem)) return;
		if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) return;

		var nbt = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
		if (nbt == null) return;

		var data = nbt.getNbt();
		if (data == null) return;

		boolean hasTopColor = data.contains(MultiColorShulkers.TOP_COLOR_KEY);
		boolean hasBottomColor = data.contains(MultiColorShulkers.BOTTOM_COLOR_KEY);

		if (hasTopColor || hasBottomColor) {
			tooltip.add(Text.empty());
			tooltip.add(Text.literal("Custom Colors:").formatted(Formatting.GRAY, Formatting.ITALIC));

			if (hasTopColor) {
				int topColorId = data.getInt(MultiColorShulkers.TOP_COLOR_KEY);
				DyeColor dyeColor = DyeColor.byId(topColorId);
				String colorName = formatColorName(dyeColor.getName());
				tooltip.add(Text.literal("  Top: ").formatted(Formatting.GRAY)
						.append(Text.literal(colorName).formatted(getFormattingForDye(dyeColor))));
			}

			if (hasBottomColor) {
				int bottomColorId = data.getInt(MultiColorShulkers.BOTTOM_COLOR_KEY);
				DyeColor dyeColor = DyeColor.byId(bottomColorId);
				String colorName = formatColorName(dyeColor.getName());
				tooltip.add(Text.literal("  Bottom: ").formatted(Formatting.GRAY)
						.append(Text.literal(colorName).formatted(getFormattingForDye(dyeColor))));
			}
		}
	}

	private static String formatColorName(String name) {
		String[] words = name.replace('_', ' ').split(" ");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (!word.isEmpty()) {
				result.append(Character.toUpperCase(word.charAt(0)))
						.append(word.substring(1).toLowerCase())
						.append(" ");
			}
		}
		return result.toString().trim();
	}

	private static Formatting getFormattingForDye(DyeColor color) {
		return switch (color) {
			case WHITE -> Formatting.WHITE;
			case ORANGE -> Formatting.GOLD;
			case MAGENTA, PINK -> Formatting.LIGHT_PURPLE;
			case LIGHT_BLUE -> Formatting.AQUA;
			case YELLOW -> Formatting.YELLOW;
			case LIME -> Formatting.GREEN;
			case GRAY -> Formatting.DARK_GRAY;
			case LIGHT_GRAY -> Formatting.GRAY;
			case CYAN -> Formatting.DARK_AQUA;
			case PURPLE -> Formatting.DARK_PURPLE;
			case BLUE -> Formatting.BLUE;
			case BROWN -> Formatting.GOLD;
			case GREEN -> Formatting.DARK_GREEN;
			case RED -> Formatting.RED;
			case BLACK -> Formatting.DARK_GRAY;
		};
	}
}

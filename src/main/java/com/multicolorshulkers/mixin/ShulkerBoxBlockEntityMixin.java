package com.multicolorshulkers.mixin;

import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.ShulkerBoxColorAccessor;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin implements ShulkerBoxColorAccessor {

	@Unique
	private int topColor = -1;

	@Unique
	private int bottomColor = -1;

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void writeCustomNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		if (topColor != -1) {
			nbt.putInt(MultiColorShulkers.TOP_COLOR_KEY, topColor);
		}
		if (bottomColor != -1) {
			nbt.putInt(MultiColorShulkers.BOTTOM_COLOR_KEY, bottomColor);
		}
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void readCustomNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
		if (nbt.contains(MultiColorShulkers.TOP_COLOR_KEY)) {
			topColor = nbt.getInt(MultiColorShulkers.TOP_COLOR_KEY);
		}
		if (nbt.contains(MultiColorShulkers.BOTTOM_COLOR_KEY)) {
			bottomColor = nbt.getInt(MultiColorShulkers.BOTTOM_COLOR_KEY);
		}
	}

	@Override
	public int multiColorShulkers$getTopColor() {
		return topColor;
	}

	@Override
	public int multiColorShulkers$getBottomColor() {
		return bottomColor;
	}

	@Override
	public void multiColorShulkers$setTopColor(int color) {
		this.topColor = color;
	}

	@Override
	public void multiColorShulkers$setBottomColor(int color) {
		this.bottomColor = color;
	}
}

package com.multicolorshulkers.mixin;

import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import com.multicolorshulkers.client.MultiColorShulkersClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.SpecialItemModel;
import net.minecraft.client.render.item.model.special.ShulkerBoxModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialItemModel.class)
public class SpecialItemModelMixin<T> {

    @Shadow
    @Final
    private SpecialModelRenderer<T> specialModelType;

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdate(ItemRenderState renderState, ItemStack stack, ItemModelManager itemModelManager,
                          ModelTransformationMode transformationMode, ClientWorld world,
                          LivingEntity user, int seed, CallbackInfo ci) {
        // Only process for shulker box renderers
        if (this.specialModelType instanceof ShulkerBoxModelRenderer) {
            ShulkerColors colors = MultiColorShulkers.getColorsFromItemStack(stack);
            MultiColorShulkersClient.setItemColors(colors);
        }
    }
}

package com.multicolorshulkers.mixin;

//? if MC: >=12102 {
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

// Stub mixin for 1.21.2+ - BuiltinModelItemRenderer was removed, uses SpecialModelTypes now
@Mixin(ItemRenderer.class)
public class BuiltinModelItemRendererMixin {
    // No-op for 1.21.2+ - item rendering handled by ShulkerBoxModelRendererMixin and SpecialItemModelMixin
}
//?} else {
/*
import com.multicolorshulkers.MultiColorShulkers;
import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import com.multicolorshulkers.client.MultiColorShulkersClient;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        // Only process shulker box items
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) return;

        // Extract colors from the item stack and set them in ThreadLocal
        // The ShulkerBoxBlockEntityRendererMixin will pick these up
        ShulkerColors colors = MultiColorShulkers.getColorsFromItemStack(stack);
        if (colors != null && (colors.topColor() != -1 || colors.bottomColor() != -1)) {
            MultiColorShulkersClient.setItemColors(colors);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                                VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        // Clean up the ThreadLocal after rendering
        MultiColorShulkersClient.clearItemColors();
    }
}
*///?}

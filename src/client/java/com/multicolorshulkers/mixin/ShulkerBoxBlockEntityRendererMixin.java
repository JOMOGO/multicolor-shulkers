package com.multicolorshulkers.mixin;

import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import com.multicolorshulkers.client.MultiColorShulkersClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntityRenderer.class)
public class ShulkerBoxBlockEntityRendererMixin {

    @Shadow
    @Final
    private ShulkerBoxBlockEntityRenderer.ShulkerBoxBlockModel model;

    @Inject(method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRender(ShulkerBoxBlockEntity shulkerBox, float tickDelta, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        // Check if we have custom colors for this shulker box
        ShulkerColors colors = MultiColorShulkersClient.getColors(shulkerBox.getPos());

        if (colors == null || (colors.topColor() == -1 && colors.bottomColor() == -1)) {
            // No custom colors, let the original render proceed
            return;
        }

        // Cancel the original render and do our own
        ci.cancel();

        // Get the facing direction
        Direction direction = Direction.UP;
        BlockState blockState = shulkerBox.getCachedState();
        if (blockState.contains(ShulkerBoxBlock.FACING)) {
            direction = blockState.get(ShulkerBoxBlock.FACING);
        }

        // Get the base color of the shulker box (for texture selection)
        // Note: getColor() returns null for undyed shulker boxes
        DyeColor baseColor = shulkerBox.getColor();
        SpriteIdentifier spriteIdentifier;
        if (baseColor == null) {
            // Use the default undyed shulker box texture
            spriteIdentifier = TexturedRenderLayers.SHULKER_TEXTURE_ID;
        } else {
            spriteIdentifier = TexturedRenderLayers.getShulkerBoxTextureId(baseColor);
        }

        // Calculate openness
        float animationProgress = shulkerBox.getAnimationProgress(tickDelta);

        matrices.push();

        // Position the model
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(0.9995F, 0.9995F, 0.9995F);

        // Apply rotation based on facing
        matrices.multiply(direction.getRotationQuaternion());
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0, -1.0, 0.0);

        // Animate the lid
        this.model.animateLid(animationProgress);

        // Get model parts - lid is a direct field, base is accessed from root
        ModelPart lidPart = this.model.lid;
        ModelPart rootPart = this.model.root;

        // Get the render layer
        VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);

        // Determine colors
        int topColor = getColorInt(colors.topColor(), baseColor);
        int bottomColor = getColorInt(colors.bottomColor(), baseColor);

        // Try to get the base part from the root
        // The shulker model structure typically has "base" and "lid" children
        ModelPart basePart = null;
        try {
            basePart = rootPart.getChild("base");
        } catch (Exception e) {
            // Base part not found, we'll render differently
        }

        if (basePart != null) {
            // Render base and lid separately with different colors
            basePart.render(matrices, vertexConsumer, light, overlay, bottomColor);
            lidPart.render(matrices, vertexConsumer, light, overlay, topColor);
        } else {
            // Fallback: If we can't separate the parts, render the whole model
            // with a blended approach - render root with bottom color, then lid with top color
            // First, hide the lid, render the root with bottom color
            boolean lidVisible = lidPart.visible;
            lidPart.visible = false;
            rootPart.render(matrices, vertexConsumer, light, overlay, bottomColor);
            lidPart.visible = lidVisible;

            // Then render just the lid with top color
            lidPart.render(matrices, vertexConsumer, light, overlay, topColor);
        }

        matrices.pop();
    }

    private int getColorInt(int colorId, DyeColor fallbackColor) {
        if (colorId == -1) {
            // Use the original shulker box color
            if (fallbackColor == null) {
                return 0xFFFFFFFF; // White for default/undyed shulker
            }
            return getDyeColorRgb(fallbackColor);
        }
        // Convert dye color ID to RGB
        DyeColor dyeColor = DyeColor.byId(colorId);
        return getDyeColorRgb(dyeColor);
    }

    private int getDyeColorRgb(DyeColor dyeColor) {
        // Get the color components and create ARGB int
        // DyeColor.getEntityColor() returns the RGB int for entity tinting
        int rgb = dyeColor.getEntityColor();
        // Add full alpha
        return 0xFF000000 | rgb;
    }
}

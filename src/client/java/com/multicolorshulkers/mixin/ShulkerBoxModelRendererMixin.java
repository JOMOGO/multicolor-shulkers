package com.multicolorshulkers.mixin;

import com.multicolorshulkers.MultiColorShulkers.ShulkerColors;
import com.multicolorshulkers.client.MultiColorShulkersClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.render.item.model.special.ShulkerBoxModelRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxModelRenderer.class)
public class ShulkerBoxModelRendererMixin {

    @Shadow
    @Final
    private ShulkerBoxBlockEntityRenderer blockEntityRenderer;

    @Shadow
    @Final
    private float openness;

    @Shadow
    @Final
    private Direction orientation;

    @Shadow
    @Final
    private SpriteIdentifier textureId;

    // Intercept render to apply dual colors
    @Inject(method = "render(Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onRender(ModelTransformationMode mode, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, int overlay,
                          boolean useGlint, CallbackInfo ci) {
        ShulkerColors colors = MultiColorShulkersClient.getItemColors();
        MultiColorShulkersClient.clearItemColors(); // Clean up

        if (colors == null || (colors.topColor() == -1 && colors.bottomColor() == -1)) {
            // No custom colors, use default rendering
            return;
        }

        ci.cancel();
        renderWithDualColors(matrices, vertexConsumers, light, overlay, colors);
    }

    @Unique
    private void renderWithDualColors(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                      int light, int overlay, ShulkerColors colors) {
        // Get base color from the textureId (for fallback when only one color is set)
        DyeColor baseColor = getBaseColorFromTexture();

        // Get the actual DyeColor for top and bottom
        DyeColor topDyeColor = getDyeColorFromId(colors.topColor(), baseColor);
        DyeColor bottomDyeColor = getDyeColorFromId(colors.bottomColor(), baseColor);

        // Use the actual vanilla colored textures
        SpriteIdentifier topTexture = getShulkerTexture(topDyeColor);
        SpriteIdentifier bottomTexture = getShulkerTexture(bottomDyeColor);

        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.scale(0.9995f, 0.9995f, 0.9995f);
        matrices.multiply(this.orientation.getRotationQuaternion());
        matrices.scale(1.0f, -1.0f, -1.0f);
        matrices.translate(0.0f, -1.0f, 0.0f);

        // Animate lid
        ShulkerBoxBlockEntityRenderer.ShulkerBoxBlockModel model = this.blockEntityRenderer.model;
        model.animateLid(this.openness);

        ModelPart lidPart = model.lid;
        ModelPart rootPart = model.root;

        // Get vertex consumers for each colored texture
        VertexConsumer topVertexConsumer = topTexture.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);
        VertexConsumer bottomVertexConsumer = bottomTexture.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);

        // Try to get base part
        ModelPart basePart = null;
        try {
            basePart = rootPart.getChild("base");
        } catch (Exception e) {
            // Base part not found
        }

        // Render with white tint (0xFFFFFFFF) since textures are already colored
        if (basePart != null) {
            basePart.render(matrices, bottomVertexConsumer, light, overlay, 0xFFFFFFFF);
            lidPart.render(matrices, topVertexConsumer, light, overlay, 0xFFFFFFFF);
        } else {
            // Fallback: hide lid, render base, show lid, render lid
            boolean lidVisible = lidPart.visible;
            lidPart.visible = false;
            rootPart.render(matrices, bottomVertexConsumer, light, overlay, 0xFFFFFFFF);
            lidPart.visible = lidVisible;
            lidPart.render(matrices, topVertexConsumer, light, overlay, 0xFFFFFFFF);
        }

        matrices.pop();
    }

    @Unique
    private DyeColor getBaseColorFromTexture() {
        // Try to determine base color from the textureId
        // This is a bit hacky but necessary since we only have the texture
        for (int i = 0; i < 16; i++) {
            DyeColor color = DyeColor.byId(i);
            SpriteIdentifier coloredTexture = TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(i);
            if (coloredTexture.equals(this.textureId)) {
                return color;
            }
        }
        // Undyed shulker
        return null;
    }

    @Unique
    private DyeColor getDyeColorFromId(int colorId, DyeColor fallbackColor) {
        if (colorId == -1) {
            return fallbackColor; // Can be null for undyed shulker
        }
        return DyeColor.byId(colorId);
    }

    @Unique
    private SpriteIdentifier getShulkerTexture(DyeColor color) {
        if (color == null) {
            // Undyed shulker box
            return TexturedRenderLayers.SHULKER_TEXTURE_ID;
        }
        // Use the colored shulker texture list indexed by color ID
        return TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(color.getId());
    }
}

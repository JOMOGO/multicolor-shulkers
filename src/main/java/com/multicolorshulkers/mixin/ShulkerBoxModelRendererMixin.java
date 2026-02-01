package com.multicolorshulkers.mixin;

//? if MC: >=12102 {
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

    @Shadow @Final private ShulkerBoxBlockEntityRenderer blockEntityRenderer;
    @Shadow @Final private float openness;
    @Shadow @Final private Direction orientation;
    @Shadow @Final private SpriteIdentifier textureId;

    @Inject(method = "render(Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IIZ)V",
            at = @At("HEAD"), cancellable = true)
    private void onRender(ModelTransformationMode mode, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, int overlay,
                          boolean useGlint, CallbackInfo ci) {
        ShulkerColors colors = MultiColorShulkersClient.getItemColors();
        MultiColorShulkersClient.clearItemColors();
        if (colors == null || (colors.topColor() == -1 && colors.bottomColor() == -1)) return;
        ci.cancel();
        renderWithDualColors(matrices, vertexConsumers, light, overlay, colors);
    }

    @Unique
    private void renderWithDualColors(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, ShulkerColors colors) {
        DyeColor baseColor = getBaseColorFromTexture();
        DyeColor topDyeColor = getDyeColorFromId(colors.topColor(), baseColor);
        DyeColor bottomDyeColor = getDyeColorFromId(colors.bottomColor(), baseColor);
        SpriteIdentifier topTexture = getShulkerTexture(topDyeColor);
        SpriteIdentifier bottomTexture = getShulkerTexture(bottomDyeColor);

        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.scale(0.9995f, 0.9995f, 0.9995f);
        matrices.multiply(this.orientation.getRotationQuaternion());
        matrices.scale(1.0f, -1.0f, -1.0f);
        matrices.translate(0.0f, -1.0f, 0.0f);

        ShulkerBoxBlockEntityRenderer.ShulkerBoxBlockModel model = this.blockEntityRenderer.model;
        model.animateLid(this.openness);
        ModelPart lidPart = model.lid;
        ModelPart rootPart = model.root;

        VertexConsumer topVC = topTexture.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);
        VertexConsumer bottomVC = bottomTexture.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);

        ModelPart basePart = null;
        try { basePart = rootPart.getChild("base"); } catch (Exception e) {}

        if (basePart != null) {
            basePart.render(matrices, bottomVC, light, overlay, 0xFFFFFFFF);
            lidPart.render(matrices, topVC, light, overlay, 0xFFFFFFFF);
        } else {
            boolean lidVisible = lidPart.visible;
            lidPart.visible = false;
            rootPart.render(matrices, bottomVC, light, overlay, 0xFFFFFFFF);
            lidPart.visible = lidVisible;
            lidPart.render(matrices, topVC, light, overlay, 0xFFFFFFFF);
        }
        matrices.pop();
    }

    @Unique private DyeColor getBaseColorFromTexture() {
        for (int i = 0; i < 16; i++) {
            DyeColor color = DyeColor.byId(i);
            if (TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(i).equals(this.textureId)) return color;
        }
        return null;
    }

    @Unique private DyeColor getDyeColorFromId(int colorId, DyeColor fallback) {
        return colorId == -1 ? fallback : DyeColor.byId(colorId);
    }

    @Unique private SpriteIdentifier getShulkerTexture(DyeColor color) {
        return color == null ? TexturedRenderLayers.SHULKER_TEXTURE_ID : TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(color.getId());
    }
}
//?} else {
/*
import net.minecraft.client.render.item.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;

// Stub mixin for 1.21.1 - ShulkerBoxModelRenderer doesn't exist
@Mixin(ItemRenderer.class)
public class ShulkerBoxModelRendererMixin {
    // No-op for 1.21.1 - item rendering handled by BuiltinModelItemRendererMixin
}
*///?}

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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntityRenderer.class)
public class ShulkerBoxBlockEntityRendererMixin {

    //? if MC: >=12102 {
    @Shadow @Final public ShulkerBoxBlockEntityRenderer.ShulkerBoxBlockModel model;
    //?} else {
    /*@Shadow @Final private net.minecraft.client.render.entity.model.ShulkerEntityModel<?> model;
    *///?}

    @Inject(method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At("HEAD"), cancellable = true)
    private void onRender(ShulkerBoxBlockEntity shulkerBox, float tickDelta, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
        // First check for block colors (placed shulker boxes)
        ShulkerColors colors = MultiColorShulkersClient.getColors(shulkerBox.getPos());
        // If no block colors, check for item colors (item rendering via ThreadLocal)
        if (colors == null) {
            colors = MultiColorShulkersClient.getItemColors();
        }
        if (colors == null || (colors.topColor() == -1 && colors.bottomColor() == -1)) return;
        ci.cancel();
        renderWithColors(shulkerBox, tickDelta, matrices, vertexConsumers, light, overlay, colors);
    }

    @Unique
    private void renderWithColors(ShulkerBoxBlockEntity shulkerBox, float tickDelta, MatrixStack matrices,
                                  VertexConsumerProvider vertexConsumers, int light, int overlay, ShulkerColors colors) {
        Direction direction = Direction.UP;
        BlockState blockState = shulkerBox.getCachedState();
        if (blockState.contains(ShulkerBoxBlock.FACING)) {
            direction = blockState.get(ShulkerBoxBlock.FACING);
        }

        DyeColor baseColor = shulkerBox.getColor();
        DyeColor topDyeColor = colors.topColor() == -1 ? baseColor : DyeColor.byId(colors.topColor());
        DyeColor bottomDyeColor = colors.bottomColor() == -1 ? baseColor : DyeColor.byId(colors.bottomColor());
        SpriteIdentifier topTexture = topDyeColor == null ? TexturedRenderLayers.SHULKER_TEXTURE_ID : TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(topDyeColor.getId());
        SpriteIdentifier bottomTexture = bottomDyeColor == null ? TexturedRenderLayers.SHULKER_TEXTURE_ID : TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(bottomDyeColor.getId());

        float animationProgress = shulkerBox.getAnimationProgress(tickDelta);

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(0.9995F, 0.9995F, 0.9995F);
        matrices.multiply(direction.getRotationQuaternion());
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0, -1.0, 0.0);

        VertexConsumer topVC = topTexture.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);
        VertexConsumer bottomVC = bottomTexture.getVertexConsumer(vertexConsumers, RenderLayer::getEntityCutoutNoCull);

        //? if MC: >=12102 {
        this.model.animateLid(animationProgress);
        ModelPart lidPart = this.model.lid;
        ModelPart rootPart = this.model.root;

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
        //?} else {
        /*ShulkerEntityModelAccessor accessor = (ShulkerEntityModelAccessor) this.model;
        ModelPart lidPart = accessor.getLid();
        ModelPart basePart = accessor.getBase();
        // Animate the lid - pivotY moves up as the box opens
        lidPart.pitch = 0;
        lidPart.pivotY = 24.0F - animationProgress * 0.5F * 16.0F;
        accessor.getHead().pivotY = 24.0F - animationProgress * 0.5F * 16.0F;

        // Render base with bottom color, lid with top color
        basePart.render(matrices, bottomVC, light, overlay, 0xFFFFFFFF);
        lidPart.render(matrices, topVC, light, overlay, 0xFFFFFFFF);
        *///?}
        matrices.pop();
    }
}

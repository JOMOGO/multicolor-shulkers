package com.multicolorshulkers.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerEntityModel.class)
public interface ShulkerEntityModelAccessor {
    @Accessor("lid")
    ModelPart getLid();

    @Accessor("head")
    ModelPart getHead();
}

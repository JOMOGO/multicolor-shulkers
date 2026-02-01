package com.multicolorshulkers.mixin;

//? if MC: >=12102 {
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import org.spongepowered.asm.mixin.Mixin;

// Stub mixin for 1.21.2+ - ShulkerBoxBlockEntityRenderer uses ShulkerBoxBlockModel directly
@Mixin(ShulkerEntityModel.class)
public interface ShulkerEntityModelAccessor {
    // Accessors not needed for 1.21.2+ but mixin must be valid
}
//?} else {
/*
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerEntityModel.class)
public interface ShulkerEntityModelAccessor {
    @Accessor("lid")
    ModelPart getLid();

    @Accessor("base")
    ModelPart getBase();

    @Accessor("head")
    ModelPart getHead();
}
*///?}

package com.multicolorshulkers;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ColorSyncPayload(BlockPos pos, int topColor, int bottomColor) implements CustomPayload {

    public static final CustomPayload.Id<ColorSyncPayload> ID =
        new CustomPayload.Id<>(Identifier.of(MultiColorShulkers.MOD_ID, "color_sync"));

    public static final PacketCodec<RegistryByteBuf, ColorSyncPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, ColorSyncPayload::pos,
        PacketCodecs.INTEGER, ColorSyncPayload::topColor,
        PacketCodecs.INTEGER, ColorSyncPayload::bottomColor,
        ColorSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

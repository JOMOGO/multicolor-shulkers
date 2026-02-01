package com.multicolorshulkers;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Client-to-server packet to request dyeing a shulker box.
 * This allows the client to decide whether to color the top or bottom based on keybind state.
 */
public record DyeRequestPayload(BlockPos pos, boolean colorBottom) implements CustomPayload {

	public static final CustomPayload.Id<DyeRequestPayload> ID =
		new CustomPayload.Id<>(Identifier.of(MultiColorShulkers.MOD_ID, "dye_request"));

	//? if MC: >=12102 {
	public static final PacketCodec<RegistryByteBuf, DyeRequestPayload> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, DyeRequestPayload::pos,
		PacketCodecs.BOOLEAN, DyeRequestPayload::colorBottom,
		DyeRequestPayload::new
	);
	//?} else {
	/*public static final PacketCodec<RegistryByteBuf, DyeRequestPayload> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, DyeRequestPayload::pos,
		PacketCodecs.BOOL, DyeRequestPayload::colorBottom,
		DyeRequestPayload::new
	);
	*///?}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}

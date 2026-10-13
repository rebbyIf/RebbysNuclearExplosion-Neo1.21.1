package net.rebby.rebbys_nuclear_explosion.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record NukeData(int age, int r, Vector3f pos, int id, boolean remove) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NukeData> TYPE =
            new Type<>(RebbysNuclearExplosion.getResource("nuke_data"));
    public static final StreamCodec<ByteBuf, NukeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            NukeData::age,
            ByteBufCodecs.VAR_INT,
            NukeData::r,
            ByteBufCodecs.VECTOR3F,
            NukeData::pos,
            ByteBufCodecs.VAR_INT,
            NukeData::id,
            ByteBufCodecs.BOOL,
            NukeData::remove,
            NukeData::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

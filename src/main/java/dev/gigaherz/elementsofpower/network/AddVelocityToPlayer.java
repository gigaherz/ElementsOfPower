package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddVelocityToPlayer(
        double vx,
        double vy,
        double vz
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("add_velocity_to_player");
    public static final Type<AddVelocityToPlayer> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, AddVelocityToPlayer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, AddVelocityToPlayer::vx,
            ByteBufCodecs.DOUBLE, AddVelocityToPlayer::vy,
            ByteBufCodecs.DOUBLE, AddVelocityToPlayer::vz,
            AddVelocityToPlayer::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientPacketHandlers.handleAddVelocityPlayer(this);
    }
}

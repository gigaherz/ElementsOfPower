package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class AddVelocityToPlayer implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("add_velocity_to_player");

    public double vx;
    public double vy;
    public double vz;

    public AddVelocityToPlayer(double vx, double vy, double vz)
    {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public AddVelocityToPlayer(FriendlyByteBuf buf)
    {
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientPacketHandlers.handleAddVelocityPlayer(this);
    }
}

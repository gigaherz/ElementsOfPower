package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AddVelocityToPlayer
{
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

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleAddVelocityPlayer(this);
    }
}

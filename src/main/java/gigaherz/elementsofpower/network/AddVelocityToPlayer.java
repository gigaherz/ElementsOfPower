package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.client.ClientPacketHandlers;
import gigaherz.elementsofpower.client.ClientProxy;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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

    public AddVelocityToPlayer(PacketBuffer buf)
    {
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
    }

    public void encode(PacketBuffer buf)
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

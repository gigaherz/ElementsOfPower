package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.Used;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class AddVelocityPlayer
        implements IMessage
{
    public double vx;
    public double vy;
    public double vz;

    @Used
    public AddVelocityPlayer()
    {
    }

    public AddVelocityPlayer(double vx, double vy, double vz)
    {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        vx = buf.readDouble();
        vy = buf.readDouble();
        vz = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeDouble(vx);
        buf.writeDouble(vy);
        buf.writeDouble(vz);
    }

    public static class Handler implements IMessageHandler<AddVelocityPlayer, IMessage>
    {
        @Nullable
        @Override
        public IMessage onMessage(AddVelocityPlayer message, MessageContext ctx)
        {
            ElementsOfPower.proxy.handleAddVelocity(message);

            return null; // no response in this case
        }
    }
}

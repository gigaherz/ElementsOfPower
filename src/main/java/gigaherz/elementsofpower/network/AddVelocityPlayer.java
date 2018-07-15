package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.common.Used;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
            return ActualHandler.SUPPLIER.get().apply(message);
        }
    }

    private static class ActualHandler
    {
        public static final Supplier<Function<AddVelocityPlayer, IMessage>> SUPPLIER = () -> ActualHandler::handle;

        public static IMessage handle(AddVelocityPlayer message)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
                    Minecraft.getMinecraft().player.addVelocity(message.vx, message.vy, message.vz));
            return null;
        }
    }
}

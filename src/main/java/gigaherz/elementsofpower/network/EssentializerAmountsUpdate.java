package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.Used;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class EssentializerAmountsUpdate
        implements IMessage
{
    public int windowId;
    public MagicAmounts contained;
    public MagicAmounts remaining;

    @Used
    public EssentializerAmountsUpdate()
    {
    }

    public EssentializerAmountsUpdate(int windowId, TileEssentializer essentializer)
    {
        this.windowId = windowId;
        this.contained = essentializer.containedMagic;
        this.remaining = essentializer.remainingToConvert;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        contained = new MagicAmounts(buf);
        remaining = new MagicAmounts(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        contained.writeTo(buf);
        remaining.writeTo(buf);
    }

    public static class Handler implements IMessageHandler<EssentializerAmountsUpdate, IMessage>
    {
        @Nullable
        @Override
        public IMessage onMessage(EssentializerAmountsUpdate message, MessageContext ctx)
        {
            ElementsOfPower.proxy.handleRemainingAmountsUpdate(message);

            return null; // no response in this case
        }
    }
}

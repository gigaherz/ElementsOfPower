package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.Used;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
        this.contained = MagicAmounts.copyOf(essentializer.containedMagic);
        this.remaining = MagicAmounts.copyOf(essentializer.remainingToConvert);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        contained = readAmounts(buf);
        remaining = readAmounts(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        writeAmounts(buf, contained);
        writeAmounts(buf, remaining);
    }

    private MagicAmounts readAmounts(ByteBuf buf)
    {
        MagicAmounts amounts = null;
        int numElements = buf.readByte();
        if (numElements > 0)
        {
            amounts = new MagicAmounts();
            for (int i = 0; i < numElements; i++)
            {
                int which = buf.readByte();
                float amount = buf.readFloat();
                amounts.amounts[which] = amount;
            }
        }
        return amounts;
    }

    private void writeAmounts(ByteBuf buf, MagicAmounts amounts)
    {
        if (amounts != null)
        {
            int count = 0;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                if (amounts.amounts[i] > 0) count++;
            }

            buf.writeByte(count);

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                if (amounts.amounts[i] > 0)
                {
                    buf.writeByte(i);
                    buf.writeFloat(amounts.amounts[i]);
                }
            }
        }
        else
        {
            buf.writeByte(0);
        }
    }

    public static class Handler implements IMessageHandler<EssentializerAmountsUpdate, IMessage>
    {
        @Override
        public IMessage onMessage(EssentializerAmountsUpdate message, MessageContext ctx)
        {
            ElementsOfPower.proxy.handleRemainingAmountsUpdate(message);

            return null; // no response in this case
        }
    }
}

package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.common.Used;
import gigaherz.elementsofpower.items.ItemWand;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SpellSequenceUpdate
        implements IMessage
{
    public enum ChangeMode
    {
        BEGIN,
        PARTIAL,
        COMMIT,
        CANCEL;
        public static final ChangeMode values[] = values();
    }

    public int slotNumber;

    public ChangeMode changeMode;
    public String sequence;

    @Used
    public SpellSequenceUpdate()
    {
    }

    public SpellSequenceUpdate(ChangeMode mode, int slotNumber, @Nullable String sequence)
    {
        changeMode = mode;
        this.sequence = sequence;
        this.slotNumber = slotNumber;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int r = buf.readInt();
        changeMode = ChangeMode.values[r];
        slotNumber = buf.readByte();
        sequence = ByteBufUtils.readUTF8String(buf);
        if (sequence.length() == 0)
        {
            sequence = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeByte(slotNumber);
        if (sequence != null)
        {
            ByteBufUtils.writeUTF8String(buf, sequence);
        }
        else
        {
            ByteBufUtils.writeUTF8String(buf, "");
        }
    }

    public static class Handler implements IMessageHandler<SpellSequenceUpdate, IMessage>
    {
        @Nullable
        @Override
        public IMessage onMessage(SpellSequenceUpdate message, MessageContext ctx)
        {
            final SpellSequenceUpdate msg = message;
            final EntityPlayer player = ctx.getServerHandler().playerEntity;
            final WorldServer ws = (WorldServer) player.worldObj;

            ws.addScheduledTask(() -> {
                ItemStack stack = player.inventory.mainInventory[msg.slotNumber];

                if (stack != null && stack.getItem() instanceof ItemWand)
                {
                    ItemWand wand = (ItemWand) stack.getItem();
                    wand.processSequenceUpdate(msg, stack, player);
                }
            });

            return null; // no response in this case
        }
    }
}

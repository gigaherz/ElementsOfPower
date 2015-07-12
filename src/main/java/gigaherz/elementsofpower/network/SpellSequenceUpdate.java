package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.items.ItemWand;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    ;

    public int dimension;
    public EntityPlayer entity;
    public int slotNumber;

    public ChangeMode changeMode;
    public String sequence;

    public SpellSequenceUpdate()
    {
    }

    public SpellSequenceUpdate(ChangeMode mode, EntityPlayer entity, int slotNumber, String sequence)
    {
        changeMode = mode;
        this.entity = entity;
        this.dimension = entity.dimension;
        this.sequence = sequence;
        this.slotNumber = slotNumber;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {

        dimension = buf.readInt();
        changeMode = ChangeMode.values[buf.readInt()];
        entity = (EntityPlayer) MinecraftServer.getServer().worldServerForDimension(dimension).getEntityByID(buf.readInt());
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

        buf.writeInt(dimension);
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(entity.getEntityId());
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

        @Override
        public IMessage onMessage(SpellSequenceUpdate message, MessageContext ctx)
        {

            final SpellSequenceUpdate msg = message;

            WorldServer ws = (WorldServer) message.entity.worldObj;
            ws.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {

                    ItemStack stack = msg.entity.inventory.mainInventory[msg.slotNumber];

                    if (stack != null && stack.getItem() instanceof ItemWand)
                    {
                        ItemWand wand = (ItemWand) stack.getItem();
                        wand.processSequenceUpdate(msg, stack);
                    }
                }
            });


            return null; // no response in this case
        }
    }
}

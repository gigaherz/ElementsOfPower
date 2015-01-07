package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ItemWand;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public class SpellSequenceUpdate
        implements IMessage {

    public enum ChangeMode {
        BEGIN,
        PARTIAL,
        COMMIT,
        CANCEL;
        public static final ChangeMode values[] = values();
    };

    public int dimension;
    public EntityPlayer entity;
    public int slotNumber;

    public ChangeMode changeMode;
    public List<Byte> sequence;

    public SpellSequenceUpdate() {
    }

    public SpellSequenceUpdate(ChangeMode mode, EntityPlayer entity, int slotNumber, List<Byte> sequence) {
        changeMode = mode;
        this.entity = entity;
        this.dimension = entity.dimension;
        this.sequence = sequence;
        this.slotNumber = slotNumber;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        dimension = buf.readInt();
        changeMode = ChangeMode.values[buf.readInt()];
        entity = (EntityPlayer)MinecraftServer.getServer().worldServerForDimension(dimension).getEntityByID(buf.readInt());
        int seqSize = buf.readInt();
        if(seqSize > 0) {
            sequence = new ArrayList<Byte>();
            for (int i = 0; i < seqSize; i++) {
                sequence.add(buf.readByte());
            }
        } else {
            sequence = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {

        buf.writeInt(dimension);
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(entity.getEntityId());
        if(sequence != null) {
            int seqSize = sequence.size();
            buf.writeInt(seqSize);
            for (int i = 0; i < seqSize; i++) {
                buf.writeByte(sequence.get(i));
            }
        } else {
            buf.writeInt(0);
        }
    }

    public static class Handler implements IMessageHandler<SpellSequenceUpdate, IMessage> {

        @Override
        public IMessage onMessage(SpellSequenceUpdate message, MessageContext ctx) {

            ItemStack stack = message.entity.inventory.mainInventory[message.slotNumber];

            if(stack != null && stack.getItem() instanceof ItemWand) {
                ItemWand wand = (ItemWand)stack.getItem();
                wand.processSequenceUpdate(message, stack);
            }

            return null; // no response in this case
        }
    }
}

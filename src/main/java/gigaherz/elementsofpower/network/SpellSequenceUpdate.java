package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.common.Used;
import gigaherz.elementsofpower.items.ItemWand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SpellSequenceUpdate
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

    public SpellSequenceUpdate(ChangeMode mode, int slotNumber, @Nullable String sequence)
    {
        changeMode = mode;
        this.sequence = sequence;
        this.slotNumber = slotNumber;
    }

    public SpellSequenceUpdate(PacketBuffer buf)
    {
        int r = buf.readInt();
        changeMode = ChangeMode.values[r];
        slotNumber = buf.readByte();
        sequence = buf.readString();
        if (sequence.length() == 0)
        {
            sequence = null;
        }
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeByte(slotNumber);
        if (sequence != null)
        {
            buf.writeString(sequence);
        }
        else
        {
            buf.writeString("");
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().enqueueWork(() ->
        {
            ServerPlayerEntity player = context.get().getSender();
            ItemStack stack = player.inventory.getStackInSlot(slotNumber);

            if (stack.getItem() instanceof ItemWand)
            {
                ItemWand wand = (ItemWand) stack.getItem();
                wand.processSequenceUpdate(this, stack, player);
            }
        });

        return true;
    }
}

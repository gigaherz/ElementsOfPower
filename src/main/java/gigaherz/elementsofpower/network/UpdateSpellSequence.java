package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.items.WandItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class UpdateSpellSequence
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

    public UpdateSpellSequence(ChangeMode mode, int slotNumber, @Nullable String sequence)
    {
        changeMode = mode;
        this.sequence = sequence;
        this.slotNumber = slotNumber;
    }

    public UpdateSpellSequence(PacketBuffer buf)
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

            if (stack.getItem() instanceof WandItem)
            {
                WandItem wand = (WandItem) stack.getItem();
                wand.processSequenceUpdate(this, stack, player);
            }
        });

        return true;
    }
}

package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateSpellSequence
{
    public enum ChangeMode
    {
        BEGIN,
        PARTIAL,
        COMMIT,
        CANCEL;
        public static final ChangeMode[] values = values();
    }

    public int slotNumber;

    public ChangeMode changeMode;
    public List<Element> sequence;
    private final int useTicks;

    public UpdateSpellSequence(ChangeMode mode, int slotNumber, @Nullable List<Element> sequence, int useTicks)
    {
        changeMode = mode;
        this.sequence = sequence;
        this.slotNumber = slotNumber;
        this.useTicks = useTicks;
    }

    public UpdateSpellSequence(FriendlyByteBuf buf)
    {
        int r = buf.readInt();
        changeMode = ChangeMode.values[r];
        slotNumber = buf.readByte();
        useTicks = buf.readVarInt();
        int count = buf.readVarInt();
        if (count > 0)
        {
            sequence = new ArrayList<>();
            for (int i = 0; i < count; i++)
            {sequence.add(Element.byName(buf.readUtf(100)));}
        }
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeByte(slotNumber);
        buf.writeVarInt(useTicks);
        if (sequence != null)
        {
            buf.writeVarInt(sequence.size());
            for (Element e : sequence)
            {buf.writeUtf(e.getName());}
        }
        else
        {
            buf.writeVarInt(0);
        }
    }

    public void handle(NetworkEvent.Context context)
    {
        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();
            ItemStack stack = Objects.requireNonNull(player).getInventory().getItem(slotNumber);

            if (stack.getItem() instanceof WandItem)
            {
                WandItem wand = (WandItem) stack.getItem();
                wand.processSequenceUpdate(this, stack, player, useTicks);
            }
        });
    }
}

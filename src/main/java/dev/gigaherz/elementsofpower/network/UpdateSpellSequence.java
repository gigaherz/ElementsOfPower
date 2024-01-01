package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateSpellSequence implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("update_spell_sequence");

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
        this.sequence = sequence != null ? List.copyOf(sequence) : null;
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

    public void write(FriendlyByteBuf buf)
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

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        context.workHandler().execute(() ->
        {
            Player player = context.player().orElseThrow();
            ItemStack stack = player.getInventory().getItem(slotNumber);

            if (stack.getItem() instanceof WandItem wand)
            {
                wand.processSequenceUpdate(this, stack, player, useTicks);
            }
        });
    }
}

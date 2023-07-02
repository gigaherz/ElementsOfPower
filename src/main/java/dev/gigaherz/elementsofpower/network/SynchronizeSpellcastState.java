package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SynchronizeSpellcastState
{
    public enum ChangeMode
    {
        BEGIN,
        END,
        INTERRUPT,
        CANCEL;
        public static final ChangeMode values[] = values();
    }

    public int casterID;
    public ChangeMode changeMode;
    public CompoundTag spellcast = new CompoundTag();

    public SynchronizeSpellcastState(ChangeMode mode, InitializedSpellcast cast)
    {
        changeMode = mode;
        cast.write(spellcast);
        casterID = cast.getCastingPlayer().getId();
    }

    public SynchronizeSpellcastState(FriendlyByteBuf buf)
    {
        changeMode = ChangeMode.values[buf.readInt()];
        casterID = buf.readInt();
        spellcast = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(casterID);
        buf.writeNbt(spellcast);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleSpellcastSync(this);
    }
}

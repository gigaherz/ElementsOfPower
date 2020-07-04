package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.client.ClientPacketHandlers;
import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

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
    public CompoundNBT spellcast = new CompoundNBT();

    public SynchronizeSpellcastState(ChangeMode mode, InitializedSpellcast cast)
    {
        changeMode = mode;
        cast.writeToNBT(spellcast);
        casterID = cast.getCastingPlayer().getEntityId();
    }

    public SynchronizeSpellcastState(PacketBuffer buf)
    {
        changeMode = ChangeMode.values[buf.readInt()];
        casterID = buf.readInt();
        spellcast = buf.readCompoundTag();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(casterID);
        buf.writeCompoundTag(spellcast);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleSpellcastSync(this);
    }
}

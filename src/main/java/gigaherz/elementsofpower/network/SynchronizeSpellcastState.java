package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.client.ClientPacketHandlers;
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
    public Spellcast spellcast;

    public SynchronizeSpellcastState(ChangeMode mode, Spellcast cast)
    {
        changeMode = mode;
        spellcast = cast;
        casterID = cast.getCastingPlayer().getEntityId();
    }

    public SynchronizeSpellcastState(PacketBuffer buf)
    {
        changeMode = ChangeMode.values[buf.readInt()];
        casterID = buf.readInt();

        CompoundNBT cast = buf.readCompoundTag();
        if (cast != null && cast.contains("sequence", Constants.NBT.TAG_LIST))
        {
            ListNBT seq = cast.getList("sequence", Constants.NBT.TAG_STRING);
            spellcast = SpellManager.makeSpell(seq);
            if (spellcast != null)
                spellcast.readFromNBT(cast);
        }
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(casterID);

        CompoundNBT tagData = new CompoundNBT();
        spellcast.writeToNBT(tagData);

        tagData.put("sequence", spellcast.getSequenceNBT());

        buf.writeCompoundTag(tagData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleSpellcastSync(this);
    }
}

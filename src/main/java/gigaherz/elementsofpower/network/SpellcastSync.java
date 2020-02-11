package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.client.ClientPacketHandlers;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.network.PacketBuffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SpellcastSync
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

    public SpellcastSync(ChangeMode mode, Spellcast cast)
    {
        changeMode = mode;
        spellcast = cast;
        casterID = cast.getCastingPlayer().getEntityId();
    }

    public SpellcastSync(PacketBuffer buf)
    {
        changeMode = ChangeMode.values[buf.readInt()];
        casterID = buf.readInt();

        CompoundNBT tagData = buf.readCompoundTag();
        String sequence = tagData.getString("sequence");

        spellcast = SpellManager.makeSpell(sequence);
        if (spellcast != null)
            spellcast.readFromNBT(tagData);
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(casterID);

        CompoundNBT tagData = new CompoundNBT();
        spellcast.writeToNBT(tagData);

        tagData.putString("sequence", spellcast.getSequence());

        buf.writeCompoundTag(tagData);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleSpellcastSync(this);
    }
}

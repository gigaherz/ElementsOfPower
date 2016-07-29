package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.Used;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class SpellcastSync
        implements IMessage
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

    @Used
    public SpellcastSync()
    {
    }

    public SpellcastSync(ChangeMode mode, Spellcast cast)
    {
        changeMode = mode;
        spellcast = cast;
        casterID = cast.getCastingPlayer().getEntityId();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        changeMode = ChangeMode.values[buf.readInt()];
        casterID = buf.readInt();

        NBTTagCompound tagData = ByteBufUtils.readTag(buf);
        String sequence = tagData.getString("sequence");

        spellcast = SpellManager.makeSpell(sequence);
        if (spellcast != null)
            spellcast.readFromNBT(tagData);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(casterID);

        NBTTagCompound tagData = new NBTTagCompound();
        spellcast.writeToNBT(tagData);

        tagData.setString("sequence", spellcast.getSequence());

        ByteBufUtils.writeTag(buf, tagData);
    }

    public static class Handler implements IMessageHandler<SpellcastSync, IMessage>
    {
        @Nullable
        @Override
        public IMessage onMessage(SpellcastSync message, MessageContext ctx)
        {
            ElementsOfPower.proxy.handleSpellcastSync(message);

            return null; // no response in this case
        }
    }
}

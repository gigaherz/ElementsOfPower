package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.spells.ISpellEffect;
import gigaherz.elementsofpower.spells.cast.ISpellcast;
import gigaherz.elementsofpower.util.Used;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
    public ISpellcast spellcast;

    @Used
    public SpellcastSync()
    {
    }

    public SpellcastSync(ChangeMode mode, ISpellcast cast)
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

        ISpellEffect ef = SpellManager.findSpell(sequence);

        spellcast = ef.getNewCast();
        spellcast.readFromNBT(tagData);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(casterID);

        NBTTagCompound tagData = new NBTTagCompound();
        spellcast.writeToNBT(tagData);

        tagData.setString("sequence", spellcast.getEffect().getSequence());

        ByteBufUtils.writeTag(buf, tagData);
    }

    public static class Handler implements IMessageHandler<SpellcastSync, IMessage>
    {
        @Override
        public IMessage onMessage(SpellcastSync message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() -> ElementsOfPower.proxy.handleSpellcastSync(message));

            return null; // no response in this case
        }
    }
}

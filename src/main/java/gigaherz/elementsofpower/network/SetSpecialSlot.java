package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.spells.ISpellEffect;
import gigaherz.elementsofpower.spells.ISpellcast;
import gigaherz.elementsofpower.util.Used;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetSpecialSlot
        implements IMessage
{
    public int windowId;
    public int slot;
    public ItemStack stack;

    @Used
    public SetSpecialSlot()
    {
    }

    public SetSpecialSlot(int windowId, int slot, ItemStack stack)
    {
        this.windowId = windowId;
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        windowId = buf.readInt();
        slot = buf.readInt();
        int count = buf.readInt();
        int meta = buf.readInt();
        Item item = Item.itemRegistry.getObject(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
        boolean hasTag = buf.readBoolean();
        stack = new ItemStack(item, count, meta);
        if(hasTag)
        {
            NBTTagCompound tag = ByteBufUtils.readTag(buf);
            stack.setTagCompound(tag);
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(windowId);
        buf.writeInt(slot);
        buf.writeInt(stack.stackSize);
        buf.writeInt(stack.getItemDamage());
        ByteBufUtils.writeUTF8String(buf, Item.itemRegistry.getNameForObject(stack.getItem()).toString());
        NBTTagCompound tag = stack.getTagCompound();
        buf.writeBoolean(tag != null);
        if(tag != null)
        {
            ByteBufUtils.writeTag(buf, tag);
        }
    }

    public static class Handler implements IMessageHandler<SetSpecialSlot, IMessage>
    {
        @Override
        public IMessage onMessage(SetSpecialSlot message, MessageContext ctx)
        {
            ElementsOfPower.proxy.handleSetSpecialSlot(message);

            return null; // no response in this case
        }
    }
}

package gigaherz.elementsofpower.blocks;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.database.MagicHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;

public class TileEssentializer
        extends TileEntity
        implements ISidedInventory, IUpdatePlayerListBox
{
    public final InventoryBasic inventory = new InventoryBasic("essentializer", false, 2);
    public final MagicHolder holder = new MagicHolder();

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
            byte slot = tag.getByte("Slot");

            if (slot < inventory.getSizeInventory())
            {
                inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
            }
        }

        holder.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);

        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack stack = inventory.getStackInSlot(i);

            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }

        tagCompound.setTag("Inventory", itemList);

        holder.writeToNBT(tagCompound);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(this.pos, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        super.onDataPacket(net, packet);
        readFromNBT(packet.getNbtCompound());
        this.worldObj.markBlockForUpdate(this.pos);
    }

    @Override
    public void update()
    {
        if (!worldObj.isRemote)
        {
            if (holder.processInventory(inventory))
                worldObj.markBlockForUpdate(getPos());
        }
    }

    // IInventory forwarders
    public String getName()
    {
        return inventory.getName();
    }

    public boolean hasCustomName()
    {
        return inventory.hasCustomName();
    }

    public IChatComponent getDisplayName()
    {
        return inventory.getDisplayName();
    }

    public int getSizeInventory()
    {
        return inventory.getSizeInventory();
    }

    public ItemStack getStackInSlot(int slotIndex)
    {
        return inventory.getStackInSlot(slotIndex);
    }

    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        inventory.setInventorySlotContents(slot, stack);
    }

    public ItemStack decrStackSize(int slotIndex, int amount)
    {
        return inventory.decrStackSize(slotIndex, amount);
    }

    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        return inventory.getStackInSlotOnClosing(slotIndex);
    }

    public int getInventoryStackLimit()
    {
        return inventory.getInventoryStackLimit();
    }

    public void clear() { inventory.clear(); }

    // Can't forward this one because it returns true always
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return (this.worldObj.getTileEntity(pos) == this)
                && (player.getDistanceSq(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5) < 64);
    }

    // Pointless inventory methods
    public int getField(int id)
    {
        return 0;
    }

    public void setField(int id, int value) {}

    public int getFieldCount()
    {
        return 0;
    }

    public void openInventory(EntityPlayer player) {}

    public void closeInventory(EntityPlayer player) {}

    // ISidedInventory
    public int[] getSlotsForFace(EnumFacing side)
    {
        return new int[]{0, 1};
    }

    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        switch(index)
        {
            case 0: return MagicDatabase.itemContainsMagic(stack) || MagicDatabase.itemHasEssence(stack);
            case 1: return MagicDatabase.canItemContainMagic(stack);
        }
        return false;
    }
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        return isItemValidForSlot(index, itemStackIn);
    }

    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        return true;
    }

}

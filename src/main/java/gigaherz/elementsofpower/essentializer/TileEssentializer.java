package gigaherz.elementsofpower.essentializer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.database.MagicHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;

public class TileEssentializer
        extends TileEntity
        implements ISidedInventory, ITickable
{
    public final InventoryBasic inventory = new InventoryBasic(ElementsOfPower.MODID + ".essentializer", false, 2);
    public final MagicHolder holder = new MagicHolder();

    public float renderTime;

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

        if(worldObj.isRemote)
        {
            renderTime ++;
        }
    }

    // IInventory forwarders
    @Override
    public String getName()
    {
        return inventory.getName();
    }

    @Override
    public boolean hasCustomName()
    {
        return inventory.hasCustomName();
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return inventory.getDisplayName();
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return inventory.getStackInSlot(slotIndex);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        inventory.setInventorySlotContents(slot, stack);
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount)
    {
        return inventory.decrStackSize(slotIndex, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int slotIndex)
    {
        return inventory.removeStackFromSlot(slotIndex);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return inventory.getInventoryStackLimit();
    }

    @Override
    public void clear()
    {
        inventory.clear();
    }

    // Can't forward this one because it returns true always
    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return (this.worldObj.getTileEntity(pos) == this)
                && (player.getDistanceSq(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5) < 64);
    }

    // Pointless inventory methods
    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    // ISidedInventory
    public int[] getSlotsForFace(EnumFacing side)
    {
        return new int[]{0, 1};
    }

    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        switch (index)
        {
            case 0:
                return MagicDatabase.itemContainsMagic(stack) || MagicDatabase.itemHasEssence(stack);
            case 1:
                return MagicDatabase.canItemContainMagic(stack);
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

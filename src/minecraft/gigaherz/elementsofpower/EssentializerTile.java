package gigaherz.elementsofpower;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import cpw.mods.fml.common.network.PacketDispatcher;

public class EssentializerTile extends TileEntity implements IInventory, ISidedInventory
{
    private ItemStack[] inventory;
	private int ticks;

    public EssentializerTile()
    {    	
        super();
        this.inventory = new ItemStack[24];
    }

    public void initialize()
    {
    }

    @Override
    public int getSizeInventory()
    {
        return this.inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        if (slotIndex >= this.inventory.length)
        {
            System.out.println("Tried to access slot " + slotIndex);
            return null;
        }

        return this.inventory[slotIndex];
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack)
    {
        this.inventory[slot] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit())
        {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int amount)
    {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack != null)
        {
            if (stack.stackSize <= amount)
            {
                setInventorySlotContents(slotIndex, null);
            }
            else
            {
                stack = stack.splitStack(amount);

                if (stack.stackSize == 0)
                {
                    setInventorySlotContents(slotIndex, null);
                }
            }
        }

        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        ItemStack stack = getStackInSlot(slotIndex);

        if (stack != null)
        {
            setInventorySlotContents(slotIndex, null);
        }

        return stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
    }

    @Override
    public void openChest() {}

    @Override
    public void closeChest() {}

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);

        // TODO: Save other variables ?
        
        NBTTagList tagList = tagCompound.getTagList("Inventory");

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
            byte slot = tag.getByte("Slot");

            if (slot >= 0 && slot < inventory.length)
            {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        
        // TODO: Save other variables ?
        
        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];

            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }

        tagCompound.setTag("Inventory", itemList);
    }

    @Override
    public String getInvName()
    {
        return "GrinderInventory";
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        
        if(this.ticks++ == 0)
        {
        	initialize();
        }

        if (this.worldObj.isRemote)
        {
            return;
        }

        boolean stateChanged = false;

        if (this.ticks == 1)
        {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.blockMetadata);
        }
        
        ForgeDirection inputDirection = ForgeDirection.getOrientation(this.getBlockMetadata() & 7);

        if (stateChanged)
        {
            this.onInventoryChanged();
        }
    }

    @Override
    public int getStartInventorySide(ForgeDirection side)
    {
        ForgeDirection left, right;

        switch (this.getBlockMetadata() & 7)
        {
            case 2: // North
                left = ForgeDirection.WEST;
                right = ForgeDirection.EAST;
                break;

            case 3: // South
                left = ForgeDirection.EAST;
                right = ForgeDirection.WEST;
                break;

            case 4: // West
                left = ForgeDirection.NORTH;
                right = ForgeDirection.SOUTH;
                break;

            case 5: // East
                left = ForgeDirection.SOUTH;
                right = ForgeDirection.NORTH;
                break;

            default:
                left = ForgeDirection.WEST;
                right = ForgeDirection.EAST;
                break;
        }

        if (side == left)
        {
            return 0;
        }

        if (side == right)
        {
            return 9;
        }

        if (side == ForgeDirection.UP)
        {
            return 18;
        }

        return 0;
    }

    @Override
    public int getSizeInventorySide(ForgeDirection side)
    {
        ForgeDirection left, right;

        switch (this.getBlockMetadata() & 7)
        {
            case 2: // North
                left = ForgeDirection.WEST;
                right = ForgeDirection.EAST;
                break;

            case 3: // South
                left = ForgeDirection.EAST;
                right = ForgeDirection.WEST;
                break;

            case 4: // West
                left = ForgeDirection.NORTH;
                right = ForgeDirection.SOUTH;
                break;

            case 5: // East
                left = ForgeDirection.SOUTH;
                right = ForgeDirection.NORTH;
                break;

            default:
                left = ForgeDirection.WEST;
                right = ForgeDirection.EAST;
                break;
        }

        if (side == left)
        {
            return 9;
        }

        if (side == right)
        {
            return 9;
        }

        if (side == ForgeDirection.UP)
        {
            return 3;
        }

        return 0;
    }

    public void updateProgressBar(int bar, int value)
    {
        
    }
}

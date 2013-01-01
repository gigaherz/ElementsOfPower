package gigaherz.elementsofpower;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class EssentializerTile extends TileEntity implements IInventory, ISidedInventory
{
    public static final int MaxEssentializerMagic = 1000;

    private ItemStack[] inventory;

    boolean stateChanged = false;

    public EssentializerTile()
    {
        super();
        // 0..7: magics
        // 8: input
        // 9: output
        this.inventory = new ItemStack[10];
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
        return MaxEssentializerMagic;
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
    public Packet getDescriptionPacket()
    {
        NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBT(var1);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 3, var1);
    }

    @Override
    public String getInvName()
    {
        return "EssentializerInventory";
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (this.stateChanged)
        {
            this.stateChanged = false;
            this.onInventoryChanged();
        }
    }

    @Override
    public int getStartInventorySide(ForgeDirection side)
    {
        if (ForgeDirection.UP == side || ForgeDirection.DOWN == side)
        {
            return 9;
        }

        return 8;
    }

    @Override
    public int getSizeInventorySide(ForgeDirection side)
    {
        return 1;
    }

    public void updateProgressBar(int bar, int value)
    {
        if (bar == 0)
        {
            convertInput();
        }
        else if (bar == 1)
        {
            addMagicToOutput(value);
        }
    }

    public int getInputEssencesOfType(int type)
    {
        ItemStack inputItem = inventory[8];

        if (inputItem == null)
        {
            return 0;
        }

        MagicAmounts amounts = MagicDatabase.getContainedMagic(inputItem);

        if (amounts == null)
        {
            amounts = MagicDatabase.getEssences(inputItem);

            if (amounts == null)
            {
                return 0;
            }
        }

        return amounts.getAmountOfType(type);
    }

    public void convertInput()
    {
        ItemStack input = inventory[8];

        if (input == null)
        {
            return;
        }

        if (MagicDatabase.itemContainsMagic(input))
        {
            MagicAmounts amounts = MagicDatabase.getContainedMagic(input);

            if (amounts == null)
            {
                return;
            }

            if (!tryAddAmountsToTile(amounts))
            {
                return;
            }

            inventory[8] = MagicDatabase.setContainedMagic(input, null);
        }
        else
        {
            MagicAmounts amounts = MagicDatabase.getEssences(input);

            if (amounts == null)
            {
                return;
            }

            if (!tryAddAmountsToTile(amounts))
            {
                return;
            }

            input.stackSize --;

            if (input.stackSize <= 0)
            {
                inventory[8] = null;
            }
        }

        this.stateChanged = true;
    }

    private boolean tryAddAmountsToTile(MagicAmounts amounts)
    {
        int[] am = amounts.amounts;

        // test if we can truly add the magic
        for (int i = 0; i < 8; i++)
        {
            int amount = am[i];

            if (amount > 0)
            {
                continue;
            }

            ItemStack magic = inventory[i];

            if (magic == null)
            {
                continue;
            }

            if (magic.stackSize + amount > MaxEssentializerMagic)
            {
                return false;
            }
        }

        // we can, add it
        for (int i = 0; i < 8; i++)
        {
            int amount = am[i];

            if (amount == 0)
            {
                continue;
            }

            addMagicOfType(i, amount);
        }

        return true;
    }

    private void addMagicOfType(int type, int amount)
    {
        ItemStack magic = inventory[type];

        if (magic == null)
        {
            inventory[type] = new ItemStack(ElementsOfPower.magicOrb, amount, type);
        }
        else
        {
            magic.stackSize += amount;
        }
    }

    public void addMagicToOutput(int slot)
    {
        ItemStack output = inventory[9];

        if (output == null)
        {
            return;
        }

        if (output.stackSize != 1)
        {
            return;
        }

        MagicAmounts limits = MagicDatabase.getMagicLimits(output);
        MagicAmounts amounts = MagicDatabase.getContainedMagic(output);
        int max = 0;
        int goal = 0;

        if (amounts == null)
        {
            amounts = new MagicAmounts();
        }
        else
        {
            for (int i = 0; i < 8; i++)
            {
                int amount = amounts.amounts[i];

                if (i == slot)
                {
                    goal = amount;
                }

                if (amount > max)
                {
                    max = amount;
                }
            }
        }

        if (goal < max)
        {
            goal = max;
        }
        else if (goal == max)
        {
            goal = max + 1;
        }

        for (int i = 0; i < 8; i++)
        {
        	int limit = limits.amounts[i];
            int amount = amounts.amounts[i];
            int required = goal - amount;

            if (required == 0 || (amount == 0 && i != slot))
            {
                continue;
            }

            if(amount + required > limit)
            	return;
            
            ItemStack magic = inventory[i];

            if (magic == null)
            {
                return;
            }

            if (magic.stackSize < required)
            {
                return;
            }
        }

        for (int i = 0; i < 8; i++)
        {
            int amount = amounts.amounts[i];
            int required = goal - amount;

            if (required == 0 || (amount == 0 && i != slot))
            {
                continue;
            }

            ItemStack magic = inventory[i];
            magic.stackSize -= required;

            if (magic.stackSize <= 0)
            {
                inventory[i] = null;
            }

            amounts.amounts[i] = goal;
        }

        inventory[9] = MagicDatabase.setContainedMagic(output, amounts);
    }
}

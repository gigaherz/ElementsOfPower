package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;

public class MagicHolder extends MagicAmounts implements IInventory
{
    public static final int MaxEssentializerMagic = 1000;

    public void readFromNBT(NBTTagCompound tagCompound)
    {
        NBTTagList tagList = tagCompound.getTagList("Essences", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
            byte slot = tag.getByte("Type");

            if (slot >= 0 && slot < 8)
            {
                amounts[slot] = tag.getFloat("Count");
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound)
    {
        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < 8; i++)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Type", (byte) i);
            tag.setFloat("Count", amounts[i]);
            itemList.appendTag(tag);
        }

        tagCompound.setTag("Essences", itemList);
    }

    public boolean processInventory(InventoryBasic inventory)
    {
        boolean b1 = convertInput(inventory);
        boolean b2 = addMagicToOutput(inventory);
        return b1 || b2;
    }

    public boolean convertInput(InventoryBasic inventory)
    {
        ItemStack input = inventory.getStackInSlot(0);

        if (input == null)
        {
            return false;
        }

        if (MagicDatabase.itemContainsMagic(input))
        {
            MagicAmounts contained = MagicDatabase.getContainedMagic(input);

            if (contained == null)
                return false;

            boolean inserted = false;
            for (int i = 0; i < 8; i++)
            {
                if (contained.amounts[i] > 0 && amounts[i] < MaxEssentializerMagic)
                {
                    amounts[i] += 1;
                    contained.amounts[i]--;
                    inserted = true;
                }
            }

            if (!inserted)
                return false;

            if (contained.getTotalMagic() == 0)
                contained = null;

            input = MagicDatabase.setContainedMagic(input, contained);
        }
        else
        {
            MagicAmounts contained = MagicDatabase.getEssences(input);

            if (contained == null)
                return false;

            if (contained.isEmpty())
                return false;

            if (!tryAddAll(contained))
                return false;

            input.stackSize--;

            if (input.stackSize <= 0)
                input = null;
        }

        inventory.setInventorySlotContents(0, input);
        return true;
    }

    public boolean addMagicToOutput(InventoryBasic inventory)
    {
        ItemStack output = inventory.getStackInSlot(1);

        if (output == null)
        {
            return false;
        }

        if (output.stackSize != 1)
        {
            return false;
        }

        MagicAmounts limits = MagicDatabase.getMagicLimits(output);
        MagicAmounts contained = MagicDatabase.getContainedMagic(output);

        if (limits == null)
            return false;

        if (contained == null)
        {
            contained = new MagicAmounts();
        }

        int added = 0;
        for (int i = 0; i < 8; i++)
        {
            float transfer = Math.min(Math.min(limits.amounts[i] - contained.amounts[i], 1), amounts[i]);

            amounts[i] -= transfer;

            contained.amounts[i] += transfer;
            added += transfer;
        }

        if (added == 0)
            return false;

        inventory.setInventorySlotContents(1, MagicDatabase.setContainedMagic(output, contained));
        return true;
    }

    private boolean tryAddAll(MagicAmounts magic)
    {
        // test if we can truly add the magic
        for (int i = 0; i < 8; i++)
        {
            float amount = magic.amounts[i];

            if (amount == 0)
                continue;

            if (amounts[i] + amount > MaxEssentializerMagic)
            {
                return false;
            }
        }

        // we can, add it
        for (int i = 0; i < 8; i++)
        {
            float amount = magic.amounts[i];

            if (amount == 0)
                continue;

            amounts[i] += amount;
        }

        return true;
    }

    @Override
    public int getSizeInventory()
    {
        return 8;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if (amounts[index] == 0)
            return null;
        return new ItemStack(ElementsOfPower.magicOrb, (int) amounts[index], index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        amounts[index] -= count;
        return getStackInSlot(index);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (stack != null)
            amounts[index] = stack.stackSize;
        else
            amounts[index] = 0;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return MaxEssentializerMagic;
    }

    @Override
    public void markDirty()
    {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {

    }

    @Override
    public void closeInventory(EntityPlayer player)
    {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return false;
    }

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
    public void clear()
    {

    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }
}

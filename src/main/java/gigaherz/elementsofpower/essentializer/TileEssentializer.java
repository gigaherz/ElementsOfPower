package gigaherz.elementsofpower.essentializer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;

public class TileEssentializer
        extends TileEntity
        implements ISidedInventory, ITickable
{
    public static final int MaxEssentializerMagic = 32768;
    public static final float MaxConvertPerTick = 5 / 20.0f;

    public final InventoryBasic inventory = new InventoryBasic(ElementsOfPower.MODID + ".essentializer", false, 3);

    public MagicAmounts containedMagic = new MagicAmounts();
    public MagicAmounts remainingToConvert;

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        readInventoryFromNBT(tagCompound);
        containedMagic = readAmountsFromNBT(tagCompound, "Contained");
        remainingToConvert = readAmountsFromNBT(tagCompound, "Remaining");
        if (containedMagic == null)
            containedMagic = new MagicAmounts();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        tagCompound = super.writeToNBT(tagCompound);
        writeInventoryToNBT(tagCompound);
        writeAmountsToNBT(tagCompound, "Contained", containedMagic);
        writeAmountsToNBT(tagCompound, "Remaining", remainingToConvert);
        return tagCompound;
    }

    @Nullable
    private MagicAmounts readAmountsFromNBT(NBTTagCompound tagCompound, String key)
    {
        MagicAmounts amounts = null;
        if (tagCompound.hasKey(key, Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound tag = tagCompound.getCompoundTag(key);

            amounts = new MagicAmounts();
            amounts.readFromNBT(tag);
        }
        return amounts;
    }

    private void writeAmountsToNBT(NBTTagCompound tagCompound, String key, @Nullable MagicAmounts amounts)
    {
        if (amounts != null)
        {
            NBTTagCompound tag = new NBTTagCompound();

            amounts.writeToNBT(tag);

            tagCompound.setTag(key, tag);
        }
    }

    private void readInventoryFromNBT(NBTTagCompound tagCompound)
    {
        NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);

        inventory.clear();
        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
            byte slot = tag.getByte("Slot");

            if (slot < inventory.getSizeInventory())
            {
                inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
            }
        }
    }

    private void writeInventoryToNBT(NBTTagCompound tagCompound)
    {
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
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        readFromNBT(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(packet.getNbtCompound());

        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public void update()
    {
        if (!worldObj.isRemote)
        {
            boolean b0 = convertRemaining();
            boolean b1 = convertSource(inventory);
            boolean b2 = getMagicFromInput(inventory);
            boolean b3 = addMagicToOutput(inventory);
            if (b0 || b1 || b2 || b3)
            {
                ElementsOfPower.channel.sendToAllAround(new EssentializerTileUpdate(this), new NetworkRegistry.TargetPoint(worldObj.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
            }
            if (b1 || b2 || b3)
            {
                markDirty();
            }
        }
    }

    private boolean convertRemaining()
    {
        if (remainingToConvert == null)
            return false;

        float totalAdded = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = Math.min(MaxConvertPerTick, MaxEssentializerMagic - containedMagic.amounts[i]);
            float transfer = Math.min(maxTransfer, remainingToConvert.amounts[i]);
            if (transfer > 0)
            {
                remainingToConvert.amounts[i] -= transfer;
                containedMagic.amounts[i] += transfer;
                totalAdded += transfer;
            }
        }

        if (remainingToConvert.isEmpty())
            remainingToConvert = null;

        return totalAdded > 0;
    }

    private boolean convertSource(InventoryBasic inventory)
    {
        if (remainingToConvert != null)
            return false;

        ItemStack input = inventory.getStackInSlot(0);

        if (input == null)
        {
            return false;
        }

        MagicAmounts contained = EssenceConversions.getEssences(input, false);

        if (contained == null)
            return false;

        if (contained.isEmpty())
            return false;

        if (!canAddAll(contained))
            return false;

        remainingToConvert = contained.copy();

        input.stackSize--;

        if (input.stackSize <= 0)
            input = null;

        inventory.setInventorySlotContents(0, input);
        return true;
    }

    private boolean getMagicFromInput(InventoryBasic inventory)
    {
        ItemStack input = inventory.getStackInSlot(1);

        if (input == null)
        {
            return false;
        }

        if (!ContainerInformation.itemContainsMagic(input))
            return false;

        boolean isInfinite = ContainerInformation.isInfiniteContainer(input);

        MagicAmounts contained = ContainerInformation.getContainedMagic(input);

        if (contained.isEmpty())
            return false;

        float totalAdded = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = Math.min(MaxConvertPerTick, MaxEssentializerMagic - containedMagic.amounts[i]);
            float transfer = isInfinite ? maxTransfer : Math.min(maxTransfer, contained.amounts[i]);
            if (transfer > 0)
            {
                if (!isInfinite) contained.amounts[i] -= transfer;
                containedMagic.amounts[i] += transfer;
                totalAdded += transfer;
            }
        }

        if (totalAdded <= 0)
            return false;

        input = ContainerInformation.setContainedMagic(input, contained);

        inventory.setInventorySlotContents(1, input);
        return true;
    }

    private boolean addMagicToOutput(InventoryBasic inventory)
    {
        ItemStack output = inventory.getStackInSlot(2);

        if (output == null)
        {
            return false;
        }

        if (output.stackSize != 1)
        {
            return false;
        }

        if (ContainerInformation.isInfiniteContainer(output))
            return false;

        MagicAmounts limits = ContainerInformation.getMagicLimits(output);
        MagicAmounts contained = ContainerInformation.getContainedMagic(output);

        if (limits.isEmpty())
            return false;

        float totalAdded = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = Math.min(MaxConvertPerTick, limits.amounts[i] - contained.amounts[i]);
            float transfer = Math.min(maxTransfer, containedMagic.amounts[i]);
            if (transfer > 0)
            {
                contained.amounts[i] += transfer;
                containedMagic.amounts[i] -= transfer;
                totalAdded += transfer;
            }
        }

        if (totalAdded <= 0)
            return false;

        inventory.setInventorySlotContents(2, ContainerInformation.setContainedMagic(output, contained));
        return true;
    }

    private boolean canAddAll(MagicAmounts magic)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float amount = magic.amounts[i];

            if (amount <= 0)
                continue;

            if (containedMagic.amounts[i] + amount > MaxEssentializerMagic)
            {
                return false;
            }
        }

        return true;
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
    public ITextComponent getDisplayName()
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
    public void setInventorySlotContents(int slot, @Nullable ItemStack stack)
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
                return ContainerInformation.itemContainsMagic(stack) || EssenceConversions.itemHasEssence(stack);
            case 1:
                return ContainerInformation.canItemContainMagic(stack);
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

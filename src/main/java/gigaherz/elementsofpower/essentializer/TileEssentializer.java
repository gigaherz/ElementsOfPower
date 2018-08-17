package gigaherz.elementsofpower.essentializer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEssentializer
        extends TileEntity
        implements ITickable
{
    public static final int MaxEssentializerMagic = 32768;
    public static final float MaxConvertPerTick = 5 / 20.0f;
    public static final float MaxTransferPerTick = 50 / 20.0f;

    public final IItemHandlerModifiable inventory = new ItemStackHandler(3)
    {

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            markDirty();
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }

        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            switch (index)
            {
                case 0:
                    return EssenceConversions.itemHasEssence(stack);
                case 1:
                    return CapabilityMagicContainer.hasContainer(stack);
                case 2:
                    return CapabilityMagicContainer.hasContainer(stack);
            }
            return false;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (!isItemValidForSlot(slot, stack))
                return stack;
            return super.insertItem(slot, stack, simulate);
        }
    };

    public IItemHandler sides = new RangedWrapper(inventory, 0, 2);
    public IItemHandler top = new RangedWrapper(inventory, 2, 3);

    public MagicAmounts containedMagic = MagicAmounts.EMPTY;
    public MagicAmounts remainingToConvert = MagicAmounts.EMPTY;

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return facing != EnumFacing.DOWN;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (facing == null) return (T) inventory;
            switch (facing)
            {
                case UP:
                    return (T) top;
                case DOWN:
                    return null;
                default:
                    return (T) sides;
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        readInventoryFromNBT(tagCompound);
        containedMagic = readAmountsFromNBT(tagCompound, "Contained");
        remainingToConvert = readAmountsFromNBT(tagCompound, "Remaining");
        if (containedMagic == null)
            containedMagic = MagicAmounts.EMPTY;
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

    private MagicAmounts readAmountsFromNBT(NBTTagCompound tagCompound, String key)
    {
        MagicAmounts amounts = MagicAmounts.EMPTY;
        if (tagCompound.hasKey(key, Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound tag = tagCompound.getCompoundTag(key);

            amounts = new MagicAmounts(tag);
        }
        return amounts;
    }

    private void writeAmountsToNBT(NBTTagCompound tagCompound, String key, MagicAmounts amounts)
    {
        tagCompound.setTag(key, amounts.serializeNBT());
    }

    private void readInventoryFromNBT(NBTTagCompound tagCompound)
    {
        if (tagCompound.getTagId("Inventory") == Constants.NBT.TAG_LIST)
        {
            NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < inventory.getSlots(); i++)
            { inventory.setStackInSlot(i, ItemStack.EMPTY); }

            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
                byte slot = tag.getByte("Slot");

                if (slot < inventory.getSlots())
                {
                    inventory.setStackInSlot(slot, new ItemStack(tag));
                }
            }

            return;
        }

        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inventory, null, tagCompound.getTag("Slots"));
    }

    private void writeInventoryToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setTag("Slots", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(inventory, null));
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

        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public void update()
    {
        if (!world.isRemote)
        {
            boolean b0 = convertRemaining();
            boolean b1 = convertSource(inventory);
            boolean b2 = getMagicFromInput(inventory);
            boolean b3 = addMagicToOutput(inventory);
            if (b0 || b1 || b2 || b3)
            {
                ElementsOfPower.channel.sendToAllAround(new EssentializerTileUpdate(this), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
            }
            if (b1 || b2 || b3)
            {
                markDirty();
            }
        }
    }

    private boolean convertRemaining()
    {
        if (remainingToConvert.isEmpty())
            return false;

        float totalAdded = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = Math.min(MaxConvertPerTick, MaxEssentializerMagic - containedMagic.get(i));
            float transfer = Math.min(maxTransfer, remainingToConvert.get(i));
            if (transfer > 0)
            {
                remainingToConvert = remainingToConvert.add(i, -transfer);
                containedMagic = containedMagic.add(i, +transfer);
                totalAdded += transfer;
            }
        }

        return totalAdded > 0;
    }

    private boolean convertSource(IItemHandlerModifiable inventory)
    {
        if (!remainingToConvert.isEmpty())
            return false;

        ItemStack input = inventory.getStackInSlot(0);

        if (input.getCount() <= 0)
        {
            return false;
        }

        MagicAmounts contained = EssenceConversions.getEssences(input, false);

        if (contained.isEmpty())
            return false;

        if (!canAddAll(contained))
            return false;

        remainingToConvert = contained;

        input.shrink(1);

        if (input.getCount() <= 0)
            input = ItemStack.EMPTY;

        inventory.setStackInSlot(0, input);
        return true;
    }

    private boolean getMagicFromInput(IItemHandlerModifiable inventory)
    {
        ItemStack input = inventory.getStackInSlot(1);

        IMagicContainer magic = CapabilityMagicContainer.getContainer(input);
        if (magic == null)
            return false;

        boolean isInfinite = magic.isInfinite();

        MagicAmounts contained = magic.getContainedMagic();
        if (contained.isEmpty())
            return false;

        float totalAdded = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = Math.min(MaxTransferPerTick, MaxEssentializerMagic - containedMagic.get(i));
            float transfer = isInfinite ? maxTransfer : Math.min(maxTransfer, contained.get(i));
            if (transfer > 0)
            {
                if (!isInfinite) contained = contained.add(i, -transfer);
                containedMagic = containedMagic.add(i, transfer);
                totalAdded += transfer;
            }
        }

        if (totalAdded <= 0)
            return false;

        magic.setContainedMagic(contained);

        inventory.setStackInSlot(1, input);
        return true;
    }

    private boolean addMagicToOutput(IItemHandlerModifiable inventory)
    {
        ItemStack output = inventory.getStackInSlot(2);

        if (output.getCount() != 1)
        {
            return false;
        }

        IMagicContainer magic = CapabilityMagicContainer.getContainer(output);
        if (magic == null)
            return false;

        if (magic.isInfinite())
            return false;

        MagicAmounts limits = magic.getCapacity();
        MagicAmounts contained = magic.getContainedMagic();

        if (limits.isEmpty())
            return false;

        float totalAdded = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float maxTransfer = Math.min(MaxTransferPerTick, limits.get(i) - contained.get(i));
            float transfer = Math.min(maxTransfer, containedMagic.get(i));
            if (transfer > 0)
            {
                contained = contained.add(i, transfer);
                containedMagic = containedMagic.add(i, -transfer);
                totalAdded += transfer;
            }
        }

        if (totalAdded <= 0)
            return false;

        magic.setContainedMagic(contained);
        return true;
    }

    private boolean canAddAll(MagicAmounts magic)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float amount = magic.get(i);

            if (amount <= 0)
                continue;

            if (containedMagic.get(i) + amount > MaxEssentializerMagic)
            {
                return false;
            }
        }

        return true;
    }

    public IItemHandlerModifiable getInventory()
    {
        return inventory;
    }
}

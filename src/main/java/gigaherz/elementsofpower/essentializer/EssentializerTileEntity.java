package gigaherz.elementsofpower.essentializer;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.essentializer.gui.IMagicAmountHolder;
import gigaherz.elementsofpower.network.UpdateEssentializerTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class EssentializerTileEntity
        extends TileEntity
        implements ITickableTileEntity, IMagicAmountHolder
{
    @ObjectHolder("elementsofpower:essentializer")
    public static TileEntityType<EssentializerTileEntity> TYPE;

    public static final int MAX_ESSENTIALIZER_MAGIC = 32768;
    public static final float MAX_CONVERT_PER_TICK = 5 / 20.0f;
    public static final float MAX_TRANSFER_PER_TICK = 50 / 20.0f;

    public final IItemHandlerModifiable inventory = new ItemStackHandler(3)
    {

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            markDirty();
            if (world == null)
                return;
            BlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }

        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            switch (index)
            {
                case 0:
                    return EssenceConversions.get(world).itemHasEssence(stack.getItem());
                case 1:
                    return MagicContainerCapability.hasContainer(stack);
                case 2:
                    return MagicContainerCapability.hasContainer(stack);
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

    private final LazyOptional<IItemHandler> inventoryGetter = LazyOptional.of(() -> inventory);
    private final LazyOptional<IItemHandler> topGetter = LazyOptional.of(() -> top);
    private final LazyOptional<IItemHandler> sideGetter = LazyOptional.of(() -> sides);

    public EssentializerTileEntity(TileEntityType<?> type)
    {
        super(type);
    }

    public EssentializerTileEntity()
    {
        super(TYPE);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (facing == null) return inventoryGetter.cast();
            switch (facing)
            {
                case UP:
                    return topGetter.cast();
                case DOWN:
                    return LazyOptional.empty();
                default:
                    return sideGetter.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT compound)
    {
        super.func_230337_a_(state, compound);

        readInventoryFromNBT(compound);
        containedMagic = readAmountsFromNBT(compound, "Contained");
        remainingToConvert = readAmountsFromNBT(compound, "Remaining");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound)
    {
        tagCompound = super.write(tagCompound);
        writeInventoryToNBT(tagCompound);
        writeAmountsToNBT(tagCompound, "Contained", containedMagic);
        writeAmountsToNBT(tagCompound, "Remaining", remainingToConvert);
        return tagCompound;
    }

    private MagicAmounts readAmountsFromNBT(CompoundNBT tagCompound, String key)
    {
        MagicAmounts amounts = MagicAmounts.EMPTY;
        if (tagCompound.contains(key, Constants.NBT.TAG_COMPOUND))
        {
            CompoundNBT tag = tagCompound.getCompound(key);

            amounts = new MagicAmounts(tag);
        }
        return amounts;
    }

    private void writeAmountsToNBT(CompoundNBT tagCompound, String key, MagicAmounts amounts)
    {
        tagCompound.put(key, amounts.serializeNBT());
    }

    private void readInventoryFromNBT(CompoundNBT tagCompound)
    {
        if (tagCompound.contains("Slots", Constants.NBT.TAG_LIST))
        {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inventory, null, tagCompound.getList("Slots", Constants.NBT.TAG_COMPOUND));
        }
    }

    private void writeInventoryToNBT(CompoundNBT tagCompound)
    {
        tagCompound.put("Slots", Objects.requireNonNull(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(inventory, null)));
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {
        func_230337_a_(state, tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(getBlockState(), packet.getNbtCompound());

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public void tick()
    {
        if (!world.isRemote)
        {
            boolean b0 = convertRemaining();
            boolean b1 = convertSource(inventory);
            boolean b2 = getMagicFromInput(inventory);
            boolean b3 = addMagicToOutput(inventory);
            if (b0 || b1 || b2 || b3)
            {
                ElementsOfPowerMod.CHANNEL.send(
                        PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk) world.getChunk(pos)),
                        new UpdateEssentializerTileEntity(this));
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
            float maxTransfer = Math.min(MAX_CONVERT_PER_TICK, MAX_ESSENTIALIZER_MAGIC - containedMagic.get(i));
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

        MagicAmounts contained = EssenceConversions.get(world).getEssences(input, false);

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

        return MagicContainerCapability.getContainer(input).map(magic -> {

            boolean isInfinite = magic.isInfinite();

            MagicAmounts contained = magic.getContainedMagic();
            if (contained.isEmpty())
                return false;

            float totalAdded = 0;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                float maxTransfer = Math.min(MAX_TRANSFER_PER_TICK, MAX_ESSENTIALIZER_MAGIC - containedMagic.get(i));
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
        }).orElse(false);
    }

    private boolean addMagicToOutput(IItemHandlerModifiable inventory)
    {
        ItemStack output = inventory.getStackInSlot(2);

        if (output.getCount() != 1)
        {
            return false;
        }

        return MagicContainerCapability.getContainer(output).map(magic -> {
            if (magic.isInfinite())
                return false;

            MagicAmounts limits = magic.getCapacity();
            MagicAmounts contained = magic.getContainedMagic();

            if (limits.isEmpty())
                return false;

            float totalAdded = 0;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                float maxTransfer = Math.min(MAX_TRANSFER_PER_TICK, limits.get(i) - contained.get(i));
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
        }).orElse(false);
    }

    private boolean canAddAll(MagicAmounts magic)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float amount = magic.get(i);

            if (amount <= 0)
                continue;

            if (containedMagic.get(i) + amount > MAX_ESSENTIALIZER_MAGIC)
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

    @Override
    public MagicAmounts getContainedMagic()
    {
        return containedMagic;
    }

    @Override
    public MagicAmounts getRemainingToConvert()
    {
        return remainingToConvert;
    }

    @Override
    public void setContainedMagic(MagicAmounts contained)
    {
        containedMagic = contained;
    }

    @Override
    public void setRemainingToConvert(MagicAmounts remaining)
    {
        remainingToConvert = remaining;
    }
}

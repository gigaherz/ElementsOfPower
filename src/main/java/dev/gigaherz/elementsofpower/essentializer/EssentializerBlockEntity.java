package dev.gigaherz.elementsofpower.essentializer;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountHolder;
import dev.gigaherz.elementsofpower.integration.aequivaleo.AequivaleoPlugin;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.UpdateEssentializerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EssentializerBlockEntity
        extends BlockEntity
        implements IMagicAmountHolder
{
    public static final int MAX_ESSENTIALIZER_MAGIC = 32768;
    public static final float MAX_CONVERT_PER_TICK = 5 / 20.0f;
    public static final float MAX_TRANSFER_PER_TICK = 50 / 20.0f;

    public final ItemStackHandler inventory = new ItemStackHandler(3)
    {

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            EssentializerBlockEntity.this.setChanged();
            if (level == null)
                return;
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }

        public boolean isItemValidForSlot(int index, ItemStack stack)
        {
            return switch (index)
                    {
                        case 0 -> ModList.get().isLoaded("aequivaleo") && AequivaleoPlugin.getEssences(level, stack, false).isPresent();
                        case 1, 2 -> MagicContainerCapability.hasContainer(stack);
                        default -> false;
                    };
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

    public float animateTick;

    public EssentializerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public EssentializerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ElementsOfPowerMod.ESSENTIALIZER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == Capabilities.ITEM_HANDLER)
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
    public void load(CompoundTag compound)
    {
        super.load(compound);

        if (compound.contains("Slots", Tag.TAG_LIST))
        {
            ListTag tagList = compound.getList("Slots", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++)
            {
                CompoundTag itemTags = tagList.getCompound(i);
                int slot = itemTags.getInt("Slot");

                if (slot >= 0 && slot < inventory.getSlots())
                {
                    inventory.setStackInSlot(slot, ItemStack.of(itemTags));
                }
            }
        }
        else if (compound.contains("Inventory", Tag.TAG_COMPOUND))
        {
            inventory.deserializeNBT(compound.getCompound("Inventory"));
        }
        containedMagic = readAmountsFromNBT(compound, "Contained");
        remainingToConvert = readAmountsFromNBT(compound, "Remaining");
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.put("Slots", inventory.serializeNBT());
        writeAmountsToNBT(tag, "Contained", containedMagic);
        writeAmountsToNBT(tag, "Remaining", remainingToConvert);
    }

    private MagicAmounts readAmountsFromNBT(CompoundTag tagCompound, String key)
    {
        MagicAmounts amounts = MagicAmounts.EMPTY;
        if (tagCompound.contains(key, Tag.TAG_COMPOUND))
        {
            CompoundTag tag = tagCompound.getCompound(key);

            amounts = new MagicAmounts(tag);
        }
        return amounts;
    }

    private void writeAmountsToNBT(CompoundTag tagCompound, String key, MagicAmounts amounts)
    {
        tagCompound.put(key, amounts.serializeNBT());
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(packet.getTag());

        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    public void tickServer()
    {
        boolean b0 = convertRemaining();
        boolean b1 = convertSource(inventory);
        boolean b2 = getMagicFromInput(inventory);
        boolean b3 = addMagicToOutput(inventory);
        if (b0 || b1 || b2 || b3)
        {
            ElementsOfPowerMod.CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> (LevelChunk) level.getChunk(worldPosition)),
                    new UpdateEssentializerTile(this));
        }
        if (b1 || b2 || b3)
        {
            setChanged();
        }
    }

    public void tickClient()
    {
        animateTick++;
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

        MagicAmounts contained = ModList.get().isLoaded("aequivaleo") ? AequivaleoPlugin.getEssences(level, input, false).orElse(MagicAmounts.EMPTY) : MagicAmounts.EMPTY;

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

    public static void doTickServer(Level level, BlockPos blockPos, BlockState blockState, EssentializerBlockEntity blockEntity)
    {
        blockEntity.tickServer();
    }

    public static void doTickClient(Level level, BlockPos blockPos, BlockState blockState, EssentializerBlockEntity blockEntity)
    {
        blockEntity.tickClient();
    }
}

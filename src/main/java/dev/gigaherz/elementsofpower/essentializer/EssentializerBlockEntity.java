package dev.gigaherz.elementsofpower.essentializer;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.database.EssenceConversionManager;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountHolder;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.UpdateEssentializerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid=ElementsOfPowerMod.MODID, bus= EventBusSubscriber.Bus.MOD)
public class EssentializerBlockEntity
        extends BlockEntity
        implements IMagicAmountHolder
{
    public static final int MAX_ESSENTIALIZER_MAGIC = 32768;
    public static final float MAX_CONVERT_PER_TICK = 5 / 20.0f;
    public static final float MAX_TRANSFER_PER_TICK = 50 / 20.0f;

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.<IItemHandler, @Nullable Direction, EssentializerBlockEntity>registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ElementsOfPowerMod.ESSENTIALIZER_BLOCK_ENTITY.get(),
                (be, facing) -> {
                    if (facing == null)
                        return be.inventory;
                    return switch (facing)
                    {
                        case UP -> be.top;
                        case DOWN -> null;
                        default -> be.sides;
                    };
                }
        );
    }

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
                        case 0 -> ModList.get().isLoaded("aequivaleo") && EssenceConversionManager.getEssences(level, stack, false).isPresent();
                        case 1, 2 -> MagicContainerCapability.hasContainer(stack);
                        default -> false;
                    };
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
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

    public float animateTick;

    public EssentializerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public EssentializerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ElementsOfPowerMod.ESSENTIALIZER_BLOCK_ENTITY.get(), pos, state);
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
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup)
    {
        super.loadAdditional(compound, lookup);

        if (compound.contains("Slots", Tag.TAG_LIST))
        {
            ListTag tagList = compound.getList("Slots", Tag.TAG_COMPOUND);
            for (int i = 0; i < tagList.size(); i++)
            {
                CompoundTag itemTags = tagList.getCompound(i);
                int slot = itemTags.getInt("Slot");

                if (slot >= 0 && slot < inventory.getSlots())
                {
                    inventory.setStackInSlot(slot, ItemStack.parse(lookup, itemTags).orElseGet(() -> ItemStack.EMPTY));
                }
            }
        }
        else if (compound.contains("Inventory", Tag.TAG_COMPOUND))
        {
            inventory.deserializeNBT(lookup, compound.getCompound("Inventory"));
        }
        containedMagic = readAmountsFromNBT(compound, "Contained");
        remainingToConvert = readAmountsFromNBT(compound, "Remaining");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup)
    {
        super.saveAdditional(tag, lookup);

        tag.put("Slots", inventory.serializeNBT(lookup));
        writeAmountsToNBT(tag, "Contained", containedMagic);
        writeAmountsToNBT(tag, "Remaining", remainingToConvert);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup)
    {
        return saveCustomOnly(lookup);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookup)
    {
        loadCustomOnly(tag, lookup);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider lookup)
    {
        handleUpdateTag(packet.getTag(), lookup);

        // Refresh render meshes
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
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel)level, new ChunkPos(worldPosition),
                    new UpdateEssentializerTile(getBlockPos(), remainingToConvert, getInventory().getStackInSlot(0)));
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

        MagicAmounts contained = ModList.get().isLoaded("aequivaleo") ? EssenceConversionManager.getEssences(level, input, false).orElse(MagicAmounts.EMPTY) : MagicAmounts.EMPTY;

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

        var magic = MagicContainerCapability.getContainer(input);
        if (magic != null)
        {
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
        }

        return false;
    }

    private boolean addMagicToOutput(IItemHandlerModifiable inventory)
    {
        ItemStack output = inventory.getStackInSlot(2);

        if (output.getCount() != 1)
        {
            return false;
        }

        var magic = MagicContainerCapability.getContainer(output);
        if (magic != null)
        {
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
        }

        return false;
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

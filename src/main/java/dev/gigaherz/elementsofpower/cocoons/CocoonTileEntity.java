package dev.gigaherz.elementsofpower.cocoons;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountContainer;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CocoonTileEntity extends BlockEntity implements IMagicAmountContainer
{
    public MagicAmounts essenceContained = MagicAmounts.EMPTY;

    public CocoonTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public CocoonTileEntity(BlockPos pos, BlockState state)
    {
        super(ElementsOfPowerMod.COCOON_BLOCL_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup)
    {
        super.loadAdditional(compound, lookup);
        essenceContained = new MagicAmounts(compound.getCompound("Magic"));
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup)
    {
        super.saveAdditional(compound, lookup);

        compound.put("Magic", essenceContained.serializeNBT());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup)
    {
        return saveWithoutMetadata(lookup);
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
        super.onDataPacket(net, packet, lookup);
        handleUpdateTag(packet.getTag(), lookup);
    }

    public MagicAmounts transferToPlayer(RandomSource random, IMagicContainer cap)
    {
        MagicAmounts am = essenceContained;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            am = am.with(i, (float) Math.floor(essenceContained.get(i) * random.nextFloat() * 2) / 10.0f);
        }

        if (!am.isEmpty())
        {
            return cap.addMagic(am);
        }

        return MagicAmounts.EMPTY;
    }

    public void addEssences(ItemStack stack)
    {
        essenceContained = essenceContained.add(((MagicOrbItem) stack.getItem()).getElement(), 1);

        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    @Override
    public MagicAmounts getContainedMagic()
    {
        return essenceContained;
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        CocoonEventHandling.track(this);
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        CocoonEventHandling.untrack(this);
    }
}

package dev.gigaherz.elementsofpower.cocoons;

import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountContainer;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Random;

public class CocoonTileEntity extends BlockEntity implements IMagicAmountContainer
{
    @ObjectHolder("elementsofpower:cocoon")
    public static BlockEntityType<CocoonTileEntity> TYPE;

    public MagicAmounts essenceContained = MagicAmounts.EMPTY;

    public CocoonTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public CocoonTileEntity(BlockPos pos, BlockState state)
    {
        super(TYPE, pos, state);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        essenceContained = new MagicAmounts(compound.getCompound("Magic"));
    }

    @Override
    public CompoundTag save(CompoundTag compound)
    {
        compound = super.save(compound);

        compound.put("Magic", essenceContained.serializeNBT());

        return compound;
    }

    public void transferToPlayer(Random random, PlayerCombinedMagicContainers cap)
    {
        MagicAmounts am = essenceContained;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            am = am.with(i, (float) Math.floor(essenceContained.get(i) * random.nextFloat() * 2) / 10.0f);
        }

        if (!am.isEmpty())
        {
            cap.addMagic(am);
        }
    }

    public void addEssences(ItemStack stack)
    {
        essenceContained = essenceContained.add(((MagicOrbItem) stack.getItem()).getElement(), 1);

        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return save(new CompoundTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet)
    {
        super.onDataPacket(net, packet);
        handleUpdateTag(packet.getTag());
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
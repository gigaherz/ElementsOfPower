package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.essentializer.gui.IMagicAmountContainer;
import gigaherz.elementsofpower.items.MagicOrbItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;
import java.util.Random;

public class CocoonTileEntity extends TileEntity implements IMagicAmountContainer
{
    @ObjectHolder("elementsofpower:cocoon")
    public static TileEntityType<CocoonTileEntity> TYPE;

    public MagicAmounts essenceContained = MagicAmounts.EMPTY;

    public CocoonTileEntity(TileEntityType<?> type)
    {
        super(type);
    }

    public CocoonTileEntity()
    {
        super(TYPE);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound)
    {
        super.read(state, compound);
        essenceContained = new MagicAmounts(compound.getCompound("Magic"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);

        compound.put("Magic", essenceContained.serializeNBT());

        return compound;
    }

    public void transferToPlayer(Random random, PlayerCombinedMagicContainers cap)
    {
        MagicAmounts am = essenceContained;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            am = am.with(i, (float) Math.floor(essenceContained.get(i) * random.nextFloat()*2)/10.0f);
        }

        if (!am.isEmpty())
        {
            cap.addMagic(am);
        }
    }

    public void addEssences(ItemStack stack)
    {
        essenceContained = essenceContained.add(((MagicOrbItem) stack.getItem()).getElement(), 1);

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {
        read(state, tag);
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
    }

    @Override
    public MagicAmounts getContainedMagic()
    {
        return essenceContained;
    }

    @Override
    public void validate()
    {
        super.validate();
        CocoonEventHandling.track(this);
    }

    @Override
    public void remove()
    {
        super.remove();
        CocoonEventHandling.untrack(this);
    }
}

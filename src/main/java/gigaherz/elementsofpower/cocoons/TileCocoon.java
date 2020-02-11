package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileCocoon extends TileEntity implements ITickable
{
    public MagicAmounts essenceContained = MagicAmounts.EMPTY;

    @Override
    public void readFromNBT(CompoundNBT compound)
    {
        super.readFromNBT(compound);

        essenceContained = new MagicAmounts(compound.getCompoundTag("Magic"));
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT compound)
    {
        compound = super.writeToNBT(compound);

        compound.setTag("Magic", essenceContained.serializeNBT());

        return compound;
    }

    @Override
    public void update()
    {

    }

    public int getDominantElement()
    {
        return essenceContained.getDominantElement();
    }

    public void addEssences(ItemStack stack)
    {
        essenceContained = essenceContained.add(Element.values[stack.getMetadata()], 1);

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return writeToNBT(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag)
    {
        readFromNBT(tag);
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
        handleUpdateTag(packet.getNbtCompound());
    }
}

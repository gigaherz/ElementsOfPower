package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileCocoon extends TileEntity implements ITickable
{
    public final MagicAmounts essenceContained = new MagicAmounts();

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        essenceContained.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        essenceContained.writeToNBT(compound);

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
        essenceContained.element(Element.values[stack.getMetadata()], 1);

        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 3);
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
    }
}

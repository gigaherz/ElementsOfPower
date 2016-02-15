package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
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
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        essenceContained.writeToNBT(compound);
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
        essenceContained.element(Element.values()[stack.getMetadata()], 1);
        worldObj.markBlockForUpdate(pos);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(this.pos, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        super.onDataPacket(net, packet);
        readFromNBT(packet.getNbtCompound());
        //this.worldObj.markBlockForUpdate(this.pos);
    }
}

package gigaherz.elementsofpower.cocoons;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class TileCocoon extends TileEntity implements ITickableTileEntity
{
    @ObjectHolder("elementsofpower:cocoon")
    public static TileEntityType<TileCocoon> TYPE;

    public MagicAmounts essenceContained = MagicAmounts.EMPTY;

    public TileCocoon(TileEntityType<?> type)
    {
        super(type);
    }

    public TileCocoon()
    {
        super(TYPE);
    }

    @Override
    public void read(CompoundNBT compound)
    {
        super.read(compound);

        essenceContained = new MagicAmounts(compound.getCompound("Magic"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);

        compound.put("Magic", essenceContained.serializeNBT());

        return compound;
    }

    @Override
    public void tick()
    {

    }

    public int getDominantElement()
    {
        return essenceContained.getDominantElement();
    }

    public void addEssences(ItemStack stack)
    {
        essenceContained = essenceContained.add(((ItemMagicOrb)stack.getItem()).getElement(), 1);

        BlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag)
    {
        read(tag);
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

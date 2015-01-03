package gigaherz.elementsofpower.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Created by gigaherz on 03/01/2015.
 */
public class ProgressUpdatePacket
        implements IMessage {

    BlockPos pos;

    public int barIndex;
    public int barValue;

    @Override
    public void fromBytes(ByteBuf buf) {

        pos = new BlockPos(
            buf.readInt(),
            buf.readInt(),
            buf.readInt());
        barIndex = buf.readInt();
        barValue = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(barIndex);
        buf.writeInt(barValue);
    }

    public void setTileEntityTarget(TileEntity tile) {
        pos = tile.getPos();
    }

    public TileEntity getTileEntityTarget() {
        //World world = DimensionManager.getWorld(dim);
        //return world.getTileEntity(pos);
        return null;
    }
}

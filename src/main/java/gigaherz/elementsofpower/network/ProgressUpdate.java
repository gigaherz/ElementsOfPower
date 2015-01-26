package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.blocks.TileEssentializer;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProgressUpdate
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

    public static class Handler implements IMessageHandler<ProgressUpdate, IMessage> {

        @Override
        public IMessage onMessage(ProgressUpdate message, MessageContext ctx) {

            TileEntity tile = message.getTileEntityTarget();

            if (!(tile instanceof TileEssentializer)) {
                return null;
            }

            TileEssentializer essentializer = (TileEssentializer) tile;
            essentializer.updateProgressBar(message.barIndex, message.barValue);

            return null; // no response in this case
        }
    }
}

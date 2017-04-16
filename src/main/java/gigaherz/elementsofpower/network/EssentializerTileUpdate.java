package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.common.Used;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class EssentializerTileUpdate
        implements IMessage
{
    public BlockPos pos = new BlockPos(0, 0, 0);
    public MagicAmounts remaining = MagicAmounts.EMPTY;
    public ItemStack activeItem = ItemStack.EMPTY;

    @Used
    public EssentializerTileUpdate()
    {
    }

    public EssentializerTileUpdate(TileEssentializer essentializer)
    {
        this.pos = essentializer.getPos();
        this.activeItem = essentializer.getInventory().getStackInSlot(0);
        this.remaining = essentializer.remainingToConvert;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        pos = new BlockPos(
                buf.readInt(),
                buf.readInt(),
                buf.readInt());
        activeItem = ByteBufUtils.readItemStack(buf);
        remaining = new MagicAmounts(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeItemStack(buf, activeItem);
        remaining.writeTo(buf);
    }

    public static class Handler implements IMessageHandler<EssentializerTileUpdate, IMessage>
    {
        @Nullable
        @Override
        public IMessage onMessage(EssentializerTileUpdate message, MessageContext ctx)
        {
            ElementsOfPower.proxy.handleEssentializerTileUpdate(message);

            return null; // no response in this case
        }
    }
}

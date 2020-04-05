package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.client.ClientPacketHandlers;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.EssentializerTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateEssentializerTileEntity
{
    public BlockPos pos;
    public MagicAmounts remaining;
    public ItemStack activeItem;

    public UpdateEssentializerTileEntity(EssentializerTileEntity essentializer)
    {
        this.pos = essentializer.getPos();
        this.activeItem = essentializer.getInventory().getStackInSlot(0);
        this.remaining = essentializer.remainingToConvert;
    }

    public UpdateEssentializerTileEntity(PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        activeItem = buf.readItemStack();
        remaining = new MagicAmounts(buf);
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeItemStack(activeItem);
        remaining.writeTo(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleEssentializerTileUpdate(this);
    }
}

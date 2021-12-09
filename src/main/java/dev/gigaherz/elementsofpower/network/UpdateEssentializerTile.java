package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateEssentializerTile
{
    public BlockPos pos;
    public MagicAmounts remaining;
    public ItemStack activeItem;

    public UpdateEssentializerTile(EssentializerBlockEntity essentializer)
    {
        this.pos = essentializer.getBlockPos();
        this.activeItem = essentializer.getInventory().getStackInSlot(0);
        this.remaining = essentializer.remainingToConvert;
    }

    public UpdateEssentializerTile(FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
        activeItem = buf.readItem();
        remaining = new MagicAmounts(buf);
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeItem(activeItem);
        remaining.writeTo(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleEssentializerTileUpdate(this);
    }
}

package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class UpdateEssentializerTile implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("update_essentializer_tile");

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

    public void write(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeItem(activeItem);
        remaining.writeTo(buf);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientPacketHandlers.handleEssentializerTileUpdate(this);
    }
}

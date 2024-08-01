package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateEssentializerTile(
        BlockPos pos,
        MagicAmounts remaining,
        ItemStack activeItem
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("update_essentializer_tile");
    public static final Type<UpdateEssentializerTile> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEssentializerTile> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateEssentializerTile::pos,
            MagicAmounts.STREAM_CODEC, UpdateEssentializerTile::remaining,
            ItemStack.STREAM_CODEC, UpdateEssentializerTile::activeItem,
            UpdateEssentializerTile::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientPacketHandlers.handleEssentializerTileUpdate(this);
    }
}

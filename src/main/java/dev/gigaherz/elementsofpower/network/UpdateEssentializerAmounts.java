package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountHolder;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateEssentializerAmounts(
        int windowId,
        MagicAmounts contained,
        MagicAmounts remaining
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("update_essentializer_amounts");
    public static final Type<UpdateEssentializerAmounts> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEssentializerAmounts> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UpdateEssentializerAmounts::windowId,
            MagicAmounts.STREAM_CODEC, UpdateEssentializerAmounts::contained,
            MagicAmounts.STREAM_CODEC, UpdateEssentializerAmounts::remaining,
            UpdateEssentializerAmounts::new
    );

    public UpdateEssentializerAmounts(int windowId, IMagicAmountHolder essentializer)
    {
        this(windowId, essentializer.getContainedMagic(), essentializer.getRemainingToConvert());
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientPacketHandlers.handleRemainingAmountsUpdate(this);
    }
}

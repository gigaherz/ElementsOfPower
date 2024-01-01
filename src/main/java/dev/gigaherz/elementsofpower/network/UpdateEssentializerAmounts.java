package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountHolder;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class UpdateEssentializerAmounts implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("update_essentializer_amounts");

    public int windowId;
    public MagicAmounts contained;
    public MagicAmounts remaining;

    public UpdateEssentializerAmounts(int windowId, IMagicAmountHolder essentializer)
    {
        this.windowId = windowId;
        this.contained = essentializer.getContainedMagic();
        this.remaining = essentializer.getRemainingToConvert();
    }

    public UpdateEssentializerAmounts(FriendlyByteBuf buf)
    {
        windowId = buf.readInt();
        contained = new MagicAmounts(buf);
        remaining = new MagicAmounts(buf);
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        contained.writeTo(buf);
        remaining.writeTo(buf);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientPacketHandlers.handleRemainingAmountsUpdate(this);
    }
}

package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.essentializer.menu.IMagicAmountHolder;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateEssentializerAmounts
{
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

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(windowId);
        contained.writeTo(buf);
        remaining.writeTo(buf);
    }

    public boolean handle(Supplier<NetworkEvent.Context> context)
    {
        return ClientPacketHandlers.handleRemainingAmountsUpdate(this);
    }
}

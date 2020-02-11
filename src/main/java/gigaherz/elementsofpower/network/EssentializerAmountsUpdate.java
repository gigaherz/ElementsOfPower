package gigaherz.elementsofpower.network;

import gigaherz.elementsofpower.client.ClientPacketHandlers;
import gigaherz.elementsofpower.client.ClientProxy;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.essentializer.gui.IMagicAmountHolder;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EssentializerAmountsUpdate
{
    public int windowId;
    public MagicAmounts contained;
    public MagicAmounts remaining;

    public EssentializerAmountsUpdate(int windowId, IMagicAmountHolder essentializer)
    {
        this.windowId = windowId;
        this.contained = essentializer.getContainedMagic();
        this.remaining = essentializer.getRemainingToConvert();
    }

    public EssentializerAmountsUpdate(PacketBuffer buf)
    {
        windowId = buf.readInt();
        contained = new MagicAmounts(buf);
        remaining = new MagicAmounts(buf);
    }

    public void encode(PacketBuffer buf)
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

package gigaherz.elementsofpower.network;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.database.InternalConversionProcess;
import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

public class SyncEssenceConversions
{
    final Map<Item, MagicAmounts> data = Maps.newHashMap();

    public SyncEssenceConversions()
    {
        data.putAll(InternalConversionProcess.SERVER.getAllConversions());
    }

    public SyncEssenceConversions(PacketBuffer buffer)
    {
        int entries = buffer.readInt();
        for (int i = 0; i < entries; i++)
        {
            Item item = buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
            MagicAmounts am = new MagicAmounts(buffer);
            data.put(item, am);
        }
    }

    public void encode(PacketBuffer buffer)
    {
        buffer.writeInt(data.size());
        for (Map.Entry<Item, MagicAmounts> entry : data.entrySet())
        {
            buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, entry.getKey());
            entry.getValue().writeTo(buffer);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            InternalConversionProcess.CLIENT.receiveFromServer(this.data);
        });
        return true;
    }
}

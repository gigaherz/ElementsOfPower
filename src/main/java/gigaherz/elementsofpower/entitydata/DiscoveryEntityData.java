package gigaherz.elementsofpower.entitydata;

import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.Set;

public class DiscoveryEntityData implements IExtendedEntityProperties
{
    public static final String PROP_NAME = ElementsOfPower.MODID + "_DiscoveryData";

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    public static DiscoveryEntityData get(EntityPlayer p)
    {
        return (DiscoveryEntityData) p.getExtendedProperties(PROP_NAME);
    }

    final Map<String, String> discoveries = Maps.newHashMap();

    EntityPlayer player;
    World world;

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        for (Map.Entry<String, String> entry : discoveries.entrySet())
        {
            NBTTagCompound discoveries = new NBTTagCompound();

            discoveries.setString(entry.getKey(), entry.getValue());

            compound.setTag("Discoveries", discoveries);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        NBTTagCompound discoveries = compound.getCompoundTag("Discoveries");
        for (String key : discoveries.getKeySet())
        {
            String value = discoveries.getString(key);
            this.discoveries.put(key, value);
        }
    }

    @Override
    public void init(Entity entity, World world)
    {
        this.player = (EntityPlayer) entity;
        this.world = world;
    }

    public Set<String> getKnowledgeKeys()
    {
        return discoveries.keySet();
    }

    public static class Handler
    {
        @SubscribeEvent
        public void entityConstruct(EntityEvent.EntityConstructing e)
        {
            if (e.entity instanceof EntityPlayer)
            {
                if (e.entity.getExtendedProperties(PROP_NAME) == null)
                    e.entity.registerExtendedProperties(PROP_NAME, new SpellcastEntityData());
            }
        }

        @SubscribeEvent
        public void playerTickEvent(TickEvent.PlayerTickEvent e)
        {
            if (e.phase == TickEvent.Phase.END)
            {
                SpellcastEntityData data = SpellcastEntityData.get(e.player);
                if (data != null)
                    data.updateSpell();
            }
        }
    }
}

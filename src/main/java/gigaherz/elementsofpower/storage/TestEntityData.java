package gigaherz.elementsofpower.storage;

import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TestEntityData implements IExtendedEntityProperties
{
    public static final String PROP_ID = ElementsOfPower.MODID + "_TestEntityData";

    /**
     * Call this during (pre-)init to activate the entity extension
     */
    public static void initHandling()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    /**
     * Call this to get the entity data for a given entity.
     * @param player The player to get the data from.
     * @return Returns the instance, if present.
     */
    public static TestEntityData get(EntityPlayer player)
    {
        return (TestEntityData)player.getExtendedProperties(PROP_ID);
    }

    int manaPoints = 5;

    @Override
    public void init(Entity entity, World world)
    {
        // Initialize Entity- or World-dependant values here.
        // NOTE: This is called when crossing dimensions, but the object is NOT recreated,
        // and loadNBTData is NOT called again.
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("MP", manaPoints);

        compound.setTag(PROP_ID, tag);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        if(compound.hasKey(PROP_ID))
        {
            NBTTagCompound tag = (NBTTagCompound)compound.getTag(PROP_ID);

            if (tag.hasKey("MP"))
                manaPoints = tag.getInteger("MP");
        }
    }

    public static class Handler
    {
        @SubscribeEvent
        public void entityConstructing(EntityEvent.EntityConstructing event)
        {
            if (!(event.entity instanceof EntityPlayer) || get((EntityPlayer)event.entity) != null)
                return;

            event.entity.registerExtendedProperties(PROP_ID, new TestEntityData());
        }
    }
}

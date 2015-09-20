package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityBeamBase;
import gigaherz.elementsofpower.entities.EntityFireBeam;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpellBeam
        extends SpellBase
{

    int power;
    float maxDistance;
    int effectInterval;
    int timeToLive;

    Class<? extends EntityBeamBase> clazz;

    public SpellBeam(Class<? extends EntityBeamBase> clazz, int power, int ticks)
    {
        this.clazz = clazz;
        this.power = power;
        this.maxDistance = 10;
        this.effectInterval = 30;
        this.timeToLive = effectInterval * ticks;
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;

        world.spawnEntityInWorld(new EntityFireBeam(world, player, maxDistance, power, effectInterval, timeToLive));
        /*try
        {
            //(World worldIn, EntityLivingBase caster, float maxDistance, int power, int effectInterval, int timeToLive)
            world.spawnEntityInWorld(clazz
                    .getConstructor(World.class, EntityLivingBase.class, float.class, int.class, int.class, int.class)
                    .newInstance(world, player, maxDistance, power, effectInterval, timeToLive));
        }
        catch (ReflectiveOperationException e)
        {
            throw new ReportedException(new CrashReport("Exception spawning Spell Entity", e));
        }*/
    }
}

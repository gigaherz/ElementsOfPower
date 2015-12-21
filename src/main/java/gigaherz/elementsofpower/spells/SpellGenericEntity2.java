package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityBallBase;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

public class SpellGenericEntity2
        extends SpellBase
{

    int power;
    boolean spawnSources;

    Class<? extends EntityBallBase> clazz;

    public SpellGenericEntity2(Class<? extends EntityBallBase> clazz, int power, boolean spawnSources)
    {
        this.clazz = clazz;
        this.power = power;
        this.spawnSources = spawnSources;
    }

    @Override
    public float getScale() { return 1 + 0.25f * power; }

    @Override
    public int getPower()
    {
        return power;
    }

    @Override
    public ISpellcast castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;

        try
        {
            if (world.spawnEntityInWorld(clazz.getConstructor(World.class, int.class, boolean.class, EntityLivingBase.class).newInstance(world, power, spawnSources, player)))
                return getNewCast();

            return null;
        }
        catch (ReflectiveOperationException e)
        {
            throw new ReportedException(new CrashReport("Exception spawning Spell Entity", e));
        }
    }
}

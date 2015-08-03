package gigaherz.elementsofpower.database.spells;

import gigaherz.elementsofpower.entities.EntityBallBase;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

public class SpellGenericEntity
        extends SpellBase
{

    int power;

    Class<? extends EntityBallBase> clazz;

    public SpellGenericEntity(Class<? extends EntityBallBase> clazz, int power)
    {
        this.clazz = clazz;
        this.power = power;
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;

        try
        {
            world.spawnEntityInWorld(clazz.getConstructor(World.class, int.class, EntityLivingBase.class).newInstance(world, power, player));
        }
        catch (ReflectiveOperationException e)
        {
            throw new ReportedException(new CrashReport("Exception spawning Spell Entity", e));
        }
    }
}

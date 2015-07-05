
package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityBallBase;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

public class SpellGenericEntity2
        extends SpellBase {

    int power;
    boolean spawnSources;

    Class<? extends EntityBallBase> clazz;

    public SpellGenericEntity2(Class<? extends EntityBallBase> clazz, int power, boolean spawnSources) {
        this.clazz = clazz;
        this.power = power;
        this.spawnSources=spawnSources;
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer player) {
        World world = player.worldObj;

        try {
            world.spawnEntityInWorld(clazz.getConstructor(World.class, int.class, boolean.class, EntityLivingBase.class).newInstance(world, power, spawnSources, player));
        }
        catch(ReflectiveOperationException e)
        {
            throw new ReportedException(new CrashReport("Exception spawning Spell Entity", e));
        }
    }
}

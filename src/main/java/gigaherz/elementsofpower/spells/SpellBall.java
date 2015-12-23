package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.cast.ISpellcast;
import gigaherz.elementsofpower.spells.cast.balls.BallBase;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;

public class SpellBall
        extends SpellBase<SpellBall, BallBase>
{
    boolean spawnSources = false;

    Class<? extends BallBase> effect;

    public SpellBall spawnSourceBlocks()
    {
        spawnSources = true;
        return this;
    }

    public SpellBall(Class<? extends BallBase> effect)
    {
        this.effect = effect;
    }

    @Override
    public BallBase getNewCast()
    {
        try
        {
            return effect.getConstructor(SpellBall.class).newInstance(this);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
        {
            throw new ReportedException(new CrashReport("Error creating spellcast instance", e));
        }
    }

    @Override
    public ISpellcast castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        BallBase cast = getNewCast();
        EntityBall entity = new EntityBall(world, cast, player);

        if (world.spawnEntityInWorld(entity))
            return cast;

        return null;
    }
}

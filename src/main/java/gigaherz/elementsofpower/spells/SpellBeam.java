package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.spells.cast.ISpellcast;
import gigaherz.elementsofpower.spells.cast.beams.BeamBase;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;

import java.lang.reflect.InvocationTargetException;

public class SpellBeam
        extends SpellBase<SpellBeam, BeamBase>
{
    public int effectInterval;
    int timeToLive;
    Class<? extends BeamBase> effect;

    public SpellBeam(Class<? extends BeamBase> effect, int effectInterval, int ticks)
    {
        this.effect = effect;
        this.effectInterval = effectInterval;
        this.timeToLive = this.effectInterval * ticks;
    }

    @Override
    public boolean isBeam()
    {
        return true;
    }

    @Override
    public int getDuration()
    {
        return timeToLive;
    }

    @Override
    public BeamBase getNewCast()
    {
        try
        {
            return effect.getConstructor(SpellBeam.class).newInstance(this);
        }
        catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
        {
            throw new ReportedException(new CrashReport("Error creating spellcast instance", e));
        }
    }

    @Override
    public ISpellcast castSpell(ItemStack stack, EntityPlayer player)
    {
        return getNewCast();
    }
}

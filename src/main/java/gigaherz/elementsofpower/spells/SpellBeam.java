package gigaherz.elementsofpower.spells;

import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;

import java.lang.reflect.InvocationTargetException;

public class SpellBeam
        extends SpellBase
{
    int power;
    int effectInterval;
    int timeToLive;
    Class<? extends SpellcastBeam> effect;

    public SpellBeam(Class<? extends SpellcastBeam> effect, int power, int ticks)
    {
        this.effect = effect;
        this.power = power;
        this.effectInterval = 2;
        this.timeToLive = effectInterval * ticks;
    }

    @Override
    public boolean isBeam() { return true; }

    @Override
    public int getDuration() {return timeToLive; }

    @Override
    public int getPower()
    {
        return power;
    }

    @Override
    public float getScale() { return 1 + 0.25f * power; }

    @Override
    public ISpellcast getNewCast()
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

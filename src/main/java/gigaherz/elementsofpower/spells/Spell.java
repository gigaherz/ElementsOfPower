package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.spells.cast.Spellcast;
import gigaherz.elementsofpower.spells.cast.effects.SpellEffect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class Spell<S extends Spell, C extends Spellcast>
{
    protected MagicAmounts spellCost = new MagicAmounts();
    protected StringBuilder spellSequence = new StringBuilder();
    protected String finalSequence = null;
    protected int power = 0;

    public final SpellEffect effect;

    protected Spell(SpellEffect effect)
    {
        this.effect = effect;
    }

    public float getScale()
    {
        return 1 + 0.25f * power;
    }

    public int getPower()
    {
        return power;
    }

    public abstract C getNewCast();

    public abstract C castSpell(ItemStack stack, EntityPlayer player);

    public MagicAmounts getSpellCost()
    {
        return spellCost;
    }

    public String getSequence()
    {
        if (finalSequence == null)
        {
            finalSequence = spellSequence.toString();
            spellSequence = null;
        }
        return finalSequence;
    }

    @SuppressWarnings("unchecked")
    protected S self()
    {
        return (S) this;
    }

    public S power(int power)
    {
        this.power = power;
        return self();
    }

    public S amount(int which)
    {
        if (spellSequence == null)
        {
            spellSequence = new StringBuilder();
            finalSequence = null;
        }
        spellCost.amounts[which]++;
        spellSequence.append(SpellManager.elementChars[which]);
        return self();
    }

    public S amountMultiple(int which, int count)
    {
        while (count > 0)
        {
            amount(which);
            count--;
        }
        return self();
    }

    public S cost(float totalCost)
    {
        float totalAmounts = spellCost.getTotalMagic();
        for (int i = 0; i < spellCost.amounts.length; i++)
        {
            spellCost.amounts[i] = (spellCost.amounts[i] * totalCost / totalAmounts);
        }
        return self();
    }

    public S fire()
    {
        return amount(0);
    }

    public S water()
    {
        return amount(1);
    }

    public S air()
    {
        return amount(2);
    }

    public S earth()
    {
        return amount(3);
    }

    public S light()
    {
        return amount(4);
    }

    public S darkness()
    {
        return amount(5);
    }

    public S life()
    {
        return amount(6);
    }

    public S death()
    {
        return amount(7);
    }

    public S fire(int count)
    {
        return amountMultiple(0, count);
    }

    public S water(int count)
    {
        return amountMultiple(1, count);
    }

    public S air(int count)
    {
        return amountMultiple(2, count);
    }

    public S earth(int count)
    {
        return amountMultiple(3, count);
    }

    public S light(int count)
    {
        return amountMultiple(4, count);
    }

    public S darkness(int count)
    {
        return amountMultiple(5, count);
    }

    public S life(int count)
    {
        return amountMultiple(6, count);
    }

    public S death(int count)
    {
        return amountMultiple(7, count);
    }
}

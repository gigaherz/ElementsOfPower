package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.spells.cast.ISpellcast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class SpellBase<Sub extends SpellBase, Effect extends ISpellcast>
        implements ISpellEffect
{
    protected MagicAmounts spellCost = new MagicAmounts();
    protected StringBuilder spellSequence = new StringBuilder();
    protected String finalSequence = null;
    protected int color = 0xFFFFFF;
    protected int power = 0;

    @Override
    public int getColor() { return color; }

    @Override
    public float getScale() { return 1 + 0.25f * power; }

    @Override
    public boolean isBeam() { return false; }

    @Override
    public int getDuration() {return 0; }

    @Override
    public int getPower()
    {
        return power;
    }

    @Override
    public abstract Effect getNewCast();

    @Override
    public abstract ISpellcast castSpell(ItemStack stack, EntityPlayer player);

    @Override
    public MagicAmounts getSpellCost()
    {
        return spellCost;
    }

    @Override
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
    protected Sub self() {return (Sub)this; }

    public Sub color(int color) { this.color=color; return self(); }
    public Sub power(int power) { this.power=power; return self(); }

    protected Sub amount(int which)
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

    protected Sub amountMultiple(int which, int count)
    {
        while (count > 0)
        {
            amount(which);
            count--;
        }
        return self();
    }

    public Sub cost(float totalCost)
    {
        float totalAmounts = spellCost.getTotalMagic();
        for (int i = 0; i < spellCost.amounts.length; i++)
        {
            spellCost.amounts[i] = (spellCost.amounts[i] * totalCost / totalAmounts);
        }
        return self();
    }

    public Sub fire()
    {
        return amount(0);
    }

    public Sub water()
    {
        return amount(1);
    }

    public Sub air()
    {
        return amount(2);
    }

    public Sub earth()
    {
        return amount(3);
    }

    public Sub light()
    {
        return amount(4);
    }

    public Sub darkness()
    {
        return amount(5);
    }

    public Sub life()
    {
        return amount(6);
    }

    public Sub death()
    {
        return amount(7);
    }

    public Sub fire(int count)
    {
        return amountMultiple(0, count);
    }

    public Sub water(int count)
    {
        return amountMultiple(1, count);
    }

    public Sub air(int count)
    {
        return amountMultiple(2, count);
    }

    public Sub earth(int count)
    {
        return amountMultiple(3, count);
    }

    public Sub light(int count)
    {
        return amountMultiple(4, count);
    }

    public Sub darkness(int count)
    {
        return amountMultiple(5, count);
    }

    public Sub life(int count)
    {
        return amountMultiple(6, count);
    }

    public Sub death(int count)
    {
        return amountMultiple(7, count);
    }
}

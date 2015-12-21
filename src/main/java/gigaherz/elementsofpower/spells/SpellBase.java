package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.SpellManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class SpellBase
        implements ISpellEffect
{
    public class Spellcast implements ISpellcast<SpellBase>
    {
        @Override
        public float getRemainingCastTime()
        {
            return 0;
        }

        @Override
        public void init(World world, EntityPlayer player)
        {

        }

        @Override
        public SpellBase getEffect() {return SpellBase.this; }

        @Override
        public void update()
        {
        }

        @Override
        public void readFromNBT(NBTTagCompound tagData)
        {
        }

        @Override
        public void writeToNBT(NBTTagCompound tagData)
        {
        }
    }

    protected MagicAmounts spellCost = new MagicAmounts();
    protected StringBuilder spellSequence = new StringBuilder();
    protected String finalSequence = null;
    protected int color = 0xFFFFFF;

    @Override
    public ISpellEffect withColor(int color) { this.color=color; return this; }

    @Override
    public int getColor() { return color; }

    @Override
    public float getScale() { return 1; }

    @Override
    public boolean isBeam() { return false; }

    @Override
    public int getDuration() {return 0; }

    @Override
    public int getPower()
    {
        return 0;
    }

    @Override
    public ISpellcast getNewCast()
    {
        return new Spellcast();
    }

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

    protected SpellBase amount(int which)
    {
        if (spellSequence == null)
        {
            spellSequence = new StringBuilder();
            finalSequence = null;
        }
        spellCost.amounts[which]++;
        spellSequence.append(SpellManager.elementChars[which]);
        return this;
    }

    protected SpellBase amountMultiple(int which, int count)
    {
        while (count > 0)
        {
            amount(which);
            count--;
        }
        return this;
    }

    public SpellBase cost(float totalCost)
    {
        double totalAmounts = spellCost.getTotalMagic();
        for (int i = 0; i < spellCost.amounts.length; i++)
        {
            spellCost.amounts[i] = (int) Math.ceil(spellCost.amounts[i] * totalCost / totalAmounts);
        }
        return this;
    }

    public SpellBase fire()
    {
        return amount(0);
    }

    public SpellBase water()
    {
        return amount(1);
    }

    public SpellBase air()
    {
        return amount(2);
    }

    public SpellBase earth()
    {
        return amount(3);
    }

    public SpellBase light()
    {
        return amount(4);
    }

    public SpellBase darkness()
    {
        return amount(5);
    }

    public SpellBase life()
    {
        return amount(6);
    }

    public SpellBase death()
    {
        return amount(7);
    }

    public SpellBase fire(int count)
    {
        return amountMultiple(0, count);
    }

    public SpellBase water(int count)
    {
        return amountMultiple(1, count);
    }

    public SpellBase air(int count)
    {
        return amountMultiple(2, count);
    }

    public SpellBase earth(int count)
    {
        return amountMultiple(3, count);
    }

    public SpellBase light(int count)
    {
        return amountMultiple(4, count);
    }

    public SpellBase darkness(int count)
    {
        return amountMultiple(5, count);
    }

    public SpellBase life(int count)
    {
        return amountMultiple(6, count);
    }

    public SpellBase death(int count)
    {
        return amountMultiple(7, count);
    }
}

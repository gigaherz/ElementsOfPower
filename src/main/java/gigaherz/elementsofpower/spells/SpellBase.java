package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.SpellManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class SpellBase
        implements ISpellEffect {

    public static final int[] spellCostLevel = {5, 8, 12, 25, 50, 250, 500, 750};

    protected MagicAmounts spellCost = new MagicAmounts();
    protected StringBuilder spellSequence = new StringBuilder();
    protected String finalSequence = null;

    @Override
    public abstract void castSpell(ItemStack stack, EntityPlayer player);

    @Override
    public MagicAmounts getSpellCost() {
        return spellCost;
    }

    public String getSequence() {
        if (finalSequence == null) {
            finalSequence = spellSequence.toString();
            spellSequence = null;
        }
        return finalSequence;
    }

    protected SpellBase amount(int which) {
        if (spellSequence == null) {
            spellSequence = new StringBuilder();
            finalSequence = null;
        }
        if(spellSequence.length() >= spellCostLevel.length)
        {
            System.out.println("ERROR: Spell too long, truncating...");
            return this;
        }
        spellCost.amounts[which] += spellCostLevel[spellSequence.length()];
        spellSequence.append(SpellManager.elementChars[which]);
        return this;
    }

    public SpellBase fire() {
        return amount(0);
    }

    public SpellBase water() {
        return amount(1);
    }

    public SpellBase air() {
        return amount(2);
    }

    public SpellBase earth() {
        return amount(3);
    }

    public SpellBase light() {
        return amount(4);
    }

    public SpellBase darkness() {
        return amount(5);
    }

    public SpellBase life() {
        return amount(6);
    }

    public SpellBase death() {
        return amount(7);
    }
}

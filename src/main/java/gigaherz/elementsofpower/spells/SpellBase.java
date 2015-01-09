package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.MagicAmounts;
import gigaherz.elementsofpower.SpellUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class SpellBase
        implements ISpellEffect {

    public static final int[] spellCostLevel = { 1, 3, 9, 27, 81, 243, 635 };

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
        if(spellSequence == null) {
            spellSequence = new StringBuilder();
            finalSequence = null;
        }
        spellCost.amounts[which] += spellCostLevel[spellSequence.length()];
        spellSequence.append(SpellUtils.elementChars[which]);
        return this;
    }

    public SpellBase fire() { return amount(0); }
    public SpellBase water() { return amount(1); }
    public SpellBase air() { return amount(2); }
    public SpellBase earth() { return amount(3); }
    public SpellBase light() { return amount(4); }
    public SpellBase darkness() { return amount(5); }
    public SpellBase life() { return amount(6); }
    public SpellBase death() { return amount(7); }
}

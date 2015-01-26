package gigaherz.elementsofpower;

import gigaherz.elementsofpower.spells.ISpellEffect;
import gigaherz.elementsofpower.spells.SpellBase;
import gigaherz.elementsofpower.spells.SpellFireball;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Hashtable;
import java.util.Map;

public class SpellManager {

    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static Map<Character, Integer> elementIndices = new Hashtable<Character, Integer>();

    public static final Map<String, ISpellEffect> spellRegistration = new Hashtable<String, ISpellEffect>();

    static {

        for (int i = 0; i < 8; i++) {
            elementIndices.put(elementChars[i], i);
        }

        registerSpell(new SpellFireball(0, false).fire());
        registerSpell(new SpellFireball(1, true).fire().earth());
        registerSpell(new SpellFireball(2, true).fire().fire().earth());
        registerSpell(new SpellFireball(3, true).fire().fire().fire().earth());
        registerSpell(new SpellFireball(4, true).fire().fire().fire().fire().earth());
        registerSpell(new SpellFireball(5, true).fire().fire().fire().fire().fire().earth());
        registerSpell(new SpellFireball(6, true).fire().fire().fire().fire().fire().fire().earth());

    }

    public static void registerSpell(SpellBase spell) {
        spellRegistration.put(spell.getSequence(), spell);
    }

    public static void registerSpell(String sequence, ISpellEffect spell) {
        spellRegistration.put(sequence, spell);
    }
}

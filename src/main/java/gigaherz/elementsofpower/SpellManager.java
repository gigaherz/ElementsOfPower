package gigaherz.elementsofpower;

import gigaherz.elementsofpower.spells.*;

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

        registerSpell(new SpellBlastball(0, true).fire());
        registerSpell(new SpellBlastball(1, true).fire().earth());
        registerSpell(new SpellBlastball(2, true).fire().fire().earth());
        registerSpell(new SpellBlastball(3, true).fire().fire().fire().earth());
        registerSpell(new SpellBlastball(4, true).fire().fire().fire().fire().earth());
        registerSpell(new SpellBlastball(5, true).fire().fire().fire().fire().fire().earth());
        registerSpell(new SpellBlastball(6, true).fire().fire().fire().fire().fire().fire().earth());

        registerSpell(new SpellFrostball(1).water().air());
        registerSpell(new SpellFrostball(2).water().water().air());
        registerSpell(new SpellFrostball(3).water().water().water().air());
        registerSpell(new SpellFrostball(4).water().water().water().water().air());
        registerSpell(new SpellFrostball(5).water().water().water().water().water().air());
        registerSpell(new SpellFrostball(6).water().water().water().water().water().water().air());

        registerSpell(new SpellWaterball(1).water());
        registerSpell(new SpellWaterball(2).water().water());
        registerSpell(new SpellWaterball(3).water().water().water());
        registerSpell(new SpellWaterball(4).water().water().water().water());
        registerSpell(new SpellWaterball(5).water().water().water().water().water());
        registerSpell(new SpellWaterball(6).water().water().water().water().water().water());

        registerSpell(new SpellFireball(1).fire());
        registerSpell(new SpellFireball(2).fire().fire());
        registerSpell(new SpellFireball(3).fire().fire().fire());
        registerSpell(new SpellFireball(4).fire().fire().fire().fire());
        registerSpell(new SpellFireball(5).fire().fire().fire().fire().fire());
        registerSpell(new SpellFireball(6).fire().fire().fire().fire().fire().fire());

        registerSpell(new SpellAirball(1).air());
        registerSpell(new SpellAirball(2).air().air());
        registerSpell(new SpellAirball(3).air().air().air());
        registerSpell(new SpellAirball(4).air().air().air().air());
        registerSpell(new SpellAirball(5).air().air().air().air().air());
        registerSpell(new SpellAirball(6).air().air().air().air().air().air());
    }

    public static void registerSpell(SpellBase spell) {
        spellRegistration.put(spell.getSequence(), spell);
    }

    public static void registerSpell(String sequence, ISpellEffect spell) {
        spellRegistration.put(sequence, spell);
    }
}

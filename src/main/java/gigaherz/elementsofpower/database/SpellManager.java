package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.spells.*;
import gigaherz.elementsofpower.entities.*;

import java.util.Hashtable;
import java.util.Map;

public class SpellManager
{
    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static Map<Character, Integer> elementIndices = new Hashtable<Character, Integer>();

    public static final Map<String, ISpellEffect> spellRegistration = new Hashtable<String, ISpellEffect>();

    public static final int COST_ELEMENTARY = 8;
    public static final int COST_SIMPLE = 24;
    public static final int COST_AVERAGE = 72;
    public static final int COST_COMPLEX = 216;
    public static final int COST_EXTREME = 648;
    public static final float COMBO_COEF = 1.2f;

    static
    {

        for (int i = 0; i < 8; i++)
        {
            elementIndices.put(elementChars[i], i);
        }

        registerSpell(new SpellBlastball(1, true).fire().earth().cost(COST_ELEMENTARY * COMBO_COEF));
        registerSpell(new SpellBlastball(2, true).fire().fire().earth().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellBlastball(4, true).fire().fire().fire().earth().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellBlastball(6, true).fire().fire().fire().fire().earth().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBlastball(8, true).fire().fire().fire().fire().fire().earth().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellGenericEntity(EntityFrostball.class, 1).water().air().cost(COST_ELEMENTARY * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityFrostball.class, 2).water().water().air().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityFrostball.class, 3).water().water().water().air().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityFrostball.class, 4).water().water().water().water().air().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityFrostball.class, 6).water().water().water().water().water().air().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 1, false).water().cost(COST_ELEMENTARY));
        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 2, false).water().water().cost(COST_SIMPLE));
        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 3, false).water().water().water().cost(COST_AVERAGE));
        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 4, false).water().water().water().water().cost(COST_COMPLEX));
        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 6, false).water().water().water().water().water().cost(COST_EXTREME));

        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 1, true).water().life().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 2, true).water().water().life().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 3, true).water().water().water().life().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellGenericEntity2(EntityWaterball.class, 4, true).water().water().water().water().life().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellGenericEntity(EntityFlameball.class, 1).fire().cost(COST_ELEMENTARY));
        registerSpell(new SpellGenericEntity(EntityFlameball.class, 2).fire().fire().cost(COST_SIMPLE));
        registerSpell(new SpellGenericEntity(EntityFlameball.class, 3).fire().fire().fire().cost(COST_AVERAGE));
        registerSpell(new SpellGenericEntity(EntityFlameball.class, 4).fire().fire().fire().fire().cost(COST_COMPLEX));
        registerSpell(new SpellGenericEntity(EntityFlameball.class, 5).fire().fire().fire().fire().fire().cost(COST_EXTREME));

        registerSpell(new SpellGenericEntity(EntityAirball.class, 1).air().cost(COST_ELEMENTARY));
        registerSpell(new SpellGenericEntity(EntityAirball.class, 2).air().air().cost(COST_SIMPLE));
        registerSpell(new SpellGenericEntity(EntityAirball.class, 3).air().air().air().cost(COST_AVERAGE));
        registerSpell(new SpellGenericEntity(EntityAirball.class, 4).air().air().air().air().cost(COST_COMPLEX));
        registerSpell(new SpellGenericEntity(EntityAirball.class, 5).air().air().air().air().air().cost(COST_EXTREME));

        registerSpell(new SpellGenericEntity(EntityDustball.class, 1).earth().cost(COST_ELEMENTARY * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityDustball.class, 2).earth().earth().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityDustball.class, 3).earth().earth().earth().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityDustball.class, 4).earth().earth().earth().earth().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellGenericEntity(EntityDustball.class, 5).earth().earth().earth().earth().earth().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellGenericEntity2(EntityLavaball.class, 2, false).earth().earth().fire().fire().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellGenericEntity2(EntityLavaball.class, 4, false).earth().earth().earth().fire().fire().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellGenericEntity2(EntityLavaball.class, 1, true).earth().earth().fire().life().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellGenericEntity2(EntityLavaball.class, 2, true).earth().earth().earth().fire().life().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellGenericEntity(EntityLifeball.class, 1).life().cost(COST_ELEMENTARY));
        registerSpell(new SpellGenericEntity(EntityLifeball.class, 2).life().life().cost(COST_SIMPLE));
        registerSpell(new SpellGenericEntity(EntityLifeball.class, 3).life().life().life().cost(COST_AVERAGE));
        registerSpell(new SpellGenericEntity(EntityLifeball.class, 4).life().life().life().life().cost(COST_COMPLEX));
        registerSpell(new SpellGenericEntity(EntityLifeball.class, 5).life().life().life().life().life().cost(COST_EXTREME));

        registerSpell(new SpellResurrection().life().life().life().light().light().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellBeam(EntityBeamBase.class, 2, 5).fire().darkness().cost(COST_AVERAGE));
    }

    public static void registerSpell(SpellBase spell)
    {
        spellRegistration.put(spell.getSequence(), spell);
    }

    public static void registerSpell(String sequence, ISpellEffect spell)
    {
        spellRegistration.put(sequence, spell);
    }
}

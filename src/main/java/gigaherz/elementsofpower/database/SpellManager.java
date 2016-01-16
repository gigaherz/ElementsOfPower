package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.spells.*;
import gigaherz.elementsofpower.spells.cast.effects.*;

import java.util.Hashtable;
import java.util.Map;

public class SpellManager
{
    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static Map<Character, Integer> elementIndices = new Hashtable<>();

    public static final Map<String, Spell> spellRegistration = new Hashtable<>();

    public static final int COST_ELEMENTARY = 8;
    public static final int COST_SIMPLE = COST_ELEMENTARY * 3;
    public static final int COST_AVERAGE = COST_SIMPLE * 3;
    public static final int COST_COMPLEX = COST_AVERAGE * 3;
    public static final int COST_EXTREME = COST_COMPLEX * 3;
    public static final float COMBO_COEF = 1.2f;

    static
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            elementIndices.put(elementChars[i], i);
        }

        registerSpell(new SpellBall(new BlastEffect()).power(1).fire().earth().cost(COST_ELEMENTARY * COMBO_COEF));
        registerSpell(new SpellBall(new BlastEffect()).power(2).fire(2).earth().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellBall(new BlastEffect()).power(4).fire(3).earth().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellBall(new BlastEffect()).power(6).fire(4).earth().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBall(new BlastEffect()).power(8).fire(5).earth().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellBall(new FrostEffect()).power(1).water().air().cost(COST_ELEMENTARY * COMBO_COEF));
        registerSpell(new SpellBall(new FrostEffect()).power(2).water(2).air().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellBall(new FrostEffect()).power(3).water(3).air().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellBall(new FrostEffect()).power(4).water(4).air().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBall(new FrostEffect()).power(6).water(5).air().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellBall(new WaterEffect(true)).power(1).water().life().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellBall(new WaterEffect(true)).power(2).water(2).life().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellBall(new WaterEffect(true)).power(3).water(3).life().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBall(new WaterEffect(true)).power(4).water(4).life().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellBall(new CushionEffect()).power(2).earth().air().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellBall(new CushionEffect()).power(3).earth(2).air().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellBall(new CushionEffect()).power(4).earth(3).air().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBall(new CushionEffect()).power(5).earth(4).air().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellBall(new LavaEffect(false)).power(2).earth(2).fire(2).cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBall(new LavaEffect(false)).power(4).earth(3).fire(2).cost(COST_EXTREME * COMBO_COEF));
        registerSpell(new SpellBall(new LavaEffect(true)).power(1).earth(2).fire().life().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBall(new LavaEffect(true)).power(2).earth(3).fire().life().cost(COST_EXTREME * COMBO_COEF));

        registerSpell(new SpellResurrection().life(3).light(2).cost(COST_EXTREME * COMBO_COEF));
        registerSpell(new SpellBall(new LifeEffect()).light().darkness().cost(COST_ELEMENTARY * COMBO_COEF));

        registerSpell(new SpellBeam(new MiningEffect()).power(2).death().darkness().cost(COST_SIMPLE));
        registerSpell(new SpellBeam(new MiningEffect()).power(4).death(2).darkness().cost(COST_AVERAGE));
        registerSpell(new SpellBeam(new MiningEffect()).power(6).death(3).darkness().cost(COST_COMPLEX));
        registerSpell(new SpellBeam(new MiningEffect()).power(8).death(4).darkness().cost(COST_EXTREME));

        registerStandardSequence(new FireEffect(), MagicAmounts.FIRE);
        registerStandardSequence(new WaterEffect(false), MagicAmounts.WATER);
        registerStandardSequence(new AirEffect(), MagicAmounts.AIR);
        registerStandardSequence(new DustEffect(), MagicAmounts.EARTH);
        registerStandardSequence(new LifeEffect(), MagicAmounts.LIFE);
        registerStandardSequence(new MiningEffect(), MagicAmounts.DEATH);
    }

    public static void registerStandardSequence(SpellEffect effect, int baseElement)
    {
        registerSpell(new SpellBall(effect).power(1).amountMultiple(baseElement,1).cost(COST_ELEMENTARY));
        registerSpell(new SpellBall(effect).power(2).amountMultiple(baseElement,2).cost(COST_SIMPLE));
        registerSpell(new SpellBall(effect).power(3).amountMultiple(baseElement,3).cost(COST_AVERAGE));
        registerSpell(new SpellBall(effect).power(4).amountMultiple(baseElement,4).cost(COST_COMPLEX));
        registerSpell(new SpellBall(effect).power(5).amountMultiple(baseElement,5).cost(COST_EXTREME));

        registerSpell(new SpellBeam(effect).power(1).amountMultiple(baseElement,1).darkness().cost(COST_SIMPLE * COMBO_COEF));
        registerSpell(new SpellBeam(effect).power(2).amountMultiple(baseElement,2).darkness().cost(COST_AVERAGE * COMBO_COEF));
        registerSpell(new SpellBeam(effect).power(3).amountMultiple(baseElement,3).darkness().cost(COST_COMPLEX * COMBO_COEF));
        registerSpell(new SpellBeam(effect).power(4).amountMultiple(baseElement,4).darkness().cost(COST_EXTREME * COMBO_COEF));
    }

    public static void registerSpell(Spell spell)
    {
        spellRegistration.put(spell.getSequence(), spell);
    }

    public static Spell findSpell(String sequence)
    {
        return spellRegistration.get(sequence);
    }
}

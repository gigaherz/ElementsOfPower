package gigaherz.elementsofpower.database;

import gigaherz.elementsofpower.spells.*;
import gigaherz.elementsofpower.spells.cast.Blastball;
import gigaherz.elementsofpower.spells.cast.balls.*;
import gigaherz.elementsofpower.spells.cast.beams.Firebeam;
import gigaherz.elementsofpower.spells.cast.beams.Miningbeam;

import java.util.Hashtable;
import java.util.Map;

public class SpellManager
{
    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static Map<Character, Integer> elementIndices = new Hashtable<>();

    public static final Map<String, ISpellEffect> spellRegistration = new Hashtable<>();

    public static final int COST_ELEMENTARY = 8;
    public static final int COST_SIMPLE = COST_ELEMENTARY*3;
    public static final int COST_AVERAGE = COST_SIMPLE*3;
    public static final int COST_COMPLEX = COST_AVERAGE*3;
    public static final int COST_EXTREME = COST_COMPLEX*3;
    public static final float COMBO_COEF = 1.2f;

    static
    {
        for (int i = 0; i < 8; i++)
        {
            elementIndices.put(elementChars[i], i);
        }

        registerSpell(new SpellBlastball().power(1).fire().earth().cost(COST_ELEMENTARY * COMBO_COEF).color(0x0020af));
        registerSpell(new SpellBlastball().power(2).fire(2).earth().cost(COST_SIMPLE * COMBO_COEF).color(0x0020af));
        registerSpell(new SpellBlastball().power(4).fire(3).earth().cost(COST_AVERAGE * COMBO_COEF).color(0x0020af));
        registerSpell(new SpellBlastball().power(6).fire(4).earth().cost(COST_COMPLEX * COMBO_COEF).color(0x0020af));
        registerSpell(new SpellBlastball().power(8).fire(5).earth().cost(COST_EXTREME * COMBO_COEF).color(0x0020af));

        registerSpell(new SpellBall(Frostball.class).power(1).water().air().cost(COST_ELEMENTARY * COMBO_COEF).color(0xff6644));
        registerSpell(new SpellBall(Frostball.class).power(2).water(2).air().cost(COST_SIMPLE * COMBO_COEF).color(0xff6644));
        registerSpell(new SpellBall(Frostball.class).power(3).water(3).air().cost(COST_AVERAGE * COMBO_COEF).color(0xff6644));
        registerSpell(new SpellBall(Frostball.class).power(4).water(4).air().cost(COST_COMPLEX * COMBO_COEF).color(0xff6644));
        registerSpell(new SpellBall(Frostball.class).power(6).water(5).air().cost(COST_EXTREME * COMBO_COEF).color(0xff6644));

        registerSpell(new SpellBall(Waterball.class).power(1).water().cost(COST_ELEMENTARY).color(0xff0000));
        registerSpell(new SpellBall(Waterball.class).power(2).water(2).cost(COST_SIMPLE).color(0xff0000));
        registerSpell(new SpellBall(Waterball.class).power(3).water(3).cost(COST_AVERAGE).color(0xff0000));
        registerSpell(new SpellBall(Waterball.class).power(4).water(4).cost(COST_COMPLEX).color(0xff0000));
        registerSpell(new SpellBall(Waterball.class).power(6).water(5).cost(COST_EXTREME).color(0xff0000));

        registerSpell(new SpellBall(Waterball.class).power(1).water().life().cost(COST_SIMPLE * COMBO_COEF).spawnSourceBlocks().color(0xff0000));
        registerSpell(new SpellBall(Waterball.class).power(2).water(2).life().cost(COST_AVERAGE * COMBO_COEF).spawnSourceBlocks().color(0xff0000));
        registerSpell(new SpellBall(Waterball.class).power(3).water(3).life().cost(COST_COMPLEX * COMBO_COEF).spawnSourceBlocks().color(0xff0000));
        registerSpell(new SpellBall(Waterball.class).power(4).water(4).life().cost(COST_EXTREME * COMBO_COEF).spawnSourceBlocks().color(0xff0000));

        registerSpell(new SpellBall(Flameball.class).power(1).fire().cost(COST_ELEMENTARY).color(0x0000ff));
        registerSpell(new SpellBall(Flameball.class).power(2).fire().fire().cost(COST_SIMPLE).color(0x0000ff));
        registerSpell(new SpellBall(Flameball.class).power(3).fire().fire().fire().cost(COST_AVERAGE).color(0x0000ff));
        registerSpell(new SpellBall(Flameball.class).power(4).fire().fire().fire().fire().cost(COST_COMPLEX).color(0x0000ff));
        registerSpell(new SpellBall(Flameball.class).power(5).fire().fire().fire().fire().fire().cost(COST_EXTREME).color(0x0000ff));

        registerSpell(new SpellBall(Airball.class).power(1).air().cost(COST_ELEMENTARY).color(0x44ffff));
        registerSpell(new SpellBall(Airball.class).power(2).air(2).cost(COST_SIMPLE).color(0x44ffff));
        registerSpell(new SpellBall(Airball.class).power(3).air(3).cost(COST_AVERAGE).color(0x44ffff));
        registerSpell(new SpellBall(Airball.class).power(4).air(4).cost(COST_COMPLEX).color(0x44ffff));
        registerSpell(new SpellBall(Airball.class).power(5).air(5).cost(COST_EXTREME).color(0x44ffff));

        registerSpell(new SpellBall(Dustball.class).power(1).earth().cost(COST_ELEMENTARY).color(0x304050));
        registerSpell(new SpellBall(Dustball.class).power(2).earth(2).cost(COST_SIMPLE).color(0x304050));
        registerSpell(new SpellBall(Dustball.class).power(3).earth(3).cost(COST_AVERAGE).color(0x304050));
        registerSpell(new SpellBall(Dustball.class).power(4).earth(4).cost(COST_COMPLEX).color(0x304050));
        registerSpell(new SpellBall(Dustball.class).power(5).earth(5).cost(COST_EXTREME).color(0x304050));

        registerSpell(new SpellBall(Cushion.class).power(2).earth().air().cost(COST_SIMPLE * COMBO_COEF).spawnSourceBlocks().color(0x8090a0));
        registerSpell(new SpellBall(Cushion.class).power(3).earth(2).air().cost(COST_AVERAGE * COMBO_COEF).spawnSourceBlocks().color(0x8090a0));
        registerSpell(new SpellBall(Cushion.class).power(4).earth(3).air().cost(COST_COMPLEX * COMBO_COEF).spawnSourceBlocks().color(0x8090a0));
        registerSpell(new SpellBall(Cushion.class).power(5).earth(4).air().cost(COST_EXTREME * COMBO_COEF).spawnSourceBlocks().color(0x8090a0));

        registerSpell(new SpellBall(Lavaball.class).power(2).earth(2).fire(2).cost(COST_COMPLEX * COMBO_COEF).color(0x2080ff));
        registerSpell(new SpellBall(Lavaball.class).power(4).earth(3).fire(2).cost(COST_EXTREME * COMBO_COEF).color(0x2080ff));
        registerSpell(new SpellBall(Lavaball.class).power(1).earth(2).fire().life().cost(COST_COMPLEX * COMBO_COEF).spawnSourceBlocks().color(0x2080ff));
        registerSpell(new SpellBall(Lavaball.class).power(2).earth(3).fire().life().cost(COST_EXTREME * COMBO_COEF).spawnSourceBlocks().color(0x2080ff));

        registerSpell(new SpellBall(Lifeball.class).power(1).life().cost(COST_ELEMENTARY).color(0xafffaf));
        registerSpell(new SpellBall(Lifeball.class).power(2).life(2).cost(COST_SIMPLE).color(0xafffaf));
        registerSpell(new SpellBall(Lifeball.class).power(3).life(3).cost(COST_AVERAGE).color(0xafffaf));
        registerSpell(new SpellBall(Lifeball.class).power(4).life(4).cost(COST_COMPLEX).color(0xafffaf));
        registerSpell(new SpellBall(Lifeball.class).power(5).life(5).cost(COST_EXTREME).color(0xafffaf));

        registerSpell(new SpellResurrection().life(3).light(2).cost(COST_EXTREME * COMBO_COEF).color(0xcfffcf));

        registerSpell(new SpellTeleport().light().darkness().cost(COST_ELEMENTARY * COMBO_COEF).color(0x008000));

        registerSpell(new SpellBeam(Firebeam.class, 2, 25).power(2).fire().darkness().cost(COST_AVERAGE).color(0x0000ff));
        registerSpell(new SpellBeam(Firebeam.class, 2, 40).power(4).fire(2).darkness().cost(COST_COMPLEX).color(0x0040ff));
        registerSpell(new SpellBeam(Firebeam.class, 2, 60).power(6).fire(3).darkness().cost(COST_EXTREME).color(0x00a0ff));

        registerSpell(new SpellBeam(Miningbeam.class, 10, 8).power(2).death().darkness().cost(COST_SIMPLE).color(0x222222));
        registerSpell(new SpellBeam(Miningbeam.class, 8, 10).power(5).death(2).darkness().cost(COST_AVERAGE).color(0x444444));
        registerSpell(new SpellBeam(Miningbeam.class, 5, 20).power(6).death(3).darkness().cost(COST_COMPLEX).color(0x666666));
        registerSpell(new SpellBeam(Miningbeam.class, 2, 60).power(9).death(4).darkness().cost(COST_EXTREME).color(0x888888));
    }

    public static void registerSpell(ISpellEffect spell)
    {
        spellRegistration.put(spell.getSequence(), spell);
    }

    public static ISpellEffect findSpell(String sequence)
    {
        return spellRegistration.get(sequence);
    }
}

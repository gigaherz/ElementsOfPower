package gigaherz.elementsofpower.spells;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Effect;
import gigaherz.elementsofpower.gemstones.Element;
import gigaherz.elementsofpower.gemstones.Shape;
import gigaherz.elementsofpower.spells.effects.*;
import gigaherz.elementsofpower.spells.shapes.*;

import java.util.EnumMap;
import java.util.List;

public class SpellManager
{
    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static int[] elementIndices = new int['Z' - 'A' + 1];

    public static final SpellShape sphere = new SphereShape();
    public static final SpellShape ball = new BallShape();
    public static final SpellShape beam = new BeamShape();
    public static final SpellShape cone = new ConeShape();
    public static final SpellShape self = new SelfShape();
    public static final SpellShape single = new SingleShape();

    public static final SpellEffect flame = new FlameEffect();
    public static final SpellEffect water = new WaterEffect(false);
    public static final SpellEffect wind = new WindEffect();
    public static final SpellEffect dust = new DustEffect();
    public static final SpellEffect mist = new MistEffect();
    public static final SpellEffect light = new LightEffect();
    public static final SpellEffect mining = new MiningEffect();
    public static final SpellEffect healing = new HealthEffect();
    public static final SpellEffect breaking = new WitherEffect();
    public static final SpellEffect cushion = new CushionEffect();
    public static final SpellEffect lava = new LavaEffect(false);
    public static final SpellEffect resurrection = new ResurrectionEffect();
    public static final SpellEffect waterSource = new WaterEffect(true);
    public static final SpellEffect lavaSource = new LavaEffect(true);
    public static final SpellEffect teleport = new TeleportEffect();

    static
    {
        for (int i = 0; i < elementIndices.length; i++)
        {
            elementIndices[i] = -1;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            elementIndices[elementChars[i] - 'A'] = i;
        }
    }

    public static Spellcast makeSpell(String sequence)
    {
        SpellBuilder b = SpellBuilder.begin();
        for (char c : sequence.toCharArray())
        {
            b.next(c);
        }
        return b.build(sequence);
    }

    static class SpellBuilder
    {
        static final EnumMap<Effect, SpellEffect> effects = Maps.newEnumMap(Effect.class);
        static final EnumMap<Shape, SpellShape> shapes = Maps.newEnumMap(Shape.class);

        static
        {
            shapes.put(Shape.Sphere, sphere);
            shapes.put(Shape.Ball, ball);
            shapes.put(Shape.Beam, beam);
            shapes.put(Shape.Cone, cone);
            shapes.put(Shape.Self, self);
            shapes.put(Shape.Single, single);
            effects.put(Effect.Flame, flame);
            effects.put(Effect.Water, water);
            effects.put(Effect.Wind, wind);
            effects.put(Effect.Dust, dust);
            effects.put(Effect.Mist, mist);
            effects.put(Effect.Light, light);
            effects.put(Effect.Mining, mining);
            effects.put(Effect.Healing, healing);
            effects.put(Effect.Breaking, breaking);
            effects.put(Effect.Cushion, cushion);
            effects.put(Effect.Lava, lava);
            effects.put(Effect.Resurrection, resurrection);
            effects.put(Effect.WaterSource, waterSource);
            effects.put(Effect.LavaSource, lavaSource);
            effects.put(Effect.Teleport, teleport);
        }

        Element primary = null;
        Element last = null;
        Shape shape = null;
        Effect effect = null;
        int primaryPower = 0;
        int empowering = 0; // can be negative!

        List<Element> sequence = Lists.newArrayList();

        private SpellBuilder()
        {
        }

        public static SpellBuilder begin()
        {
            return new SpellBuilder();
        }

        public void next(char c)
        {
            switch (Character.toUpperCase(c))
            {
                case 'F':
                    apply(Element.Fire);
                    break;
                case 'W':
                    apply(Element.Water);
                    break;
                case 'A':
                    apply(Element.Air);
                    break;
                case 'E':
                    apply(Element.Earth);
                    break;
                case 'G':
                    apply(Element.Light);
                    break;
                case 'K':
                    apply(Element.Darkness);
                    break;
                case 'L':
                    apply(Element.Life);
                    break;
                case 'D':
                    apply(Element.Death);
                    break;
            }
        }

        public Spellcast build(String sequence)
        {
            if (this.effect == null)
                return null;

            SpellEffect effect = effects.get(this.effect);
            SpellShape shape = shapes.get(this.shape);
            Spellcast cast = new Spellcast(shape, effect, primaryPower, sequence);

            if (empowering != 0)
                cast.setEmpowering(empowering);

            cast.setSpellCost(computeCost());

            return cast;
        }

        private MagicAmounts computeCost()
        {
            MagicAmounts amounts = new MagicAmounts();

            if (sequence.size() > 0)
            {
                HashMultiset<Element> multiset = HashMultiset.create();
                multiset.addAll(sequence);

                float total = (float) Math.ceil(Math.pow(3, sequence.size()) * (1 + 0.6 * multiset.size()));

                for (Multiset.Entry<Element> e : multiset.entrySet())
                {
                    amounts.amounts[e.getElement().ordinal()] += total * e.getCount() / sequence.size();
                }
            }

            return amounts;
        }

        private void apply(Element e)
        {
            if (primary == null)
            {
                setPrimary(e);
            }
            else if (primary == e && last == e)
            {
                augmentPrimary();
            }
            else
            {
                addModifier(e);
            }
            last = e;
        }

        private void setPrimary(Element e)
        {
            primary = e;
            primaryPower = 1;
            switch (e)
            {
                case Fire:
                    effect = Effect.Flame;
                    break;
                case Water:
                    effect = Effect.Water;
                    break;
                case Air:
                    effect = Effect.Wind;
                    break;
                case Earth:
                    effect = Effect.Dust;
                    break;
                case Light:
                    effect = Effect.Light;
                    break;
                case Darkness:
                    effect = Effect.Mining;
                    break;
                case Life:
                    effect = Effect.Healing;
                    break;
                case Death:
                    effect = Effect.Breaking;
                    break;
            }
            shape = e.getShape();
            sequence.add(e);
        }

        private void augmentPrimary()
        {
            primaryPower++;
            sequence.add(primary);
        }

        private void addModifier(Element e)
        {
            if (sequence.get(sequence.size() - 1) == e.getOpposite())
            {
                sequence.remove(sequence.size() - 1);
                if (sequence.size() == 0)
                {
                    reset();
                    return;
                }
            }

            shape = e.getShape();

            switch (e)
            {
                case Fire:
                    empowering++;
                    break;
                case Water:
                    empowering--;
                    break;
                case Air:
                    switch (effect)
                    {
                        case Flame:
                            break;
                        case Wind:
                            break;
                        case Dust:
                            effect = Effect.Cushion;
                            break;
                        case Cushion:
                            break;
                        case Water:
                            effect = Effect.Mist;
                            break;
                        case Lava:
                            effect = Effect.Flame;
                            break;
                        case Light:
                            reset();
                            break;
                        case Mining:
                            reset();
                            break;
                        case Healing:
                            reset();
                            break;
                        case Breaking:
                            reset();
                            break;
                        case Resurrection:
                            reset();
                            break;
                        case WaterSource:
                            reset();
                            break;
                        case LavaSource:
                            effect = Effect.Flame;
                            break;
                    }
                    break;
                case Life:
                    switch (effect)
                    {
                        default:
                            break;
                        case Mining:
                            effect = Effect.Teleport;
                            shape = Shape.Ball;
                            break;
                    }
                    break;
            }

            sequence.add(e);
        }

        private void reset()
        {
            effect = null;
            shape = null;
            last = null;
            primaryPower = 0;
            sequence.clear();
        }
    }
}

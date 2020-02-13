package gigaherz.elementsofpower.spells;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.effects.*;
import gigaherz.elementsofpower.spells.shapes.*;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

public class SpellManager
{
    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static int[] elementIndices = new int['Z' - 'A' + 1];

    public static final SpellShape sphere = new SphereShape();
    public static final SpellShape ball = new BallShape();
    public static final SpellShape beam = new LaserShape(); // FIXME: new BeamShape();
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

    @Nullable
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
            shapes.put(Shape.SPHERE, sphere);
            shapes.put(Shape.BALL, ball);
            shapes.put(Shape.BEAM, beam);
            shapes.put(Shape.CONE, cone);
            shapes.put(Shape.SELF, self);
            shapes.put(Shape.SINGLE, single);
            effects.put(Effect.FLAME, flame);
            effects.put(Effect.WATER, water);
            effects.put(Effect.WIND, wind);
            effects.put(Effect.DUST, dust);
            effects.put(Effect.MIST, mist);
            effects.put(Effect.LIGHT, light);
            effects.put(Effect.MINING, mining);
            effects.put(Effect.HEALING, healing);
            effects.put(Effect.BREAKING, breaking);
            effects.put(Effect.CUSHION, cushion);
            effects.put(Effect.LAVA, lava);
            effects.put(Effect.RESURRECTION, resurrection);
            effects.put(Effect.WATER_SOURCE, waterSource);
            effects.put(Effect.LAVA_SOURCE, lavaSource);
            effects.put(Effect.TELEPORT, teleport);
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
                    apply(Element.FIRE);
                    break;
                case 'W':
                    apply(Element.WATER);
                    break;
                case 'A':
                    apply(Element.AIR);
                    break;
                case 'E':
                    apply(Element.EARTH);
                    break;
                case 'G':
                    apply(Element.LIGHT);
                    break;
                case 'K':
                    apply(Element.DARKNESS);
                    break;
                case 'L':
                    apply(Element.LIFE);
                    break;
                case 'D':
                    apply(Element.DEATH);
                    break;
            }
        }

        @Nullable
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
            MagicAmounts amounts = MagicAmounts.EMPTY;

            if (sequence.size() > 0)
            {
                HashMultiset<Element> multiset = HashMultiset.create();
                multiset.addAll(sequence);

                float total = (float) Math.ceil(Math.pow(3, sequence.size()) * (1 + 0.6 * multiset.size()));

                for (Multiset.Entry<Element> e : multiset.entrySet())
                {
                    amounts = amounts.add(e.getElement(), total * e.getCount() / sequence.size());
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
                case FIRE:
                    effect = Effect.FLAME;
                    break;
                case WATER:
                    effect = Effect.WATER;
                    break;
                case AIR:
                    effect = Effect.WIND;
                    break;
                case EARTH:
                    effect = Effect.DUST;
                    break;
                case LIGHT:
                    effect = Effect.LIGHT;
                    break;
                case DARKNESS:
                    effect = Effect.MINING;
                    break;
                case LIFE:
                    effect = Effect.HEALING;
                    break;
                case DEATH:
                    effect = Effect.BREAKING;
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
            if (sequence.size() > 0 && sequence.get(sequence.size() - 1) == e.getOpposite())
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
                case FIRE:
                    empowering++;
                    break;
                case WATER:
                    empowering--;
                    break;
                case AIR:
                    switch (effect)
                    {
                        case FLAME:
                            break;
                        case WIND:
                            break;
                        case DUST:
                            effect = Effect.CUSHION;
                            break;
                        case CUSHION:
                            break;
                        case WATER:
                            effect = Effect.MIST;
                            break;
                        case LAVA:
                            effect = Effect.FLAME;
                            break;
                        case LIGHT:
                            reset();
                            break;
                        case MINING:
                            reset();
                            break;
                        case HEALING:
                            reset();
                            break;
                        case BREAKING:
                            reset();
                            break;
                        case RESURRECTION:
                            reset();
                            break;
                        case WATER_SOURCE:
                            reset();
                            break;
                        case LAVA_SOURCE:
                            effect = Effect.FLAME;
                            break;
                    }
                    break;
                case LIFE:
                    switch (effect)
                    {
                        default:
                            break;
                        case MINING:
                            effect = Effect.TELEPORT;
                            shape = Shape.BALL;
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

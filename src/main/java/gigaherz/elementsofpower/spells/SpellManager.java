package gigaherz.elementsofpower.spells;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.spells.effects.*;
import gigaherz.elementsofpower.spells.shapes.*;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SpellManager
{
    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static int[] elementIndices = new int['Z' - 'A' + 1];

    public static final SpellShape SPHERE = new SphereShape();
    public static final SpellShape BALL = new BallShape();
    public static final SpellShape BEAM = new LaserShape(); // FIXME: new BeamShape();
    public static final SpellShape CONE = new ConeShape();
    public static final SpellShape SELF = new SelfShape();
    //public static final SpellShape SPIKE = new SpikeShape();
    //public static final SpellShape WALL = new WallShape();
    //public static final SpellShape SHIELD = new ShieldShape();
    public static final SpellShape SINGLE = new SingleShape();

    public static final SpellEffect FLAME = new FlameEffect();
    public static final SpellEffect FROST = new FrostEffect();
    public static final SpellEffect WATER = new WaterEffect(false);
    public static final SpellEffect WIND = new WindEffect();
    public static final SpellEffect DUST = new DustEffect();
    public static final SpellEffect MIST = new MistEffect();
    public static final SpellEffect LIGHT = new LightEffect();
    public static final SpellEffect MINING = new MiningEffect();
    public static final SpellEffect HEALING = new HealthEffect();
    public static final SpellEffect BREAKING = new WitherEffect();
    public static final SpellEffect CUSHION = new CushionEffect();
    public static final SpellEffect LAVA = new LavaEffect(false);
    public static final SpellEffect RESURRECTION = new ResurrectionEffect();
    public static final SpellEffect WATER_SOURCE = new WaterEffect(true);
    public static final SpellEffect LAVA_SOURCE = new LavaEffect(true);
    public static final SpellEffect TELEPORT = new TeleportEffect();

    static
    {
        Arrays.fill(elementIndices, -1);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            elementIndices[elementChars[i] - 'A'] = i;
        }
    }

    public static List<Element> sequenceFromList(ListNBT seq)
    {
        return seq.stream().map(e -> Element.byName(e.getString())).collect(Collectors.toList());
    }

    public static ListNBT sequenceToList(List<Element> sequence)
    {
        ListNBT list = new ListNBT();
        for (Element e : sequence)
        { list.add(StringNBT.valueOf(e.getName())); }
        return list;
    }

    @Nullable
    public static Spellcast makeSpell(ListNBT seq)
    {
        if (seq.size() == 0)
            return null;
        List<Element> sequence = sequenceFromList(seq);
        return makeSpell(sequence);
    }

    @Nullable
    public static Spellcast makeSpell(List<Element> sequence)
    {
        if (sequence.size() == 0)
            return null;
        SpellBuilder b = new SpellBuilder();
        return b.build(sequence);
    }

    private static class SpellBuilder
    {
        private SpellState spellState = SpellState.START;
        private Element last = null;

        private Element primary = null;
        private Effect effect = null;
        private int primaryPower = 0;
        private int empowering = 0; // can be negative!
        private int radiance = 0; // can be negative!

        private List<Element> sequence = Lists.newArrayList();

        @Nullable
        public Spellcast build(List<Element> sequence)
        {
            for (Element c : sequence)
            {
                if (!addElement(c))
                    return null;
            }

            switch (spellState)
            {
                case PRIMARY:
                    increasePrimary();
                    break;
                case SECONDARY:
                    applySecondary(last);
                    break;
                case AUGMENT:
                    applyAugment(last);
                    break;
                default:
                    // do nothing
                    break;
            }

            if (this.effect == null)
                return null;

            SpellEffect effect = effects.get(this.effect);
            SpellShape shape = shapes.get(last.getShape());
            Spellcast cast = new Spellcast(shape, effect, primaryPower, sequence);

            if (empowering != 0)
                cast.setEmpowering(empowering);

            if (radiance != 0)
                cast.setRadiating(empowering);

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

        private boolean addElement(Element e)
        {
            return processTransition(this, e);
        }

        private boolean isOpposite(Element e)
        {
            return last.getOpposite() == e;
        }

        private boolean isPrimary(Element e)
        {
            return primary == e;
        }

        private boolean canMutate(Element modifier)
        {
            return getMutationResult(effect, modifier) != null;
        }

        boolean transition(Element e, SpellState primary)
        {
            // Update last
            last = e;
            // Next state
            spellState = primary;
            return true;
        }

        private void setPrimary(Element e)
        {
            primary = e;
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
        }

        @Nullable
        private Effect getMutationResult(Effect base, Element modifier)
        {
            switch (modifier)
            {
                case EARTH:
                    switch (base)
                    {
                        case FLAME:
                            return Effect.LAVA;
                        case LAVA:
                            return Effect.LAVA_SOURCE;
                        default:
                            return null;
                    }
                case AIR:
                    switch (base)
                    {
                        case DUST:
                            return Effect.CUSHION;
                        case WATER:
                            return Effect.MIST;
                        case LAVA_SOURCE:
                            return Effect.LAVA;
                        case LAVA:
                            return Effect.FLAME;
                    }
                    break;
                case LIFE:
                    switch (base)
                    {
                        case MINING:
                            return Effect.TELEPORT;
                        case WIND:
                            return Effect.WIND;
                    }
                    break;
                case DARKNESS:
                    switch (base)
                    {
                        case FLAME:
                            return Effect.FROST;
                        case WIND:
                            return Effect.WIND;
                    }
                    break;
            }
            return null;
        }

        private void increasePrimary()
        {
            primaryPower++;
            sequence.add(primary);
        }

        private void recordPrimary()
        {
            sequence.add(primary);
        }

        private boolean applySecondary(Element e)
        {
            effect = getMutationResult(effect, e);
            if (effect == null)
                return invalid();

            sequence.add(e);
            return true;
        }

        private boolean applyAugment(Element e)
        {
            switch (e)
            {
                case FIRE:
                    empowering++;
                    break;
                case WATER:
                    empowering--;
                    break;
                case LIGHT:
                    radiance++;
                    break;
                case AIR:
                    radiance--;
                    break;
                default:
                    return invalid();
            }

            sequence.add(e);
            return true;
        }

        private boolean invalid()
        {
            spellState = SpellState.INVALID;
            effect = null;
            last = null;
            primaryPower = 0;
            sequence.clear();
            return false;
        }


        private static final Map<Character, Element> elements = Maps.newHashMap();
        private static final EnumMap<Effect, SpellEffect> effects = Maps.newEnumMap(Effect.class);
        private static final EnumMap<Shape, SpellShape> shapes = Maps.newEnumMap(Shape.class);
        private static final EnumMap<SpellState, List<Pair<BiPredicate<SpellBuilder, Element>, BiPredicate<SpellBuilder, Element>>>> transitions = Maps.newEnumMap(SpellState.class);

        static
        {
            elements.put('F', Element.FIRE);
            elements.put('W', Element.WATER);
            elements.put('A', Element.AIR);
            elements.put('E', Element.EARTH);
            elements.put('G', Element.LIGHT);
            elements.put('K', Element.DARKNESS);
            elements.put('L', Element.LIFE);
            elements.put('D', Element.DEATH);

            shapes.put(Shape.SPHERE, SPHERE);
            shapes.put(Shape.BALL, BALL);
            shapes.put(Shape.BEAM, BEAM);
            shapes.put(Shape.CONE, CONE);
            shapes.put(Shape.SELF, SELF);
            shapes.put(Shape.SINGLE, SINGLE);

            effects.put(Effect.FLAME, FLAME);
            effects.put(Effect.FROST, FROST);
            effects.put(Effect.WATER, WATER);
            effects.put(Effect.WIND, WIND);
            effects.put(Effect.DUST, DUST);
            effects.put(Effect.MIST, MIST);
            effects.put(Effect.LIGHT, LIGHT);
            effects.put(Effect.MINING, MINING);
            effects.put(Effect.HEALING, HEALING);
            effects.put(Effect.BREAKING, BREAKING);
            effects.put(Effect.CUSHION, CUSHION);
            effects.put(Effect.LAVA, LAVA);
            effects.put(Effect.RESURRECTION, RESURRECTION);
            effects.put(Effect.WATER_SOURCE, WATER_SOURCE);
            effects.put(Effect.LAVA_SOURCE, LAVA_SOURCE);
            effects.put(Effect.TELEPORT, TELEPORT);

            transitions.put(SpellState.START, Collections.singletonList(
                    unconditional(makeTransition(SpellBuilder::setPrimary, SpellState.PRIMARY))
            ));
            transitions.put(SpellState.PRIMARY, Lists.newArrayList(
                    conditional(SpellBuilder::isPrimary, makeTransition(SpellBuilder::increasePrimary, SpellState.PRIMARY)),
                    conditional(SpellBuilder::isOpposite, makeOppositeTransition(SpellBuilder::recordPrimary, SpellState.PRIMARY_CANCEL)),
                    conditional(SpellBuilder::canMutate, makeTransition(SpellBuilder::increasePrimary, SpellState.SECONDARY)),
                    unconditional(makeTransition(SpellBuilder::increasePrimary, SpellState.AUGMENT))
            ));
            transitions.put(SpellState.PRIMARY_CANCEL, Lists.newArrayList(
                    conditional(SpellBuilder::canMutate, makeTransition(SpellState.SECONDARY)),
                    unconditional(makeTransition(SpellBuilder::increasePrimary, SpellState.AUGMENT))
            ));
            transitions.put(SpellState.SECONDARY, Lists.newArrayList(
                    conditional(SpellBuilder::isOpposite, makeOppositeTransition(SpellState.MODIFIER_CANCEL)),
                    conditional(SpellBuilder::canMutate, makeTransition(SpellBuilder::applySecondary, SpellState.SECONDARY)),
                    unconditional(makeTransition(SpellBuilder::applySecondary, SpellState.AUGMENT))
            ));
            transitions.put(SpellState.AUGMENT, Lists.newArrayList(
                    conditional(SpellBuilder::isOpposite, makeOppositeTransition(SpellState.MODIFIER_CANCEL)),
                    unconditional(makeTransition(SpellBuilder::applyAugment, SpellState.AUGMENT))
            ));
            transitions.put(SpellState.MODIFIER_CANCEL, Lists.newArrayList(
                    unconditional(makeTransition(SpellState.AUGMENT))
            ));
        }

        private static boolean processTransition(SpellBuilder b, Element e)
        {
            for (Pair<BiPredicate<SpellBuilder, Element>, BiPredicate<SpellBuilder, Element>> item : transitions.computeIfAbsent(b.spellState, state -> Lists.newArrayList(
                    unconditional(makeTransition(logUnimplementedState(), SpellState.INVALID))
            )))
            {
                if (item.getLeft().test(b, e))
                {
                    return item.getRight().test(b, e);
                }
            }
            ElementsOfPowerMod.LOGGER.error("Spell sequence transition was incomplete");
            return b.invalid();
        }

        private static Pair<BiPredicate<SpellBuilder, Element>, BiPredicate<SpellBuilder, Element>> conditional(BiPredicate<SpellBuilder, Element> condition, BiPredicate<SpellBuilder, Element> transition)
        {
            return Pair.of(condition, transition);
        }

        private static Pair<BiPredicate<SpellBuilder, Element>, BiPredicate<SpellBuilder, Element>> unconditional(BiPredicate<SpellBuilder, Element> transition)
        {
            return Pair.of((b, e) -> true, transition);
        }

        private static BiPredicate<SpellBuilder, Element> makeTransition(BiPredicate<SpellBuilder, Element> action, SpellState next)
        {
            return (b, e) -> {
                if (!action.test(b, e))
                    return false;

                return b.transition(e, next);
            };
        }

        private static BiPredicate<SpellBuilder, Element> makeTransition(BiConsumer<SpellBuilder, Element> action, SpellState next)
        {
            return (b, e) -> {
                action.accept(b, e);

                return b.transition(e, next);
            };
        }

        private static BiPredicate<SpellBuilder, Element> makeTransition(Consumer<SpellBuilder> action, SpellState next)
        {
            return (b, e) -> {
                action.accept(b);

                return b.transition(e, next);
            };
        }

        private static BiPredicate<SpellBuilder, Element> makeTransition(SpellState next)
        {
            return (b, e) -> b.transition(e, next);
        }

        private static BiPredicate<SpellBuilder, Element> makeOppositeTransition(Consumer<SpellBuilder> action, SpellState next)
        {
            return (b, e) -> {
                action.accept(b);

                return b.transition(b.last, next);
            };
        }

        private static BiPredicate<SpellBuilder, Element> makeOppositeTransition(SpellState next)
        {
            return (b, e) -> b.transition(b.last, next);
        }

        private static BiPredicate<SpellBuilder, Element> logUnimplementedState()
        {
            return (b, e) -> {
                ElementsOfPowerMod.LOGGER.error("Spell sequence transitioned to invalid state.");
                return false;
            };
        }

        private enum SpellState
        {
            START,      // The initial state of the spell, before any element has been added
            PRIMARY,    // Primary chain, the first element repeated multiple times
            PRIMARY_CANCEL,     // The last element was opposite of the previous, so we shouldn't process the previous modifier
            SECONDARY,  // Direct modifiers which mutate the spell effect
            AUGMENT,    // Augmentations: modifiers that change how the spell behaves
            MODIFIER_CANCEL,     // The last element was opposite of the previous, so we shouldn't process the previous modifier
            END,        // Final state, the spell is complete and the last seen element dictates the shape.
            INVALID
        }
    }
}

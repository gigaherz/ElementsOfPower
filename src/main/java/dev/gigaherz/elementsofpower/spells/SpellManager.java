package dev.gigaherz.elementsofpower.spells;

import com.google.common.collect.*;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SpellManager
{
    public final static char[] elementChars = {'F', 'W', 'A', 'E', 'G', 'K', 'L', 'D'};
    public final static int[] elementIndices = new int['Z' - 'A' + 1];

    static
    {
        Arrays.fill(elementIndices, -1);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            elementIndices[elementChars[i] - 'A'] = i;
        }
    }

    public static List<Element> sequenceFromList(ListTag seq)
    {
        return seq.stream().map(e -> Element.byName(e.getAsString())).collect(Collectors.toList());
    }

    public static List<Element> sequenceFromList(List<String> seq)
    {
        return seq.stream().map(Element::byName).collect(Collectors.toList());
    }

    public static List<String> sequenceToList(List<Element> sequence)
    {
        List<String> list = new ArrayList<>();
        for (Element e : sequence)
            list.add(e.getName());
        return list;
    }

    @Nullable
    public static Spellcast makeSpell(ListTag seq)
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

        for (Element c : sequence)
        {
            if (!b.doTransition(c))
                return null;
        }

        return b.build();
    }

    public static MagicAmounts computeCost(Spellcast cast)
    {
        MagicAmounts amounts = MagicAmounts.EMPTY;

        var sequence = cast.sequence();
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

    public static int getChargeDuration(List<Element> seq)
    {
        // temporary math
        return seq.size() * 10; // keep in sync with MagicContainerOverlay#setRunes
    }

    private static class SpellBuilder
    {
        private SpellState spellState = SpellState.START;
        private Element last = null;

        private Element previous = null;
        private EffectType effectType = null;
        private int primaryPower = 0;
        private int empowering = 0; // can be negative!
        private int radiance = 0; // can be negative!

        private final List<Element> sequence = Lists.newArrayList();
        private final EnumMap<ElementUsageType, Integer> usageTypes = new EnumMap<>(ElementUsageType.class);

        @Nullable
        public Spellcast build()
        {
            finalTransition();

            if (this.effectType == null)
                return null;

            SpellEffect effect = effects.get(this.effectType);
            if (effect == null)
                return null;

            SpellShape shape = shapes.get(last.getShape());
            if (shape == null)
                return null;

            return new Spellcast(sequence, shape, effect, primaryPower, empowering, radiance, /*timing*/0);
        }

        private boolean doTransition(Element e)
        {
            for (Transition item : transitions.computeIfAbsent(spellState, state -> Lists.newArrayList(
                    always(makeTransition(logUnimplementedState(), SpellState.INVALID))
            )))
            {
                if (item.test(this, e))
                {
                    return item.apply(this, e);
                }
            }
            ElementsOfPowerMod.LOGGER.error("Spell sequence transition was incomplete");
            return invalid();
        }

        private void finalTransition()
        {
            switch (spellState)
            {
                case PRIMARY -> increasePrimary();
                case SECONDARY -> applySecondary(last);
                case AUGMENT -> applyAugment(last);
                default -> {
                    // do nothing
                }
            }
        }

        private boolean isRepeat(Element e)
        {
            return previous == e;
        }

        private boolean canMix(Element modifier)
        {
            return getMixResult(effectType, modifier) != null;
        }

        private void transition(Element e, SpellState primary)
        {
            // Update last
            last = e;
            // Next state
            spellState = primary;
        }

        private void addInitial(Element e)
        {
            effectType = e.getInitialEffect();
            addToSequence(e, ElementUsageType.INITIAL);
        }

        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        @Nullable
        private EffectType getMixResult(EffectType base, Element modifier)
        {
            return switch (base)
                    {
                        case FLAME -> switch(modifier)
                                {
                                    case EARTH -> EffectType.LAVA; //
                                    case TIME -> EffectType.FROST;
                                    default -> null;
                                };
                        case LAVA -> switch (modifier)
                                {
                                    case EARTH -> EffectType.LAVA_SOURCE;
                                    case AIR -> EffectType.FLAME;
                                    default -> null;
                                };
                        case LAVA_SOURCE -> switch (modifier)
                                {
                                    case AIR -> EffectType.FLAME;
                                    default -> null;
                                };
                        case DUST -> switch (modifier)
                                {
                                    case AIR -> EffectType.CUSHION;
                                    default -> null;
                                };
                        case WATER -> switch (modifier)
                                {
                                    case AIR -> EffectType.MIST;
                                    default -> null;
                                };
                        case MINING -> switch (modifier)
                                {
                                    case LIFE -> EffectType.TELEPORT;
                                    default -> null;
                                };
                        default -> null;
                    };
        }

        private void addToSequence(Element e, ElementUsageType type)
        {
            previous = e;
            sequence.add(e);
            usageTypes.compute(type, (__, value) -> value != null ? value+1 : 1);
        }

        private void increasePrimary()
        {
            primaryPower++;
            addToSequence(previous, ElementUsageType.EMPOWER);
        }

        private boolean applySecondary(Element e)
        {
            effectType = getMixResult(effectType, e);
            if (effectType == null)
                return invalid();

            addToSequence(e, ElementUsageType.MIX);
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

            addToSequence(e, ElementUsageType.MODIFY);
            return true;
        }

        private boolean invalid()
        {
            spellState = SpellState.INVALID;
            effectType = null;
            last = null;
            primaryPower = 0;
            sequence.clear();
            return false;
        }

        private static final Map<EffectType, SpellEffect> effects = Maps.immutableEnumMap(ImmutableMap.<EffectType, SpellEffect>builder()
                .put(EffectType.BREAKING, SpellEffects.BREAKING)
                .put(EffectType.CUSHION, SpellEffects.CUSHION)
                .put(EffectType.DUST, SpellEffects.DUST)
                .put(EffectType.FLAME, SpellEffects.FLAME)
                .put(EffectType.FROST, SpellEffects.FROST)
                .put(EffectType.HEALING, SpellEffects.HEALING)
                .put(EffectType.LAVA, SpellEffects.LAVA)
                .put(EffectType.LAVA_SOURCE, SpellEffects.LAVA_SOURCE)
                .put(EffectType.LIGHT, SpellEffects.LIGHT)
                .put(EffectType.MINING, SpellEffects.MINING)
                .put(EffectType.MIST, SpellEffects.MIST)
                .put(EffectType.PUSH, SpellEffects.WIND)
                .put(EffectType.RESURRECTION, SpellEffects.RESURRECTION)
                .put(EffectType.SLOWNESS, SpellEffects.SLOWNESS)
                .put(EffectType.TELEPORT, SpellEffects.TELEPORT)
                .put(EffectType.WATER, SpellEffects.WATER)
                .put(EffectType.WATER_SOURCE, SpellEffects.WATER_SOURCE)
                .build()
        );
        private static final Map<ShapeType, SpellShape> shapes = Maps.immutableEnumMap(Map.of(
                ShapeType.BALL, SpellShapes.BALL,
                ShapeType.BEAM, SpellShapes.BEAM,
                ShapeType.CONE, SpellShapes.CONE,
                ShapeType.WALL, SpellShapes.WALL,
                ShapeType.SELF, SpellShapes.SELF,
                ShapeType.SINGLE, SpellShapes.SINGLE,
                ShapeType.SPHERE, SpellShapes.SPHERE
        ));
        private static final EnumMap<SpellState, List<Transition>> transitions = Maps.newEnumMap(SpellState.class);

        static
        {
            from(SpellState.START,
                    always(makeTransition(SpellBuilder::addInitial, SpellState.PRIMARY))
            );
            from(SpellState.PRIMARY,
                    when(SpellBuilder::isRepeat, makeTransition(SpellBuilder::increasePrimary, SpellState.PRIMARY)),
                    when(SpellBuilder::canMix, makeTransition(SpellBuilder::increasePrimary, SpellState.SECONDARY)),
                    always(makeTransition(SpellBuilder::increasePrimary, SpellState.AUGMENT))
            );
            from(SpellState.SECONDARY,
                    when(SpellBuilder::canMix, makeTransition(SpellBuilder::applySecondary, SpellState.SECONDARY)),
                    always(makeTransition(SpellBuilder::applySecondary, SpellState.AUGMENT))
            );
            from(SpellState.AUGMENT,
                    always(makeTransition(SpellBuilder::applyAugment, SpellState.AUGMENT))
            );
        }

        private static void from(SpellState state, Transition... edges)
        {
            transitions.put(state, List.of(edges));
        }

        private static Transition when(BiPredicate<SpellBuilder, Element> condition, TransitionFunction transition)
        {
            return new Transition(condition, transition);
        }

        private static Transition always(TransitionFunction transition)
        {
            return new Transition((b, e) -> true, transition);
        }

        public record Transition(BiPredicate<SpellBuilder, Element> condition, TransitionFunction transition)
        {
            public boolean test(SpellBuilder builder, Element element)
            {
                return condition.test(builder, element);
            }

            public boolean apply(SpellBuilder builder, Element element)
            {
                return transition.apply(builder, element);
            }
        }

        @FunctionalInterface
        public interface TransitionFunction
        {
            boolean apply(SpellBuilder builder, Element element);
        }

        private static TransitionFunction makeTransition(TransitionFunction action, SpellState next)
        {
            return (b, e) -> {
                if (!action.apply(b, e))
                    return false;

                b.transition(e, next);
                return true;
            };
        }

        private static TransitionFunction makeTransition(BiConsumer<SpellBuilder, Element> action, SpellState next)
        {
            return (b, e) -> {
                action.accept(b, e);

                b.transition(e, next);
                return true;
            };
        }

        private static TransitionFunction makeTransition(Consumer<SpellBuilder> action, SpellState next)
        {
            return (b, e) -> {
                action.accept(b);

                b.transition(e, next);
                return true;
            };
        }

        private static TransitionFunction makeTransition(SpellState next)
        {
            return (b, e) -> {
                b.transition(e, next);
                return true;
            };
        }

        private static TransitionFunction makeOppositeTransition(Consumer<SpellBuilder> action, SpellState next)
        {
            return (b, e) -> {
                action.accept(b);

                b.transition(e, next);
                return true;
            };
        }

        private static TransitionFunction makeOppositeTransition(SpellState next)
        {
            return (b, e) -> {
                b.transition(e, next);
                return true;
            };
        }

        private static TransitionFunction logUnimplementedState()
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
            SECONDARY,  // Direct modifiers which mutate the spell effect
            AUGMENT,    // Augmentations: modifiers that change how the spell behaves
            END,        // Final state, the spell is complete and the last seen element dictates the shape.
            INVALID
        }

        private enum ElementUsageType
        {
            INITIAL,
            EMPOWER,
            MIX,
            MODIFY
        }
    }
}

package dev.gigaherz.elementsofpower.integration.aequivaleo;
/*
import com.google.common.collect.ImmutableMap;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.instanced.IInstancedEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.results.IEquivalencyResults;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static dev.gigaherz.elementsofpower.ElementsOfPowerMod.location;

@com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin
public class AequivaleoPlugin implements IAequivaleoPlugin
{
    public static final String ID = location("magic_essences").toString();

    private static final ResourceLocation TYPES_REGISTRY = new ResourceLocation("aequivaleo", "compound_type");
    private static final ResourceLocation TYPE_GROUPS_REGISTRY = new ResourceLocation("aequivaleo", "compound_type_group");

    public static final DeferredRegister<ICompoundType> TYPES = DeferredRegister.create(TYPES_REGISTRY, ElementsOfPowerMod.MODID);
    public static final DeferredRegister<ICompoundTypeGroup> TYPE_GROUPS = DeferredRegister.create(TYPE_GROUPS_REGISTRY, ElementsOfPowerMod.MODID);

    public static final DeferredHolder<ICompoundTypeGroup, EssenceGroupType> ESSENCE = TYPE_GROUPS.register("essence", () -> new EssenceGroupType(location("essence")));
    public static final DeferredHolder<ICompoundType, EssenceType> FIRE = TYPES.register("fire", () -> new EssenceType(Element.FIRE, ESSENCE));
    public static final DeferredHolder<ICompoundType, EssenceType> WATER = TYPES.register("water", () -> new EssenceType(Element.WATER, ESSENCE));
    public static final DeferredHolder<ICompoundType, EssenceType> AIR = TYPES.register("air", () -> new EssenceType(Element.AIR, ESSENCE));
    public static final DeferredHolder<ICompoundType, EssenceType> EARTH = TYPES.register("earth", () -> new EssenceType(Element.EARTH, ESSENCE));
    public static final DeferredHolder<ICompoundType, EssenceType> LIGHT = TYPES.register("light", () -> new EssenceType(Element.LIGHT, ESSENCE));
    public static final DeferredHolder<ICompoundType, EssenceType> TIME = TYPES.register("time", () -> new EssenceType(Element.TIME, ESSENCE));
    public static final DeferredHolder<ICompoundType, EssenceType> LIFE = TYPES.register("life", () -> new EssenceType(Element.LIFE, ESSENCE));
    public static final DeferredHolder<ICompoundType, EssenceType> CHAOS = TYPES.register("chaos", () -> new EssenceType(Element.CHAOS, ESSENCE));

    public static final Map<Element, Holder<ICompoundType>> BY_ELEMENT = ImmutableMap.<Element, Holder<ICompoundType>>builder()
            .put(Element.FIRE, FIRE)
            .put(Element.WATER, WATER)
            .put(Element.AIR, AIR)
            .put(Element.EARTH, EARTH)
            .put(Element.LIGHT, LIGHT)
            .put(Element.TIME, TIME)
            .put(Element.LIFE, LIFE)
            .put(Element.CHAOS, CHAOS).build();

    public static IEquivalencyResults get(@NotNull Level world)
    {
        return IAequivaleoAPI.getInstance().getEquivalencyResults(world.dimension());
    }

    public static Optional<MagicAmounts> getEssences(@Nullable Level world, ItemStack stack, boolean wholeStack)
    {
        if (world == null) return Optional.empty();
        return getEssences(get(world), stack, wholeStack);
    }

    public static Optional<MagicAmounts> getEssences(@NotNull IEquivalencyResults cache, ItemStack stack, boolean wholeStack)
    {
        Optional<MagicAmounts> am = cache.mappedDataFor(ESSENCE.get(), stack);
        if (wholeStack) am = am.map(amounts -> amounts.multiply(stack.getCount()));
        return am.filter(MagicAmounts::isPositive);
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public void onConstruction()
    {
        ModList.get().getModContainerById(ElementsOfPowerMod.MODID).ifPresent(mod -> {
            TYPES.register(((FMLModContainer) mod).getEventBus());
            TYPE_GROUPS.register(((FMLModContainer) mod).getEventBus());
        });
    }

    @Override
    public void onCommonSetup()
    {
        IInstancedEquivalencyHandlerRegistry equivalencyHandlerRegistry = IInstancedEquivalencyHandlerRegistry.getInstance();
        for (Gemstone g : Gemstone.values)
        {
            GemstoneItem gemstoneItem = g.getItem();
            equivalencyHandlerRegistry.registerHandler(gemstoneItem, (equivalences) -> {
                if (g.isVanilla())
                {
                    equivalences.accept(g.getVanillaItem());
                }
                for (Quality q : Quality.values)
                {
                    equivalences.accept(gemstoneItem.setQuality(new ItemStack(gemstoneItem), q));
                }
            });
        }
    }

    public static Optional<MagicAmounts> getMagicAmounts(Set<CompoundInstance> results)
    {
        if (results.isEmpty())
            return Optional.empty();

        MagicAmounts.Accumulator b = MagicAmounts.builder();
        for (CompoundInstance i : results)
        {
            ICompoundType type = i.getType();
            if (type instanceof EssenceType)
            {
                b.add(MagicAmounts.ofElement(((EssenceType) type).getElement(), (float) (double) i.getAmount()));
            }
        }
        return Optional.of(b.toAmounts());
    }
}
*/
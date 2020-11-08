package gigaherz.elementsofpower.integration.aequivaleo;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.aequivaleo.api.IAequivaleoAPI;
import com.ldtteam.aequivaleo.api.compound.CompoundInstance;
import com.ldtteam.aequivaleo.api.compound.type.ICompoundType;
import com.ldtteam.aequivaleo.api.compound.type.group.ICompoundTypeGroup;
import com.ldtteam.aequivaleo.api.instanced.IInstancedEquivalencyHandlerRegistry;
import com.ldtteam.aequivaleo.api.plugin.IAequivaleoPlugin;
import com.ldtteam.aequivaleo.api.results.IResultsInformationCache;
import gigaherz.elementsofpower.ConfigManager;
import gigaherz.elementsofpower.ElementsOfPowerItems;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.ConversionCache;
import gigaherz.elementsofpower.database.IConversionCache;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.gemstones.Quality;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@com.ldtteam.aequivaleo.api.plugin.AequivaleoPlugin
public class AequivaleoPlugin implements IAequivaleoPlugin
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String ID = ElementsOfPowerMod.location("magic_essences").toString();

    public static final DeferredRegister<ICompoundType> TYPES = DeferredRegister.create(ICompoundType.class, ElementsOfPowerMod.MODID);
    public static final DeferredRegister<ICompoundTypeGroup> TYPE_GROUPS = DeferredRegister.create(ICompoundTypeGroup.class, ElementsOfPowerMod.MODID);

    public static final RegistryObject<EssenceGroupType> ESSENCE = TYPE_GROUPS.register("essence", EssenceGroupType::new);
    public static final RegistryObject<EssenceType> FIRE = TYPES.register("fire", () -> new EssenceType(Element.FIRE, ESSENCE));
    public static final RegistryObject<EssenceType> WATER = TYPES.register("water", () -> new EssenceType(Element.WATER, ESSENCE));
    public static final RegistryObject<EssenceType> AIR = TYPES.register("air", () -> new EssenceType(Element.AIR, ESSENCE));
    public static final RegistryObject<EssenceType> EARTH = TYPES.register("earth", () -> new EssenceType(Element.EARTH, ESSENCE));
    public static final RegistryObject<EssenceType> LIGHT = TYPES.register("light", () -> new EssenceType(Element.LIGHT, ESSENCE));
    public static final RegistryObject<EssenceType> DARKNESS = TYPES.register("darkness", () -> new EssenceType(Element.DARKNESS, ESSENCE));
    public static final RegistryObject<EssenceType> LIFE = TYPES.register("life", () -> new EssenceType(Element.LIFE, ESSENCE));
    public static final RegistryObject<EssenceType> DEATH = TYPES.register("death", () -> new EssenceType(Element.DEATH, ESSENCE));

    public static final Map<Element, RegistryObject<EssenceType>> BY_ELEMENT = ImmutableMap.<Element, RegistryObject<EssenceType>>builder()
         .put(Element.FIRE, FIRE)
         .put(Element.WATER, WATER)
         .put(Element.AIR, AIR)
         .put(Element.EARTH, EARTH)
         .put(Element.LIGHT, LIGHT)
         .put(Element.DARKNESS, DARKNESS)
         .put(Element.LIFE, LIFE)
         .put(Element.DEATH, DEATH).build();

    public static final ConversionCache CLIENT = new ConversionCache();
    public static final ConversionCache SERVER = new ConversionCache();

    public static ConversionCache get(@Nullable World world)
    {
        return (world != null && world.isRemote) ? CLIENT : SERVER;
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

        if (!ConfigManager.COMMON.disableAequivaleoSupport.get())
        {
            LOGGER.info("Aequivaleo has been enabled. Aequivaleo-calculated magic conversions will be used instead of the internal system...");
            ConversionCache.aequivaleoGetter = EssenceConversionCache::new;
        }
    }

    @Override
    public void onCommonSetup()
    {
        IInstancedEquivalencyHandlerRegistry equivalencyHandlerRegistry = IInstancedEquivalencyHandlerRegistry.getInstance();
        for(Gemstone g : Gemstone.values)
        {
            GemstoneItem gemstoneItem = g.getItem();
            equivalencyHandlerRegistry.registerHandler(gemstoneItem, (equivalences) ->{
                if (g.isVanilla())
                {
                    equivalences.accept(g.getVanillaItem());
                }
                for(Quality q : Quality.values)
                {
                    equivalences.accept(gemstoneItem.setQuality(new ItemStack(gemstoneItem), q));
                }
            }) ;
        }
    }

    @Override
    public void onReloadStartedFor(ServerWorld world)
    {
        SERVER.clear();
    }


    @Override
    public void onDataSynced(RegistryKey<World> worldRegistryKey)
    {
        CLIENT.clear();
    }

    private static class EssenceConversionCache implements IConversionCache
    {
        private final ConversionCache conversions;
        private final RegistryKey<World> worldKey;

        private EssenceConversionCache(World world)
        {
            this.worldKey = world.getDimensionKey();
            this.conversions = get(world);
        }

        @Override
        public boolean hasEssences(ItemStack stack)
        {
            return !getEssences(stack, false).isEmpty();
        }

        @Override
        public MagicAmounts getEssences(ItemStack stack, boolean wholeStack)
        {
            MagicAmounts amounts = conversions.computeIfAbsent(stack.getItem(), item -> {
                IResultsInformationCache aequivaleoCache = IAequivaleoAPI.getInstance().getResultsInformationCache(worldKey);
                Optional<MagicAmounts> am = aequivaleoCache.getCacheFor(ESSENCE.get(), item);
                return am.orElse(MagicAmounts.EMPTY);
            });
            return wholeStack ? amounts.multiply(stack.getCount()) : amounts;
        }
    }

    public static Optional<MagicAmounts> getMagicAmounts(Set<CompoundInstance> results)
    {
        if (results.isEmpty())
            return Optional.empty();

        MagicAmounts.Accumulator b = MagicAmounts.builder();
        for(CompoundInstance i : results)
        {
            ICompoundType type = i.getType();
            if (type instanceof EssenceType)
            {
                b.add(MagicAmounts.ofElement(((EssenceType)type).getElement(), (float)(double)i.getAmount()));
            }
        }
        return Optional.of(b.toAmounts());
    }
}

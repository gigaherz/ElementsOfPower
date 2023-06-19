package dev.gigaherz.elementsofpower;

import com.mojang.serialization.Codec;
import dev.gigaherz.elementsofpower.analyzer.AnalyzerItem;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerMenu;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerScreen;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.client.NbtToModel;
import dev.gigaherz.elementsofpower.client.WandUseManager;
import dev.gigaherz.elementsofpower.client.renderers.BallEntityRenderer;
import dev.gigaherz.elementsofpower.client.renderers.EssentializerTileEntityRender;
import dev.gigaherz.elementsofpower.cocoons.*;
import dev.gigaherz.elementsofpower.entities.BallEntity;
import dev.gigaherz.elementsofpower.essentializer.ColoredSmokeData;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerMenu;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerScreen;
import dev.gigaherz.elementsofpower.gemstones.AnalyzedFilteringIngredient;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.items.BaubleItem;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.items.StaffItem;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.network.*;
import dev.gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import dev.gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

@Mod(ElementsOfPowerMod.MODID)
public class ElementsOfPowerMod
{
    public static final String MODID = "elementsofpower";

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }

    private static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);

    public static final RegistryObject<EntityType<BallEntity>> SPELL_BALL_ENTITY_TYPE = ENTITY_TYPES.register("spell_ball", () ->
            EntityType.Builder.<BallEntity>of(BallEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).build(location("spell_ball").toString()));

    public static final RegistryObject<BlockEntityType<EssentializerBlockEntity>> ESSENTIALIZER_BLOCK_ENTITY = BLOCK_ENTITIES.register("essentializer", () ->
            BlockEntityType.Builder.of(EssentializerBlockEntity::new, ElementsOfPowerBlocks.ESSENTIALIZER.get()).build(null)
    );
    public static final RegistryObject<BlockEntityType<CocoonTileEntity>> COCOON_BLOCL_ENTITY = BLOCK_ENTITIES.register("cocoon", () ->
            BlockEntityType.Builder.of(CocoonTileEntity::new,
                    Element.stream_without_balance().map(Element::getCocoon).filter(Objects::nonNull).toArray(Block[]::new)
            ).build(null)
    );

    public static final RegistryObject<MenuType<EssentializerMenu>> ESSENTIALIZER_MENU = MENU_TYPES.register("essentializer", () ->
            new MenuType<>(EssentializerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
    public static final RegistryObject<MenuType<AnalyzerMenu>> ANALYZER_MENU = MENU_TYPES.register("analyzer", () ->
            IForgeMenuType.create(AnalyzerMenu::new)
    );

    public static final RegistryObject<SimpleCraftingRecipeSerializer<ContainerChargeRecipe>> CONTAINER_CHARGE = RECIPE_SERIALIZERS.register("container_charge", () ->
            new SimpleCraftingRecipeSerializer<>(ContainerChargeRecipe::new)
    );
    public static final RegistryObject<SimpleCraftingRecipeSerializer<GemstoneChangeRecipe>> GEMSTONE_CHANGE = RECIPE_SERIALIZERS.register("gemstone_change", () ->
            new SimpleCraftingRecipeSerializer<>(GemstoneChangeRecipe::new)
    );

    public static final RegistryObject<CocoonFeature> COCOON_FEATURE = FEATURES.register("cocoon", () ->
            new CocoonFeature(NoneFeatureConfiguration.CODEC)
    );

    public static final RegistryObject<ParticleType<ColoredSmokeData>> COLORED_SMOKE_DATA = PARTICLE_TYPES.register("colored_smoke", () ->
            new ColoredSmokeData.Type(false)
    );

    public static RegistryObject<LootItemFunctionType> APPLY_ORB_SIZE = LOOT_FUNCTION_TYPES.register("apply_orb_size", () -> new LootItemFunctionType(new ApplyOrbSizeFunction.Serializer()));

    public static RegistryObject<Codec<CocoonModifier>> COCOON_MODIFIER_SERIALIZER = BIOME_MODIFIER_SERIALIZERS.register("cocoons", () -> Codec.unit(CocoonModifier::new));
    public static RegistryObject<Codec<GemstoneOreModifier>> GEMSTONE_ORE_MODIFIER_SERIALIZER = BIOME_MODIFIER_SERIALIZERS.register("gemstone_ores", () -> Codec.unit(GemstoneOreModifier::new));


    public static final RegistryObject<PlacementModifierType<CocoonPlacement>> COCOON_PLACEMENT = PLACEMENT_MODIFIER_TYPES.register("cocoon_placement", () -> CocoonPlacement::codec);

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB_MAGIC = CREATIVE_TABS.register("magic", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP,0)
            .icon(() -> ElementsOfPowerItems.WAND.get().getStack(Gemstone.DIAMOND, Quality.COMMON))
            .title(Component.translatable("tab.elementsofpower.magic"))
            .displayItems((featureFlags, output) -> {
                output.accept(ElementsOfPowerItems.ESSENTIALIZER.get());

                output.accept(ElementsOfPowerItems.ANALYZER.get());
                output.accept(ElementsOfPowerItems.WAND.get());
                output.accept(ElementsOfPowerItems.STAFF.get());
                output.accept(ElementsOfPowerItems.RING.get());
                output.accept(ElementsOfPowerItems.HEADBAND.get());
                output.accept(ElementsOfPowerItems.NECKLACE.get());

                output.accept(ElementsOfPowerItems.FIRE_ORB.get());
                output.accept(ElementsOfPowerItems.WATER_ORB.get());
                output.accept(ElementsOfPowerItems.AIR_ORB.get());
                output.accept(ElementsOfPowerItems.EARTH_ORB.get());
                output.accept(ElementsOfPowerItems.LIGHT_ORB.get());
                output.accept(ElementsOfPowerItems.TIME_ORB.get());
                output.accept(ElementsOfPowerItems.LIFE_ORB.get());
                output.accept(ElementsOfPowerItems.CHAOS_ORB.get());

                output.accept(ElementsOfPowerItems.FIRE_COCOON.get());
                output.accept(ElementsOfPowerItems.WATER_COCOON.get());
                output.accept(ElementsOfPowerItems.AIR_COCOON.get());
                output.accept(ElementsOfPowerItems.EARTH_COCOON.get());
                output.accept(ElementsOfPowerItems.LIGHT_COCOON.get());
                output.accept(ElementsOfPowerItems.TIME_COCOON.get());
                output.accept(ElementsOfPowerItems.LIFE_COCOON.get());
                output.accept(ElementsOfPowerItems.CHAOS_COCOON.get());


            }).build()
    );

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB_GEMSTONES = CREATIVE_TABS.register("gemstones", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP,0)
            .icon(() -> new ItemStack(Gemstone.RUBY))
            .title(Component.translatable("tab.elementsofpower.gemstones"))
            .displayItems((featureFlags, output) -> {


                output.accept(ElementsOfPowerItems.RUBY_ORE.get());
                output.accept(ElementsOfPowerItems.SAPPHIRE_ORE.get());
                output.accept(ElementsOfPowerItems.CITRINE_ORE.get());
                output.accept(ElementsOfPowerItems.AGATE_ORE.get());
                output.accept(ElementsOfPowerItems.SERENDIBITE_ORE.get());
                output.accept(ElementsOfPowerItems.ELBAITE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_RUBY_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_SAPPHIRE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_CITRINE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_AGATE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_SERENDIBITE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_ELBAITE_ORE.get());
                output.accept(ElementsOfPowerItems.RUBY_BLOCK.get());
                output.accept(ElementsOfPowerItems.SAPPHIRE_BLOCK.get());
                output.accept(ElementsOfPowerItems.CITRINE_BLOCK.get());
                output.accept(ElementsOfPowerItems.AGATE_BLOCK.get());
                output.accept(ElementsOfPowerItems.SERENDIBITE_BLOCK.get());
                output.accept(ElementsOfPowerItems.ELBAITE_BLOCK.get());

                ElementsOfPowerItems.RUBY.get().addToTab(output::accept);
                ElementsOfPowerItems.SAPPHIRE.get().addToTab(output::accept);
                ElementsOfPowerItems.CITRINE.get().addToTab(output::accept);
                ElementsOfPowerItems.AGATE.get().addToTab(output::accept);
                ElementsOfPowerItems.QUARTZ.get().addToTab(output::accept);
                ElementsOfPowerItems.SERENDIBITE.get().addToTab(output::accept);
                ElementsOfPowerItems.EMERALD.get().addToTab(output::accept);
                ElementsOfPowerItems.ELBAITE.get().addToTab(output::accept);
                ElementsOfPowerItems.DIAMOND.get().addToTab(output::accept);
                ElementsOfPowerItems.CREATIVITE.get().addToTab(output::accept);

            }).build()
    );

    //private static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> CONFIGURED_COCOON_FEATURE;
    //private static Holder<PlacedFeature> PLACED_COCOON_FEATURE;


    public record CocoonModifier() implements BiomeModifier
    {
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder)
        {
            if (phase == Phase.ADD)
            {
                if (biome.is(Tags.Biomes.IS_VOID))
                    return;

                //builder.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ElementsOfPowerMod.PLACED_COCOON_FEATURE);
            }
        }

        public Codec<? extends BiomeModifier> codec()
        {
            return COCOON_MODIFIER_SERIALIZER.get();
        }
    }

    public record GemstoneOreModifier() implements BiomeModifier
    {
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder)
        {
            if (phase == Phase.ADD)
            {
                if (biome.is(Tags.Biomes.IS_VOID))
                    return;

                boolean isEndBiome = biome.is(BiomeTags.IS_END);
                boolean isNetherBiome = biome.is(BiomeTags.IS_NETHER);
                if (!isEndBiome && !isNetherBiome)
                {
                    /* TODO: holders
                    var values = getBiomeValues(biome);
                    for (var feat : oreFeatures.get().getOrDefault(values, List.of()))
                    {
                        builder.getGenerationSettings().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feat);
                    }
                     */
                }
            }
        }

        public Codec<? extends BiomeModifier> codec()
        {
            return GEMSTONE_ORE_MODIFIER_SERIALIZER.get();
        }
    }

    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(location("general"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static Logger LOGGER = LogManager.getLogger(MODID);

    public ElementsOfPowerMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ElementsOfPowerBlocks.BLOCKS.register(modEventBus);
        ElementsOfPowerItems.ITEMS.register(modEventBus);
        LOOT_FUNCTION_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        FEATURES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        PLACEMENT_MODIFIER_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerParticleFactory);
        modEventBus.addListener(this::imcEnqueue);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::modelRegistry);
        modEventBus.addListener(this::registerCapabilities);

        //MinecraftForge.EVENT_BUS.addListener(this::addStuffToBiomes);
    }

    public void registerParticleFactory(RegisterParticleProvidersEvent event)
    {
        event.registerSpriteSet(COLORED_SMOKE_DATA.get(), ColoredSmokeData.Factory::new);
    }

    public void modelRegistry(ModelEvent.RegisterGeometryLoaders event)
    {
        event.register("nbt_to_model", NbtToModel.Loader.INSTANCE);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents
    {
        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(SPELL_BALL_ENTITY_TYPE.get(), BallEntityRenderer::new);

            event.registerBlockEntityRenderer(ESSENTIALIZER_BLOCK_ENTITY.get(), EssentializerTileEntityRender::new);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                MenuScreens.register(ANALYZER_MENU.get(), AnalyzerScreen::new);
                MenuScreens.register(ESSENTIALIZER_MENU.get(), EssentializerScreen::new);
            });

            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.DUST.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.MIST.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.CUSHION.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.LIGHT.get(), RenderType.translucent());

            WandUseManager.initialize();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {

            /* TODO: datagen
            CONFIGURED_COCOON_FEATURE = FeatureUtils.register("elementsofpower:overworld_cocoon", ElementsOfPowerMod.COCOON_FEATURE.get(), NoneFeatureConfiguration.INSTANCE);

            PLACED_COCOON_FEATURE = PlacementUtils.register("elementsofpower:overworld_cocoon", CONFIGURED_COCOON_FEATURE, CocoonPlacement.INSTANCE);

            oreFeatures.get();
            */
        });

        int messageNumber = 0;
        CHANNEL.messageBuilder(UpdateSpellSequence.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(UpdateSpellSequence::encode).decoder(UpdateSpellSequence::new).consumerNetworkThread(UpdateSpellSequence::handle).add();
        CHANNEL.messageBuilder(SynchronizeSpellcastState.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SynchronizeSpellcastState::encode).decoder(SynchronizeSpellcastState::new).consumerNetworkThread(SynchronizeSpellcastState::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerAmounts.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerAmounts::encode).decoder(UpdateEssentializerAmounts::new).consumerNetworkThread(UpdateEssentializerAmounts::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerTile.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerTile::encode).decoder(UpdateEssentializerTile::new).consumerNetworkThread(UpdateEssentializerTile::handle).add();
        CHANNEL.messageBuilder(AddVelocityToPlayer.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(AddVelocityToPlayer::encode).decoder(AddVelocityToPlayer::new).consumerNetworkThread(AddVelocityToPlayer::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);

        SpellcastEntityData.register();
        //DiscoveryHandler.init();

        CraftingHelper.register(AnalyzedFilteringIngredient.ID, AnalyzedFilteringIngredient.Serializer.INSTANCE);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        MagicContainerCapability.register(event);
        PlayerCombinedMagicContainers.register(event);
        CocoonEventHandling.enable(event);
    }

    private void imcEnqueue(InterModEnqueueEvent event)
    {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("headband").icon(location("gui/headband_slot_background")).size(1).build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("necklace").icon(location("gui/necklace_slot_background")).size(1).build());
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("ring").size(2).build());
    }

    public void gatherData(GatherDataEvent event)
    {
        ElementsofPowerDataGen.gatherData(event);
    }

    private static BiomeValues getBiomeValues(Holder<Biome> biome)
    {
        var heat = BiomeValue.NEUTRAL;
        var humidity = BiomeValue.NEUTRAL;
        var life = BiomeValue.NEUTRAL;

        if (biome.is(Tags.Biomes.IS_HOT))
            heat = BiomeValue.FOR;
        if (biome.is(Tags.Biomes.IS_COLD))
            heat = BiomeValue.AGAINST;
        if (biome.is(Tags.Biomes.IS_WET))
            humidity = BiomeValue.FOR;
        if (biome.is(Tags.Biomes.IS_DRY))
            humidity = BiomeValue.AGAINST;
        if (biome.is(Tags.Biomes.IS_DENSE))
            life = BiomeValue.FOR;
        if (biome.is(Tags.Biomes.IS_SPARSE))
            life = BiomeValue.AGAINST;

        return new BiomeValues(heat, humidity, life);
    }

    public enum BiomeValue implements StringRepresentable
    {
        NEUTRAL("neutral", 1),
        FOR("for", 2),
        AGAINST("against", 0);

        private final String name;
        private final int value;

        BiomeValue(String name, int value)
        {
            this.name = name;
            this.value = value;
        }

        public int value()
        {
            return value;
        }

        @Override
        public String getSerializedName()
        {
            return name;
        }
    }

    public static record BiomeValues(BiomeValue heat, BiomeValue humidity, BiomeValue life)
    {
        private int getBiomeBonus(@Nullable Element e)
        {
            if (e == null)
                return 1;
            return switch (e)
            {
                case FIRE -> heat.value();
                case WATER -> humidity.value();
                case LIFE -> life.value();
                case CHAOS -> 2 - life.value();
                default -> 1;
            };
        }
    }

    /* TODO: datagen
    private static Supplier<Map<BiomeValues, List<Holder<PlacedFeature>>>> oreFeatures = Lazy.of(() -> Util.make(new HashMap<>(), map -> {
        for(var heat : BiomeValue.values())
        {
            for(var humidity : BiomeValue.values())
            {
                for(var life : BiomeValue.values())
                {
                    var name = heat.getSerializedName() + "_" + humidity.getSerializedName() + "_" + life.getSerializedName();
                    var values = new BiomeValues(heat, humidity, life);
                    var list = new ArrayList<Holder<PlacedFeature>>();
                    for (Gemstone g : Gemstone.values)
                    {
                        if (g.generateInWorld())
                        {
                            var ores = g.getOres();
                            var stone_ore = ores.get(0);
                            var deepslate_ore = ores.size() >= 2 ? ores.get(1) : null;

                            List<OreConfiguration.TargetBlockState> targets = new ArrayList<>();

                            targets.add(OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, stone_ore.defaultBlockState()));

                            if (deepslate_ore != null)
                                targets.add(OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, deepslate_ore.defaultBlockState()));

                            int numPerVein = 3 + values.getBiomeBonus(g.getElement());
                            var name2 = g.getSerializedName() + "_ore_" + name;
                            var configured = FeatureUtils.register(name2, Feature.ORE, new OreConfiguration(targets, numPerVein, 0.0f));
                            var placed = PlacementUtils.register(name2, configured,
                                    CountPlacement.of(16),
                                    InSquarePlacement.spread(),
                                    HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)),
                                    BiomeFilter.biome());
                            list.add(placed);

                        }
                    }
                    map.put(values, list);
                }
            }
        }
    }));

     */
}


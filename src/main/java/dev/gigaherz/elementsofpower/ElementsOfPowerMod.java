package dev.gigaherz.elementsofpower;

import com.mojang.datafixers.util.Either;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerMenu;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerScreen;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.client.NbtToModel;
import dev.gigaherz.elementsofpower.client.StaffModel;
import dev.gigaherz.elementsofpower.client.WandUseManager;
import dev.gigaherz.elementsofpower.client.models.PillarModel;
import dev.gigaherz.elementsofpower.client.renderers.entities.BallEntityRenderer;
import dev.gigaherz.elementsofpower.client.renderers.EssentializerTileEntityRender;
import dev.gigaherz.elementsofpower.client.renderers.entities.PillarEntityRenderer;
import dev.gigaherz.elementsofpower.cocoons.*;
import dev.gigaherz.elementsofpower.entities.BallEntity;
import dev.gigaherz.elementsofpower.entities.PillarEntity;
import dev.gigaherz.elementsofpower.essentializer.ColoredSmokeData;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerMenu;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerScreen;
import dev.gigaherz.elementsofpower.gemstones.AnalyzedFilteringIngredient;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.network.*;
import dev.gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import dev.gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

@Mod(ElementsOfPowerMod.MODID)
public class ElementsOfPowerMod
{
    public static final String MODID = "elementsofpower";

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }

    private static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BallEntity>> BALL_ENTITY_TYPE = ENTITY_TYPES.register("spell_ball", () ->
            EntityType.Builder.<BallEntity>of(BallEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true)
                    .build(location("spell_ball").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<PillarEntity>> PILLAR_ENTITY_TYPE = ENTITY_TYPES.register("spell_pillar", () ->
            EntityType.Builder.<PillarEntity>of(PillarEntity::new, MobCategory.MISC)
                    .sized(14/16.0f, 31/16.0f).fireImmune()
                    .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true)
                    .setCustomClientFactory((packet,level) -> new PillarEntity(ElementsOfPowerMod.PILLAR_ENTITY_TYPE.get(), level))
                            .build(location("spell_pillar").toString()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssentializerBlockEntity>> ESSENTIALIZER_BLOCK_ENTITY = BLOCK_ENTITIES.register("essentializer", () ->
            BlockEntityType.Builder.of(EssentializerBlockEntity::new, ElementsOfPowerBlocks.ESSENTIALIZER.get()).build(null)
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CocoonTileEntity>> COCOON_BLOCL_ENTITY = BLOCK_ENTITIES.register("cocoon", () ->
            BlockEntityType.Builder.of(CocoonTileEntity::new,
                    Element.stream_without_balance().map(Element::getCocoon).filter(Objects::nonNull).toArray(Block[]::new)
            ).build(null)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<EssentializerMenu>> ESSENTIALIZER_MENU = MENU_TYPES.register("essentializer", () ->
            new MenuType<>(EssentializerMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );
    public static final DeferredHolder<MenuType<?>, MenuType<AnalyzerMenu>> ANALYZER_MENU = MENU_TYPES.register("analyzer", () ->
            IMenuTypeExtension.create(AnalyzerMenu::new)
    );

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ContainerChargeRecipe>> CONTAINER_CHARGE = RECIPE_SERIALIZERS.register("container_charge", () ->
            new SimpleCraftingRecipeSerializer<>(ContainerChargeRecipe::new)
    );
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<GemstoneChangeRecipe>> GEMSTONE_CHANGE = RECIPE_SERIALIZERS.register("gemstone_change", () ->
            new SimpleCraftingRecipeSerializer<>(GemstoneChangeRecipe::new)
    );

    public static final DeferredHolder<Feature<?>, CocoonFeature> COCOON_FEATURE = FEATURES.register("cocoon", () ->
            new CocoonFeature(NoneFeatureConfiguration.CODEC)
    );

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColoredSmokeData>> COLORED_SMOKE_DATA = PARTICLE_TYPES.register("colored_smoke", () ->
            new ColoredSmokeData.Type(false)
    );

    public static DeferredHolder<LootItemFunctionType, LootItemFunctionType> APPLY_ORB_SIZE = LOOT_FUNCTION_TYPES.register("apply_orb_size", () -> new LootItemFunctionType(ApplyOrbSizeFunction.CODEC));

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<CocoonPlacement>> COCOON_PLACEMENT = PLACEMENT_MODIFIER_TYPES.register("cocoon_placement", () -> CocoonPlacement::codec);

    public static final DeferredHolder<IngredientType<?>, IngredientType<AnalyzedFilteringIngredient>> ANALYZED_FILTERING_INGREDIENT
            = INGREDIENT_TYPES.register("analyzed_filtering_ingredient", () -> new IngredientType<>(AnalyzedFilteringIngredient.CODEC, AnalyzedFilteringIngredient.NON_EMPTY_CODEC));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB_MAGIC = CREATIVE_TABS.register("magic", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP,0)
            .icon(() -> ElementsOfPowerItems.WAND.get().getStack(Gemstone.DIAMOND, Quality.COMMON))
            .title(Component.translatable("tab.elementsofpower.magic"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                output.accept(ElementsOfPowerItems.ESSENTIALIZER.get());

                output.accept(ElementsOfPowerItems.ANALYZER.get());
                output.accept(ElementsOfPowerItems.WAND.get());
                output.accept(ElementsOfPowerItems.STAFF.get());
                output.accept(ElementsOfPowerItems.RING.get());
                output.accept(ElementsOfPowerItems.BRACELET.get());
                output.accept(ElementsOfPowerItems.NECKLACE.get());
                output.accept(ElementsOfPowerItems.GEM_POUCH.get());

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

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB_GEMSTONES = CREATIVE_TABS.register("gemstones", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP,0)
            .icon(() -> new ItemStack(Gemstone.RUBY))
            .title(Component.translatable("tab.elementsofpower.gemstones"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS, CREATIVE_TAB_MAGIC.getKey())
            .displayItems((featureFlags, output) -> {

                output.accept(ElementsOfPowerItems.RUBY_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_RUBY_ORE.get());
                output.accept(ElementsOfPowerItems.RUBY_BLOCK.get());
                ElementsOfPowerItems.RUBY.get().creativeTabStacks(output::accept);


                output.accept(ElementsOfPowerItems.SAPPHIRE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_SAPPHIRE_ORE.get());
                output.accept(ElementsOfPowerItems.SAPPHIRE_BLOCK.get());
                ElementsOfPowerItems.SAPPHIRE.get().creativeTabStacks(output::accept);

                output.accept(ElementsOfPowerItems.CITRINE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_CITRINE_ORE.get());
                output.accept(ElementsOfPowerItems.CITRINE_BLOCK.get());
                ElementsOfPowerItems.CITRINE.get().creativeTabStacks(output::accept);

                output.accept(ElementsOfPowerItems.AGATE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_AGATE_ORE.get());
                output.accept(ElementsOfPowerItems.AGATE_BLOCK.get());
                ElementsOfPowerItems.AGATE.get().creativeTabStacks(output::accept);

                output.accept(ElementsOfPowerItems.ONYX_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_ONYX_ORE.get());
                output.accept(ElementsOfPowerItems.ONYX_BLOCK.get());
                ElementsOfPowerItems.ONYX.get().creativeTabStacks(output::accept);

                output.accept(ElementsOfPowerItems.rubellite_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_rubellite_ORE.get());
                output.accept(ElementsOfPowerItems.rubellite_BLOCK.get());
                ElementsOfPowerItems.RUBELLITE.get().creativeTabStacks(output::accept);

                ElementsOfPowerItems.QUARTZ.get().creativeTabStacks(output::accept);
                ElementsOfPowerItems.EMERALD.get().creativeTabStacks(output::accept);
                ElementsOfPowerItems.DIAMOND.get().creativeTabStacks(output::accept);
                ElementsOfPowerItems.CREATIVITE.get().creativeTabStacks(output::accept);
            }).build()
    );

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
        CREATIVE_TABS.register(modEventBus);
        PLACEMENT_MODIFIER_TYPES.register(modEventBus);
        INGREDIENT_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::modelRegistry);
        modEventBus.addListener(this::registerCapabilities);
    }

    public void modelRegistry(ModelEvent.RegisterGeometryLoaders event)
    {
        event.register("nbt_to_model", NbtToModel.Loader.INSTANCE);
        event.register("staff_model", StaffModel.Loader.INSTANCE);
        //event.register(location("nbt_to_model"), NbtToModel.Loader.INSTANCE);
        //event.register(location("staff_model"), StaffModel.Loader.INSTANCE);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents
    {
        @SubscribeEvent
        public static void registerParticleFactory(RegisterParticleProvidersEvent event)
        {
            event.registerSpriteSet(COLORED_SMOKE_DATA.get(), ColoredSmokeData.Factory::new);
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(BALL_ENTITY_TYPE.get(), BallEntityRenderer::new);
            event.registerEntityRenderer(PILLAR_ENTITY_TYPE.get(), PillarEntityRenderer::new);

            event.registerBlockEntityRenderer(ESSENTIALIZER_BLOCK_ENTITY.get(), EssentializerTileEntityRender::new);
        }

        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(PillarModel.LAYER_LOCATION, PillarModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                MenuScreens.register(ANALYZER_MENU.get(), AnalyzerScreen::new);
                MenuScreens.register(ESSENTIALIZER_MENU.get(), EssentializerScreen::new);

                Gemstone.values.forEach(gem -> {
                    if (gem.generateCustomOre())
                    {
                        for (var ore : gem.getOres())
                        {
                            ItemBlockRenderTypes.setRenderLayer(ore, RenderType.translucent());
                        }
                    }
                });

                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.DUST.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.MIST.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.CUSHION.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.LIGHT.get(), RenderType.translucent());
            });
            WandUseManager.initialize();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(UpdateSpellSequence.class, messageNumber++, PlayNetworkDirection.PLAY_TO_SERVER).encoder(UpdateSpellSequence::encode).decoder(UpdateSpellSequence::new).consumerNetworkThread(UpdateSpellSequence::handle).add();
        CHANNEL.messageBuilder(SynchronizeSpellcastState.class, messageNumber++, PlayNetworkDirection.PLAY_TO_CLIENT).encoder(SynchronizeSpellcastState::encode).decoder(SynchronizeSpellcastState::new).consumerNetworkThread(SynchronizeSpellcastState::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerAmounts.class, messageNumber++, PlayNetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerAmounts::encode).decoder(UpdateEssentializerAmounts::new).consumerNetworkThread(UpdateEssentializerAmounts::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerTile.class, messageNumber++, PlayNetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerTile::encode).decoder(UpdateEssentializerTile::new).consumerNetworkThread(UpdateEssentializerTile::handle).add();
        CHANNEL.messageBuilder(AddVelocityToPlayer.class, messageNumber++, PlayNetworkDirection.PLAY_TO_CLIENT).encoder(AddVelocityToPlayer::encode).decoder(AddVelocityToPlayer::new).consumerNetworkThread(AddVelocityToPlayer::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);

        SpellcastEntityData.register();
        //DiscoveryHandler.init();
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        MagicContainerCapability.register(event);
        PlayerCombinedMagicContainers.register(event);
        CocoonEventHandling.enable(event);
    }

    public void gatherData(GatherDataEvent event)
    {
        ElementsOfPowerDataGen.gatherData(event);
    }


    private void test()
    {
        Either<List<String>, String> e = Either.left(List.of("a"));

        List<String> list = e.map(Function.identity(), List::of);
    }

}


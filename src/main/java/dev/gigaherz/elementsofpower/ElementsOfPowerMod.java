package dev.gigaherz.elementsofpower;

import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerMenu;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerScreen;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.client.ModelVariants;
import dev.gigaherz.elementsofpower.client.NbtToModel;
import dev.gigaherz.elementsofpower.client.StaffModel;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
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

import java.util.*;

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

    public static final RegistryObject<PlacementModifierType<CocoonPlacement>> COCOON_PLACEMENT = PLACEMENT_MODIFIER_TYPES.register("cocoon_placement", () -> CocoonPlacement::codec);

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB_MAGIC = CREATIVE_TABS.register("magic", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP,0)
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
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS, CREATIVE_TAB_MAGIC.getKey())
            .displayItems((featureFlags, output) -> {


                output.accept(ElementsOfPowerItems.RUBY_ORE.get());
                output.accept(ElementsOfPowerItems.SAPPHIRE_ORE.get());
                output.accept(ElementsOfPowerItems.CITRINE_ORE.get());
                output.accept(ElementsOfPowerItems.AGATE_ORE.get());
                output.accept(ElementsOfPowerItems.ONYX_ORE.get());
                output.accept(ElementsOfPowerItems.rubellite_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_RUBY_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_SAPPHIRE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_CITRINE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_AGATE_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_ONYX_ORE.get());
                output.accept(ElementsOfPowerItems.DEEPSLATE_rubellite_ORE.get());
                output.accept(ElementsOfPowerItems.RUBY_BLOCK.get());
                output.accept(ElementsOfPowerItems.SAPPHIRE_BLOCK.get());
                output.accept(ElementsOfPowerItems.CITRINE_BLOCK.get());
                output.accept(ElementsOfPowerItems.AGATE_BLOCK.get());
                output.accept(ElementsOfPowerItems.ONYX_BLOCK.get());
                output.accept(ElementsOfPowerItems.rubellite_BLOCK.get());

                ElementsOfPowerItems.RUBY.get().addToTab(output::accept);
                ElementsOfPowerItems.SAPPHIRE.get().addToTab(output::accept);
                ElementsOfPowerItems.CITRINE.get().addToTab(output::accept);
                ElementsOfPowerItems.AGATE.get().addToTab(output::accept);
                ElementsOfPowerItems.QUARTZ.get().addToTab(output::accept);
                ElementsOfPowerItems.ONYX.get().addToTab(output::accept);
                ElementsOfPowerItems.EMERALD.get().addToTab(output::accept);
                ElementsOfPowerItems.RUBELLITE.get().addToTab(output::accept);
                ElementsOfPowerItems.DIAMOND.get().addToTab(output::accept);
                ElementsOfPowerItems.CREATIVITE.get().addToTab(output::accept);
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

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerParticleFactory);
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
        event.register("staff_model", StaffModel.Loader.INSTANCE);
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

    public void gatherData(GatherDataEvent event)
    {
        ElementsofPowerDataGen.gatherData(event);
    }

}


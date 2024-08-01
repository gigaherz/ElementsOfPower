package dev.gigaherz.elementsofpower;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import dev.gigaherz.elementsofpower.advancements.SpellCastTrigger;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerMenu;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerScreen;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.client.StaffModel;
import dev.gigaherz.elementsofpower.client.WandUseManager;
import dev.gigaherz.elementsofpower.client.models.PillarModel;
import dev.gigaherz.elementsofpower.client.renderers.EssentializerTileEntityRender;
import dev.gigaherz.elementsofpower.client.renderers.entities.BallEntityRenderer;
import dev.gigaherz.elementsofpower.client.renderers.entities.PillarEntityRenderer;
import dev.gigaherz.elementsofpower.cocoons.*;
import dev.gigaherz.elementsofpower.entities.BallEntity;
import dev.gigaherz.elementsofpower.entities.PillarEntity;
import dev.gigaherz.elementsofpower.essentializer.ColoredSmokeData;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerMenu;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerScreen;
import dev.gigaherz.elementsofpower.gemstones.AnalyzedFilteringIngredient;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.items.TransferMode;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.*;
import dev.gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mod(ElementsOfPowerMod.MODID)
public class ElementsOfPowerMod
{
    public static final String MODID = "elementsofpower";

    public static ResourceLocation location(String location)
    {
        return  ResourceLocation.fromNamespaceAndPath(MODID, location);
    }

    private static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(BuiltInRegistries.FEATURE, MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES, MODID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
    private static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MODID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MagicAmounts>>
            CONTAINED_MAGIC = DATA_COMPONENTS.register("contained_magic", () -> DataComponentType.<MagicAmounts>builder()
                    .persistent(MagicAmounts.CODEC).networkSynchronized(MagicAmounts.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Quality>>
            GEMSTONE_QUALITY = DATA_COMPONENTS.register("gemstone_quality", () -> DataComponentType.<Quality>builder()
            .persistent(Quality.CODEC).networkSynchronized(Quality.STREAM_CODEC)
            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TransferMode>>
            TRANSFER_MODE = DATA_COMPONENTS.register("transfer_mode", () -> DataComponentType.<TransferMode>builder()
            .persistent(TransferMode.CODEC).networkSynchronized(TransferMode.STREAM_CODEC)
            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>>
            SPELL_SEQUENCE = DATA_COMPONENTS.register("spell_sequence", () -> DataComponentType.<List<String>>builder()
            .persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.stringUtf8(32768)))
            .build());

    public static final DeferredHolder<EntityType<?>, EntityType<BallEntity>>
            BALL_ENTITY_TYPE = ENTITY_TYPES.register("spell_ball", () -> EntityType.Builder.<BallEntity>of(BallEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true)
                            .build(location("spell_ball").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<PillarEntity>>
            PILLAR_ENTITY_TYPE = ENTITY_TYPES.register("spell_pillar", () -> EntityType.Builder.<PillarEntity>of(PillarEntity::new, MobCategory.MISC)
                    .sized(14/16.0f, 31/16.0f).fireImmune()
                    .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true)
                            .build(location("spell_pillar").toString()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssentializerBlockEntity>>
            ESSENTIALIZER_BLOCK_ENTITY = BLOCK_ENTITIES.register("essentializer", () ->
                    BlockEntityType.Builder.of(EssentializerBlockEntity::new, ElementsOfPowerBlocks.ESSENTIALIZER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CocoonTileEntity>>
            COCOON_BLOCL_ENTITY = BLOCK_ENTITIES.register("cocoon", () ->
                    BlockEntityType.Builder.of(CocoonTileEntity::new, Element.stream_without_balance().map(Element::getCocoon).filter(Objects::nonNull).toArray(Block[]::new)).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<EssentializerMenu>>
            ESSENTIALIZER_MENU = MENU_TYPES.register("essentializer", () -> new MenuType<>(EssentializerMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<AnalyzerMenu>>
            ANALYZER_MENU = MENU_TYPES.register("analyzer", () -> IMenuTypeExtension.create(AnalyzerMenu::new));

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ContainerChargeRecipe>>
            CONTAINER_CHARGE = RECIPE_SERIALIZERS.register("container_charge", () -> new SimpleCraftingRecipeSerializer<>(ContainerChargeRecipe::new));

    public static final DeferredHolder<Feature<?>, CocoonFeature>
            COCOON_FEATURE = FEATURES.register("cocoon", () -> new CocoonFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColoredSmokeData>>
            COLORED_SMOKE_DATA = PARTICLE_TYPES.register("colored_smoke", () -> new ColoredSmokeData.Type(false));

    public static DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<ApplyOrbSizeFunction>>
            APPLY_ORB_SIZE = LOOT_FUNCTION_TYPES.register("apply_orb_size", () -> new LootItemFunctionType<>(ApplyOrbSizeFunction.CODEC));

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<CocoonPlacement>>
            COCOON_PLACEMENT = PLACEMENT_MODIFIER_TYPES.register("cocoon_placement", () -> CocoonPlacement::codec);

    public static final DeferredHolder<IngredientType<?>, IngredientType<AnalyzedFilteringIngredient>>
            ANALYZED_FILTERING_INGREDIENT = INGREDIENT_TYPES.register("analyzed_filtering_ingredient", () ->
                    new IngredientType<>(AnalyzedFilteringIngredient.CODEC, AnalyzedFilteringIngredient.STREAM_CODEC));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab>
            CREATIVE_TAB_MAGIC = CREATIVE_TABS.register("magic", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP,0)
                    .icon(() -> new ItemStack(ElementsOfPowerItems.WAND.get())/*.getStack(Gemstone.DIAMOND, Quality.COMMON)*/)
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


                    }).build());

    public static final Supplier<AttachmentType<CocoonEventHandling.CocoonTracker>>
            COCOON_TRACKER = ATTACHMENT_TYPES.register("cocoon_tracker", () -> AttachmentType.builder(CocoonEventHandling.CocoonTracker::new).build());

    public static final Supplier<AttachmentType<SpellcastState>>
            SPELLCAST_STATE = ATTACHMENT_TYPES.register("spellcast_state", () -> AttachmentType.builder(SpellcastState::new).build());


    public static final DeferredHolder<CriterionTrigger<?>, SpellCastTrigger>
            SPELLCAST_TRIGGER = CRITERION_TRIGGERS.register("spellcast", SpellCastTrigger::new);

    public static Logger LOGGER = LogManager.getLogger(MODID);

    public ElementsOfPowerMod(IEventBus modEventBus)
    {
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
        ATTACHMENT_TYPES.register(modEventBus);
        CRITERION_TRIGGERS.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);

        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::modelRegistry);
        modEventBus.addListener(this::registerCapabilities);


    }
    private void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        MagicContainerCapability.registerCapabilities(event);
        PlayerCombinedMagicContainers.registerCapabilities(event);
    }

    public void modelRegistry(ModelEvent.RegisterGeometryLoaders event)
    {
//        event.register(location("nbt_to_model"), NbtToModel.Loader.INSTANCE);
        event.register(location("staff_model"), StaffModel.Loader.INSTANCE);
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = EventBusSubscriber.Bus.MOD)
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
        public static void menuScreens(RegisterMenuScreensEvent event)
        {
            event.register(ANALYZER_MENU.get(), AnalyzerScreen::new);
            event.register(ESSENTIALIZER_MENU.get(), EssentializerScreen::new);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.DUST.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.MIST.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.CUSHION.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.LIGHT.get(), RenderType.translucent());
            });
            WandUseManager.initialize();
        }

        @SubscribeEvent
        public static void clientExtensions(RegisterClientExtensionsEvent event)
        {
            event.registerItem(new IClientItemExtensions() {
                @Override
                public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand,
                                                       float partialTick, float equipProcess, float swingProcess) {
                    return WandUseManager.instance.applyCustomArmTransforms(poseStack, player, arm, itemInHand, partialTick, equipProcess, swingProcess);
                }
            }, ElementsOfPowerItems.WAND.get(), ElementsOfPowerItems.STAFF.get());
        }
    }

    private void registerPackets(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");
        registrar.playToServer(UpdateSpellSequence.TYPE, UpdateSpellSequence.STREAM_CODEC, UpdateSpellSequence::handle);
        registrar.playToClient(SynchronizeSpellcastState.TYPE, SynchronizeSpellcastState.STREAM_CODEC, SynchronizeSpellcastState::handle);
        registrar.playToClient(UpdateEssentializerAmounts.TYPE, UpdateEssentializerAmounts.STREAM_CODEC, UpdateEssentializerAmounts::handle);
        registrar.playToClient(UpdateEssentializerTile.TYPE, UpdateEssentializerTile.STREAM_CODEC, UpdateEssentializerTile::handle);
        registrar.playToClient(AddVelocityToPlayer.TYPE, AddVelocityToPlayer.STREAM_CODEC, AddVelocityToPlayer::handle);
        registrar.playToClient(ParticlesInShape.TYPE, ParticlesInShape.STREAM_CODEC, ParticlesInShape::handle);
    }

    public void gatherData(GatherDataEvent event)
    {
        ElementsOfPowerDataGen.gatherData(event);
    }
}


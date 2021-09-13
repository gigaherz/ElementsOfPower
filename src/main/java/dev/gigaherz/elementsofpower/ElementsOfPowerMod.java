package dev.gigaherz.elementsofpower;

import dev.gigaherz.elementsofpower.analyzer.AnalyzerItem;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerContainer;
import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerScreen;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import dev.gigaherz.elementsofpower.client.NbtToModel;
import dev.gigaherz.elementsofpower.client.WandUseManager;
import dev.gigaherz.elementsofpower.client.renderers.BallEntityRenderer;
import dev.gigaherz.elementsofpower.client.renderers.EssenceEntityRenderer;
import dev.gigaherz.elementsofpower.client.renderers.EssentializerTileEntityRender;
import dev.gigaherz.elementsofpower.client.renderers.MagicContainerOverlay;
import dev.gigaherz.elementsofpower.cocoons.*;
import dev.gigaherz.elementsofpower.entities.BallEntity;
import dev.gigaherz.elementsofpower.entities.EssenceEntity;
import dev.gigaherz.elementsofpower.essentializer.ColoredSmokeData;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlock;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerContainer;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerScreen;
import dev.gigaherz.elementsofpower.gemstones.*;
import dev.gigaherz.elementsofpower.items.BaubleItem;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.items.StaffItem;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.network.*;
import dev.gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import dev.gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import dev.gigaherz.elementsofpower.spelldust.SpelldustItem;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.SpellcastEntityData;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

@Mod(ElementsOfPowerMod.MODID)
public class ElementsOfPowerMod
{
    public static final String MODID = "elementsofpower";

    public static LootItemFunctionType APPLY_ORB_SIZE;

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }

    public static final MobCategory ESSENCE_CLASSIFICATION = MobCategory.create("EOP_LIVING_ESSENCE", "eop_living_essence", 15, true, false, 32);

    public static String fixDescription(String description)
    {
        return description.endsWith(":NOFML\uFFFDr") ? description.substring(0, description.length() - 8) + "\uFFFDr" : description;
    }

    // FIXME: Remove once spawn eggs can take a supplier
    // To be used only during loading.
    private final NonNullLazy<EntityType<BallEntity>> spellBallInit = NonNullLazy.of(() -> EntityType.Builder.<BallEntity>of(BallEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).build(location("spell_ball").toString()));
    private final NonNullLazy<EntityType<EssenceEntity>> essenceInit = NonNullLazy.of(() -> EntityType.Builder.<EssenceEntity>of(EssenceEntity::new, ESSENCE_CLASSIFICATION)
            .sized(0.5f, 0.5f)
            .setTrackingRange(20).setUpdateInterval(5).setShouldReceiveVelocityUpdates(true).build(location("essence").toString()));

    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(location("general"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static Logger LOGGER = LogManager.getLogger(MODID);

    public static CreativeModeTab tabMagic = new CreativeModeTab("elementsofpower.magic")
    {
        @Override
        public ItemStack makeIcon()
        {
            return ElementsOfPowerItems.WAND.getStack(Gemstone.DIAMOND, Quality.COMMON);
        }
    };

    public static CreativeModeTab tabGemstones = new CreativeModeTab("elementsofpower.gemstones")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(Gemstone.RUBY);
        }
    };

    public ElementsOfPowerMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);
        modEventBus.addGenericListener(EntityType.class, this::registerEntityTypes);
        modEventBus.addGenericListener(BlockEntityType.class, this::registerTileEntityTypes);
        modEventBus.addGenericListener(MenuType.class, this::registerContainerTypes);
        modEventBus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addGenericListener(ParticleType.class, this::registerParticleTypes);
        modEventBus.addGenericListener(Feature.class, this::registerFeature);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerParticleFactory);
        modEventBus.addListener(this::imcEnqueue);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::modelRegistry);
        modEventBus.addListener(this::entityAttributes);
        modEventBus.addListener(this::registerCapabilities);

        MinecraftForge.EVENT_BUS.addListener(this::addStuffToBiomes);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new EssentializerBlock(Block.Properties.of(Material.METAL).strength(15.0F).sound(SoundType.METAL).lightLevel(b -> 1)).setRegistryName("essentializer"),
                new DustBlock(Block.Properties.of(ElementsOfPowerBlocks.BlockMaterials.DUST).noDrops().noCollission().noOcclusion()
                        .isSuffocating((s, w, p) -> true).isViewBlocking((s, w, p) -> false)
                        .strength(0.1F).sound(SoundType.WOOL).dynamicShape()).setRegistryName("dust"),
                new MistBlock(Block.Properties.of(ElementsOfPowerBlocks.BlockMaterials.MIST).noDrops().noCollission().noOcclusion()
                        .isSuffocating((s, w, p) -> false).isViewBlocking((s, w, p) -> false)
                        .strength(0.1F).sound(SoundType.WOOL).dynamicShape()).setRegistryName("mist"),
                new LightBlock(Block.Properties.of(ElementsOfPowerBlocks.BlockMaterials.LIGHT).noDrops().noCollission().noOcclusion()
                        .isSuffocating((s, w, p) -> false).isViewBlocking((s, w, p) -> false)
                        .strength(15.0F).lightLevel(b -> 15).sound(SoundType.METAL)).setRegistryName("light"),
                new CushionBlock(Block.Properties.of(ElementsOfPowerBlocks.BlockMaterials.CUSHION).noDrops().noCollission().noOcclusion()
                        .isSuffocating((s, w, p) -> false).isViewBlocking((s, w, p) -> false)
                        .strength(15.0F).sound(SoundType.METAL).dynamicShape()).setRegistryName("cushion")
        );
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomBlock())
            {
                event.getRegistry().register(
                        new GemstoneBlock(type, Block.Properties.of(Material.METAL)
                                .strength(5F, 6F)
                                .sound(SoundType.METAL)
                        ).setRegistryName(type.getSerializedName() + "_block")
                );
            }
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomOre())
            {
                event.getRegistry().registerAll(
                        new GemstoneOreBlock(type, Block.Properties.of(Material.STONE)
                                .strength(3.0F, 3.0F)
                        ).setRegistryName(type.getSerializedName() + "_ore")
                );
            }
        }
        for (Element type : Element.values())
        {
            event.getRegistry().registerAll(
                    new CocoonBlock(type, Block.Properties.of(Material.STONE).strength(1F)
                            .sound(SoundType.WOOD).lightLevel(b -> 11)).setRegistryName(type.getName() + "_cocoon")
            );
        }
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(ElementsOfPowerBlocks.ESSENTIALIZER, new Item.Properties().tab(tabMagic)).setRegistryName(Objects.requireNonNull(ElementsOfPowerBlocks.ESSENTIALIZER.getRegistryName())),

                new WandItem(new Item.Properties().tab(tabMagic).stacksTo(1)).setRegistryName("wand"),
                new StaffItem(new Item.Properties().tab(tabMagic).stacksTo(1)).setRegistryName("staff"),
                new BaubleItem(new Item.Properties().tab(tabMagic).stacksTo(1)).setRegistryName("ring"),
                new BaubleItem(new Item.Properties().tab(tabMagic).stacksTo(1)).setRegistryName("headband"),
                new BaubleItem(new Item.Properties().tab(tabMagic).stacksTo(1)).setRegistryName("necklace"),
                new AnalyzerItem(new Item.Properties().tab(tabMagic).stacksTo(1)).setRegistryName("analyzer"),

                new SpawnEggItem(essenceInit.get(), 0x0000FF, 0xFFFF00, new Item.Properties().tab(tabMagic)).setRegistryName("essence")
        );
        for (Gemstone type : Gemstone.values())
        {
            event.getRegistry().register(new GemstoneItem(type, new Item.Properties().tab(tabGemstones).stacksTo(1)).setRegistryName(type.getSerializedName()));
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomBlock())
                event.getRegistry().register(new BlockItem(type.getBlock(), new Item.Properties().tab(tabMagic)).setRegistryName(type.getSerializedName() + "_block"));
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomOre())
                event.getRegistry().register(new BlockItem(type.getOre(), new Item.Properties().tab(tabMagic)).setRegistryName(type.getSerializedName() + "_ore"));
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateSpelldust())
                event.getRegistry().register(new SpelldustItem(type, new Item.Properties().tab(tabMagic)).setRegistryName(type.getSerializedName() + "_spelldust"));
        }
        for (Element type : Element.values())
        {
            event.getRegistry().register(
                    new MagicOrbItem(type, new Item.Properties().tab(tabMagic)).setRegistryName(type.getName() + "_orb")
            );
        }
        for (Element type : Element.values())
        {
            event.getRegistry().register(
                    new BlockItem(type.getCocoon(), new Item.Properties().tab(tabMagic)).setRegistryName(type.getName() + "_cocoon")
            );
        }
    }

    public void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry().registerAll(
                spellBallInit.get().setRegistryName("ball"),
                essenceInit.get().setRegistryName("essence")
        );
    }

    public void registerTileEntityTypes(RegistryEvent.Register<BlockEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                BlockEntityType.Builder.of(EssentializerBlockEntity::new, ElementsOfPowerBlocks.ESSENTIALIZER).build(null).setRegistryName("essentializer"),
                BlockEntityType.Builder.of(CocoonTileEntity::new,
                        Arrays.stream(Element.values()).map(Element::getCocoon).toArray(Block[]::new)
                ).build(null).setRegistryName("cocoon")
        );
    }

    public void registerContainerTypes(RegistryEvent.Register<MenuType<?>> event)
    {
        event.getRegistry().registerAll(
                new MenuType<>(EssentializerContainer::new).setRegistryName("essentializer"),
                IForgeContainerType.create(AnalyzerContainer::new).setRegistryName("analyzer")
        );
    }

    public void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SimpleRecipeSerializer<>(ContainerChargeRecipe::new).setRegistryName("container_charge"),
                new SimpleRecipeSerializer<>(GemstoneChangeRecipe::new).setRegistryName("gemstone_change")
        );

        // FIXME
        APPLY_ORB_SIZE = LootItemFunctions.register(location("apply_orb_size").toString(), new ApplyOrbSizeFunction.Serializer());
    }

    public void registerFeature(RegistryEvent.Register<Feature<?>> event)
    {
        event.getRegistry().registerAll(
                new CocoonFeature(CocoonFeatureConfig.CODEC).setRegistryName("cocoon")
        );
    }

    public void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event)
    {
        event.getRegistry().registerAll(
                new ColoredSmokeData.Type(false).setRegistryName("colored_smoke")
        );
    }

    public void registerParticleFactory(ParticleFactoryRegisterEvent event)
    {
        Minecraft.getInstance().particleEngine.register(ColoredSmokeData.TYPE, ColoredSmokeData.Factory::new);
    }

    private void entityAttributes(EntityAttributeCreationEvent event)
    {
        event.put(EssenceEntity.TYPE, EssenceEntity.prepareAttributes().build());
    }

    public void modelRegistry(ModelRegistryEvent event)
    {
        ModelLoaderRegistry.registerLoader(location("nbt_to_model"), NbtToModel.Loader.INSTANCE);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents
    {
        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(EssenceEntity.TYPE, EssenceEntityRenderer::new);
            event.registerEntityRenderer(BallEntity.TYPE, BallEntityRenderer::new);

            event.registerBlockEntityRenderer(EssentializerBlockEntity.TYPE, EssentializerTileEntityRender::new);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                MenuScreens.register(AnalyzerContainer.TYPE, AnalyzerScreen::new);
                MenuScreens.register(EssentializerContainer.TYPE, EssentializerScreen::new);
            });

            MagicContainerOverlay.init();

            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.DUST, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.MIST, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.CUSHION, RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ElementsOfPowerBlocks.LIGHT, RenderType.translucent());
            Arrays.stream(Element.values).forEach(e ->
                    ItemBlockRenderTypes.setRenderLayer(e.getCocoon(), layer -> layer == RenderType.translucent() || layer == RenderType.solid())
            );

            MinecraftForge.EVENT_BUS.register(new WandUseManager());


            WandUseManager.instance.initialize();
        }
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(UpdateSpellSequence.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(UpdateSpellSequence::encode).decoder(UpdateSpellSequence::new).consumer(UpdateSpellSequence::handle).add();
        CHANNEL.messageBuilder(SynchronizeSpellcastState.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SynchronizeSpellcastState::encode).decoder(SynchronizeSpellcastState::new).consumer(SynchronizeSpellcastState::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerAmounts.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerAmounts::encode).decoder(UpdateEssentializerAmounts::new).consumer(UpdateEssentializerAmounts::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerTile.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerTile::encode).decoder(UpdateEssentializerTile::new).consumer(UpdateEssentializerTile::handle).add();
        CHANNEL.messageBuilder(AddVelocityToPlayer.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(AddVelocityToPlayer::encode).decoder(AddVelocityToPlayer::new).consumer(AddVelocityToPlayer::handle).add();
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

    private void addStuffToBiomes(BiomeLoadingEvent event)
    {
        ResourceKey<Biome> biome = ResourceKey.create(Registry.BIOME_REGISTRY, event.getName());
        if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.VOID))
        {
            boolean isEndBiome = BiomeDictionary.hasType(biome, BiomeDictionary.Type.END);
            boolean isNetherBiome = BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER);
            if (!isEndBiome && !isNetherBiome)
            {
                for (Gemstone g : Gemstone.values)
                {
                    if (g.generateCustomOre())
                    {
                        int numPerVein = 3 + getBiomeBonus(g.getElement(), biome);
                        event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE
                                .configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, g.getOre().defaultBlockState(), numPerVein))
                                .countRandom(16).squared());
                    }
                }
            }

            CocoonFeatureConfig cfg;
            if (isNetherBiome)
                cfg = CocoonFeatureConfig.THE_NETHER;
            else if (isEndBiome)
                cfg = CocoonFeatureConfig.THE_END;
            else
                cfg = CocoonFeatureConfig.OVERWORLD;

            event.getGeneration().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CocoonFeature.INSTANCE.configured(cfg)
                    .decorated(CocoonPlacement.INSTANCE.configured(NoneDecoratorConfiguration.NONE)));
        }
    }

    private int getBiomeBonus(@Nullable Element e, ResourceKey<Biome> biome)
    {
        if (e == null)
            return 1;
        switch (e)
        {
            case FIRE:
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT))
                    return 2;
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD))
                    return 0;
                return 1;
            case WATER:
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET))
                    return 2;
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY))
                    return 0;
                return 1;
            case LIFE:
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DENSE))
                    return 2;
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPARSE))
                    return 0;
                return 1;
            case DEATH:
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPARSE))
                    return 2;
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.DENSE))
                    return 0;
                return 1;
        }
        return 1;
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
}

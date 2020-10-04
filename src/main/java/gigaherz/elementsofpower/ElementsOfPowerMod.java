package gigaherz.elementsofpower;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import gigaherz.elementsofpower.analyzer.AnalyzerItem;
import gigaherz.elementsofpower.analyzer.gui.AnalyzerContainer;
import gigaherz.elementsofpower.analyzer.gui.AnalyzerScreen;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.capabilities.PlayerCombinedMagicContainers;
import gigaherz.elementsofpower.client.NbtToModel;
import gigaherz.elementsofpower.client.WandUseManager;
import gigaherz.elementsofpower.client.renderers.BallEntityRenderer;
import gigaherz.elementsofpower.client.renderers.EssenceEntityRenderer;
import gigaherz.elementsofpower.client.renderers.EssentializerTileEntityRender;
import gigaherz.elementsofpower.client.renderers.MagicContainerOverlay;
import gigaherz.elementsofpower.cocoons.*;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.GemstoneExaminer;
import gigaherz.elementsofpower.entities.BallEntity;
import gigaherz.elementsofpower.entities.EssenceEntity;
import gigaherz.elementsofpower.essentializer.ColoredSmokeData;
import gigaherz.elementsofpower.essentializer.EssentializerBlock;
import gigaherz.elementsofpower.essentializer.EssentializerTileEntity;
import gigaherz.elementsofpower.essentializer.gui.EssentializerContainer;
import gigaherz.elementsofpower.essentializer.gui.EssentializerScreen;
import gigaherz.elementsofpower.gemstones.*;
import gigaherz.elementsofpower.items.BaubleItem;
import gigaherz.elementsofpower.items.MagicOrbItem;
import gigaherz.elementsofpower.items.StaffItem;
import gigaherz.elementsofpower.items.WandItem;
import gigaherz.elementsofpower.network.*;
import gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import gigaherz.elementsofpower.spelldust.SpelldustItem;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.spells.blocks.CushionBlock;
import gigaherz.elementsofpower.spells.blocks.DustBlock;
import gigaherz.elementsofpower.spells.blocks.LightBlock;
import gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.command.CommandSource;
import net.minecraft.data.*;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod(ElementsOfPowerMod.MODID)
public class ElementsOfPowerMod
{
    public static final String MODID = "elementsofpower";

    public static LootFunctionType APPLY_ORB_SIZE;

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }

    public static final EntityClassification ESSENCE_CLASSIFICATION = EntityClassification.create("EOP_LIVING_ESSENCE", "eop_living_essence", 15, true, false, 32);

    // FIXME: Remove once spawn eggs can take a supplier
    // To be used only during loading.
    private final NonNullLazy<EntityType<BallEntity>> spellBallInit = NonNullLazy.of(() -> EntityType.Builder.<BallEntity>create(BallEntity::new, EntityClassification.MISC)
            .size(0.5f, 0.5f)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).build(location("spell_ball").toString()));
    private final NonNullLazy<EntityType<EssenceEntity>> essenceInit = NonNullLazy.of(() -> EntityType.Builder.<EssenceEntity>create(EssenceEntity::new, ESSENCE_CLASSIFICATION)
            .size(0.5f, 0.5f)
            .setTrackingRange(20).setUpdateInterval(5).setShouldReceiveVelocityUpdates(true).build(location("essence").toString()));

    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(location("general"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static Logger LOGGER = LogManager.getLogger(MODID);

    public static ItemGroup tabMagic = new ItemGroup("elementsofpower.magic")
    {
        @Override
        public ItemStack createIcon()
        {
            return ElementsOfPowerItems.WAND.getStack(Gemstone.DIAMOND, Quality.COMMON);
        }
    };

    public static ItemGroup tabGemstones = new ItemGroup("elementsofpower.gemstones")
    {
        @Override
        public ItemStack createIcon()
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
        modEventBus.addGenericListener(TileEntityType.class, this::registerTileEntityTypes);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainerTypes);
        modEventBus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
        modEventBus.addGenericListener(ParticleType.class, this::registerParticleTypes);
        modEventBus.addGenericListener(Feature.class, this::registerFeature);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerParticleFactory);
        modEventBus.addListener(this::imcEnqueue);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::modelRegistry);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::addStuffToBiomes);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new EssentializerBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL).setLightLevel(b -> 1)).setRegistryName("essentializer"),
                new DustBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.DUST).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s,w,p) -> true).setBlocksVision((s,w,p)-> false)
                        .hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("dust"),
                new MistBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.MIST).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s,w,p) -> false).setBlocksVision((s,w,p)-> false)
                        .hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("mist"),
                new LightBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.LIGHT).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s,w,p) -> false).setBlocksVision((s,w,p)-> false)
                        .hardnessAndResistance(15.0F).setLightLevel(b -> 15).sound(SoundType.METAL)).setRegistryName("light"),
                new CushionBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.CUSHION).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s,w,p) -> false).setBlocksVision((s,w,p)-> false)
                        .hardnessAndResistance(15.0F).sound(SoundType.METAL).variableOpacity()).setRegistryName("cushion")
        );
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomBlock())
            {
                event.getRegistry().register(
                        new GemstoneBlock(type, Block.Properties.create(Material.IRON)
                                .hardnessAndResistance(5F, 6F)
                                .harvestTool(ToolType.PICKAXE)
                                .harvestLevel(ItemTier.IRON.getHarvestLevel())
                                .sound(SoundType.METAL)
                        ).setRegistryName(type.getString() + "_block")
                );
            }
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomOre())
            {
                event.getRegistry().registerAll(
                        new GemstoneOreBlock(type, Block.Properties.create(Material.ROCK)
                                .hardnessAndResistance(3.0F, 3.0F)
                                .harvestTool(ToolType.PICKAXE)
                                .harvestLevel(ItemTier.IRON.getHarvestLevel())
                        ).setRegistryName(type.getString() + "_ore")
                );
            }
        }
        for (Element type : Element.values())
        {
            event.getRegistry().registerAll(
                    new CocoonBlock(type, Block.Properties.create(Material.ROCK).hardnessAndResistance(1F)
                            .sound(SoundType.WOOD).setLightLevel(b -> 11)).setRegistryName(type.getName() + "_cocoon")
            );
        }
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(ElementsOfPowerBlocks.ESSENTIALIZER, new Item.Properties().group(tabMagic)).setRegistryName(Objects.requireNonNull(ElementsOfPowerBlocks.ESSENTIALIZER.getRegistryName())),

                new WandItem(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("wand"),
                new StaffItem(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("staff"),
                new BaubleItem(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("ring"),
                new BaubleItem(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("headband"),
                new BaubleItem(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("necklace"),
                new AnalyzerItem(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("analyzer"),

                new SpawnEggItem(essenceInit.get(), 0x0000FF, 0xFFFF00, new Item.Properties().group(tabMagic)).setRegistryName("essence")
        );
        for (Gemstone type : Gemstone.values())
        {
            event.getRegistry().register(new GemstoneItem(type, new Item.Properties().group(tabGemstones).maxStackSize(1)).setRegistryName(type.getString()));
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomBlock())
                event.getRegistry().register(new BlockItem(type.getBlock(), new Item.Properties().group(tabMagic)).setRegistryName(type.getString() + "_block"));
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateCustomOre())
                event.getRegistry().register(new BlockItem(type.getOre(), new Item.Properties().group(tabMagic)).setRegistryName(type.getString() + "_ore"));
        }
        for (Gemstone type : Gemstone.values())
        {
            if (type.generateSpelldust())
                event.getRegistry().register(new SpelldustItem(type, new Item.Properties().group(tabMagic)).setRegistryName(type.getString() + "_spelldust"));
        }
        for (Element type : Element.values())
        {
            event.getRegistry().register(
                    new MagicOrbItem(type, new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_orb")
            );
        }
        for (Element type : Element.values())
        {
            event.getRegistry().register(
                    new BlockItem(type.getCocoon(), new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_cocoon")
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

    public void registerTileEntityTypes(RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(EssentializerTileEntity::new, ElementsOfPowerBlocks.ESSENTIALIZER).build(null).setRegistryName("essentializer"),
                TileEntityType.Builder.create(CocoonTileEntity::new,
                        Arrays.stream(Element.values()).map(Element::getCocoon).toArray(Block[]::new)
                ).build(null).setRegistryName("cocoon")
        );
    }

    public void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                new ContainerType<>(EssentializerContainer::new).setRegistryName("essentializer"),
                IForgeContainerType.create(AnalyzerContainer::new).setRegistryName("analyzer")
        );
    }

    public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                new SpecialRecipeSerializer<>(ContainerChargeRecipe::new).setRegistryName("container_charge"),
                new SpecialRecipeSerializer<>(GemstoneChangeRecipe::new).setRegistryName("gemstone_change")
        );

        // FIXME
        APPLY_ORB_SIZE = LootFunctionManager.func_237451_a_(location("apply_orb_size").toString(), new ApplyOrbSizeFunction.Serializer());
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
        Minecraft.getInstance().particles.registerFactory(ColoredSmokeData.TYPE, ColoredSmokeData.Factory::new);
    }

    public void modelRegistry(ModelRegistryEvent event)
    {
        ModelLoaderRegistry.registerLoader(location("nbt_to_model"), NbtToModel.Loader.INSTANCE);
    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EssenceEntity.TYPE, EssenceEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(BallEntity.TYPE, BallEntityRenderer::new);

        ClientRegistry.bindTileEntityRenderer(EssentializerTileEntity.TYPE, EssentializerTileEntityRender::new);

        ScreenManager.registerFactory(AnalyzerContainer.TYPE, AnalyzerScreen::new);
        ScreenManager.registerFactory(EssentializerContainer.TYPE, EssentializerScreen::new);

        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.DUST, RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.MIST, RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.CUSHION, RenderType.getTranslucent());
        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.LIGHT, RenderType.getTranslucent());

        MinecraftForge.EVENT_BUS.register(new WandUseManager());
        MinecraftForge.EVENT_BUS.register(new MagicContainerOverlay());

        WandUseManager.instance.initialize();
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(UpdateSpellSequence.class, messageNumber++).encoder(UpdateSpellSequence::encode).decoder(UpdateSpellSequence::new).consumer(UpdateSpellSequence::handle).add();
        CHANNEL.messageBuilder(SynchronizeSpellcastState.class, messageNumber++).encoder(SynchronizeSpellcastState::encode).decoder(SynchronizeSpellcastState::new).consumer(SynchronizeSpellcastState::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerAmounts.class, messageNumber++).encoder(UpdateEssentializerAmounts::encode).decoder(UpdateEssentializerAmounts::new).consumer(UpdateEssentializerAmounts::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerTileEntity.class, messageNumber++).encoder(UpdateEssentializerTileEntity::encode).decoder(UpdateEssentializerTileEntity::new).consumer(UpdateEssentializerTileEntity::handle).add();
        CHANNEL.messageBuilder(AddVelocityToPlayer.class, messageNumber++).encoder(AddVelocityToPlayer::encode).decoder(AddVelocityToPlayer::new).consumer(AddVelocityToPlayer::handle).add();
        CHANNEL.messageBuilder(SyncEssenceConversions.class, messageNumber++).encoder(SyncEssenceConversions::encode).decoder(SyncEssenceConversions::new).consumer(SyncEssenceConversions::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);

        MagicContainerCapability.register();
        PlayerCombinedMagicContainers.register();
        SpellcastEntityData.register();
        //DiscoveryHandler.init();

        CraftingHelper.register(AnalyzedFilteringIngredient.ID, AnalyzedFilteringIngredient.Serializer.INSTANCE);

        EssenceConversions.init();

        GlobalEntityTypeAttributes.put(EssenceEntity.TYPE, EssenceEntity.prepareAttributes().create());

        CocoonEventHandling.enable();

        BiomeLabels.loadBiomeConfig();
    }

    private void addStuffToBiomes(BiomeLoadingEvent event)
    {
        ResourceLocation biome = event.getName();
        if (!BiomeLabels.hasType(biome, "void"))
        {
            boolean isEndBiome = BiomeLabels.hasType(biome, "end");
            boolean isNetherBiome = BiomeLabels.hasType(biome, "nether");
            if (!isEndBiome && !isNetherBiome)
            {
                for (Gemstone g : Gemstone.values)
                {
                    if (g.generateCustomOre())
                    {
                        int numPerChunk = 3 + getBiomeBonus(g.getElement(), biome);
                        event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE
                                .withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.field_241882_a, g.getOre().getDefaultState(), numPerChunk))
                                .func_242733_d(16).func_242728_a());
                    }
                }
            }

            CocoonFeatureConfig cfg;
            if (isNetherBiome)
                cfg = CocoonFeatureConfig.THE_NETHER;
            else if(isEndBiome)
                cfg = CocoonFeatureConfig.THE_END;
            else
                cfg = CocoonFeatureConfig.OVERWORLD;

            event.getGeneration().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, CocoonFeature.INSTANCE.withConfiguration(cfg)
                    .withPlacement(CocoonPlacement.INSTANCE.configure(NoPlacementConfig.NO_PLACEMENT_CONFIG)));
        }
    }

    private int getBiomeBonus(@Nullable Element e, ResourceLocation biome)
    {
        if (e == null)
            return 1;
        switch (e)
        {
            case FIRE:
                if (BiomeLabels.hasType(biome, "hot"))
                    return 2;
                if (BiomeLabels.hasType(biome, "cold"))
                    return 0;
                return 1;
            case WATER:
                if (BiomeLabels.hasType(biome, "wet"))
                    return 2;
                if (BiomeLabels.hasType(biome, "dry"))
                    return 0;
                return 1;
            case LIFE:
                if (BiomeLabels.hasType(biome, "dense"))
                    return 2;
                if (BiomeLabels.hasType(biome, "sparse"))
                    return 0;
                return 1;
            case DEATH:
                if (BiomeLabels.hasType(biome, "sparse"))
                    return 2;
                if (BiomeLabels.hasType(biome, "dense"))
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

    public void registerCommands(RegisterCommandsEvent event)
    {
        LiteralArgumentBuilder<CommandSource> s = LiteralArgumentBuilder.literal("elementsofpower");
        {
            EssenceConversions.registerSubcommands(s);
        }
        event.getDispatcher().register(s);
    }

    public void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        if (event.includeServer())
        {
            BlockTagGens blockTags = new BlockTagGens(gen);
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new LootTables(gen));
            gen.addProvider(blockTags);
            gen.addProvider(new ItemTagGens(gen, blockTags));
        }
        if (event.includeClient())
        {
            gen.addProvider(new BlockStates(gen, event));
        }
    }

    private static class ItemTagGens extends ItemTagsProvider implements IDataProvider
    {
        public ItemTagGens(DataGenerator gen, BlockTagsProvider blockTags)
        {
            super(gen, blockTags);
        }

        @Override
        protected void registerTags()
        {
            GemstoneExaminer.GEMS.forEach((gem, tag) -> {
                this.getOrCreateBuilder(tag).add(gem.getTagItems());
            });
            Gemstone.values.forEach(gem -> {
                if (gem != Gemstone.CREATIVITE)
                {
                    ITag.INamedTag<Item> tag = ItemTags.makeWrapperTag(new ResourceLocation("forge", "gems/" + gem.getString()).toString());
                    this.getOrCreateBuilder(tag).add(gem.getTagItems());
                }
                if (gem.generateCustomOre())
                {
                    Block ore = gem.getOre();
                    ITag.INamedTag<Item> tag = ItemTags.makeWrapperTag(new ResourceLocation("forge", "ores/" + gem.getString()).toString());
                    this.getOrCreateBuilder(tag).add(ore.asItem());
                }
                if (gem.generateCustomBlock())
                {
                    Block block = gem.getBlock();
                    ITag.INamedTag<Item> tag = ItemTags.makeWrapperTag(new ResourceLocation("forge", "blocks/" + gem.getString()).toString());
                    this.getOrCreateBuilder(tag).add(block.asItem());
                }
            });

            this.getOrCreateBuilder(GemstoneExaminer.GEMSTONES).add(
                    Arrays.stream(Gemstone.values()).flatMap(g -> Arrays.stream(g.getTagItems())).toArray(Item[]::new)
            );
        }
    }

    private static class BlockTagGens extends BlockTagsProvider implements IDataProvider
    {
        public BlockTagGens(DataGenerator gen)
        {
            super(gen);
        }

        @Override
        protected void registerTags()
        {
            this.getOrCreateBuilder(CocoonFeature.REPLACEABLE_TAG)
                    .add(Blocks.SAND, Blocks.RED_SAND, Blocks.DIRT, Blocks.NETHERRACK);
        }
    }

    private static class LootTables extends LootTableProvider implements IDataProvider
    {
        public LootTables(DataGenerator gen)
        {
            super(gen);
        }

        private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = ImmutableList.of(
                Pair.of(BlockTables::new, LootParameterSets.BLOCK)
                //Pair.of(FishingLootTables::new, LootParameterSets.FISHING),
                //Pair.of(ChestLootTables::new, LootParameterSets.CHEST),
                //Pair.of(EntityLootTables::new, LootParameterSets.ENTITY),
                //Pair.of(GiftLootTables::new, LootParameterSets.GIFT)
        );

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables()
        {
            return tables;
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker)
        {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTableManager.validateLootTable(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLootTables
        {
            @Override
            protected void addTables()
            {
                this.registerDropSelfLootTable(ElementsOfPowerBlocks.ESSENTIALIZER);

                for (Element e : Element.values)
                {
                    this.registerLootTable(e.getCocoon(), BlockTables::dropWithOrbs);
                }

                for (Gemstone g : Gemstone.values)
                {
                    if (g.generateCustomBlock())
                        this.registerDropSelfLootTable(g.getBlock());
                    if (g.generateCustomOre())
                        this.registerLootTable(g.getOre(), (block) -> droppingItemWithFortune(block, g.getItem()));
                }
            }

            protected static LootTable.Builder dropWithOrbs(Block block)
            {
                LootTable.Builder builder = LootTable.builder();
                for (Element e : Element.values)
                {
                    LootPool.Builder pool = LootPool.builder()
                            .rolls(ConstantRange.of(1))
                            .addEntry(ItemLootEntry.builder(e.getOrb())
                                    .acceptFunction(ApplyOrbSizeFunction.builder().with(e)));
                    builder = builder.addLootPool(withSurvivesExplosion(block, pool));
                }
                return builder;
            }

            @Override
            protected Iterable<Block> getKnownBlocks()
            {
                return ForgeRegistries.BLOCKS.getValues().stream()
                        .filter(b -> b.getRegistryName().getNamespace().equals(MODID))
                        .collect(Collectors.toList());
            }
        }
    }

    private static class BlockStates extends BlockStateProvider
    {
        public BlockStates(DataGenerator gen, GatherDataEvent event)
        {
            super(gen, ElementsOfPowerMod.MODID, event.getExistingFileHelper());
        }

        @Override
        protected void registerStatesAndModels()
        {
            densityBlock(ElementsOfPowerBlocks.MIST, MistBlock.DENSITY);
            densityBlock(ElementsOfPowerBlocks.DUST, DustBlock.DENSITY);
            densityBlock(ElementsOfPowerBlocks.LIGHT, LightBlock.DENSITY, (value) -> location("transparent"));
            densityBlock(ElementsOfPowerBlocks.CUSHION, CushionBlock.DENSITY, (density) -> location("block/dust_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty)
        {
            densityBlock(block, densityProperty, (density) -> location("block/" + Objects.requireNonNull(block.getRegistryName()).getPath() + "_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty, Function<Integer, ResourceLocation> texMapper)
        {
            Map<Integer, ModelFile> densityModels = Maps.asMap(
                    new HashSet<>(densityProperty.getAllowedValues()),
                    density -> {
                        return models().cubeAll(location(Objects.requireNonNull(block.getRegistryName()).getPath() + "_" + density).getPath(), texMapper.apply(density));
                    });

            getVariantBuilder(block)
                    .forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(densityModels.get(state.get(MistBlock.DENSITY)))
                            .build()
                    );
        }
    }

    private static class Recipes extends RecipeProvider
    {
        public Recipes(DataGenerator gen)
        {
            super(gen);
        }

        @Override
        protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
        {
            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.ANALYZER)
                    .patternLine("glg")
                    .patternLine("i  ")
                    .patternLine("psp")
                    .key('l', Items.GOLD_INGOT)
                    .key('i', Items.IRON_INGOT)
                    .key('g', Tags.Items.GLASS_PANES)
                    .key('p', ItemTags.PLANKS)
                    .key('s', ItemTags.WOODEN_SLABS)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.ESSENTIALIZER)
                    .patternLine("IQI")
                    .patternLine("ONO")
                    .patternLine("IOI")
                    .key('I', Items.IRON_INGOT)
                    .key('O', Items.OBSIDIAN)
                    .key('Q', GemstoneExaminer.GEMSTONES)
                    .key('N', Items.NETHER_STAR)
                    .addCriterion("has_star", hasItem(Items.NETHER_STAR))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.WAND)
                    .patternLine(" G")
                    .patternLine("S ")
                    .key('G', Items.GOLD_INGOT)
                    .key('S', Items.STICK)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.STAFF)
                    .patternLine(" GW")
                    .patternLine(" SG")
                    .patternLine("S  ")
                    .key('G', Items.IRON_BLOCK)
                    .key('S', Items.STICK)
                    .key('W', ElementsOfPowerItems.WAND)
                    .addCriterion("has_wand", hasItem(ElementsOfPowerItems.WAND))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.RING)
                    .patternLine(" GG")
                    .patternLine("G G")
                    .patternLine(" G ")
                    .key('G', Items.GOLD_INGOT)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.NECKLACE)
                    .patternLine("GGG")
                    .patternLine("G G")
                    .patternLine(" G ")
                    .key('G', Items.GOLD_INGOT)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            ShapedRecipeBuilder.shapedRecipe(ElementsOfPowerItems.HEADBAND)
                    .patternLine(" G ")
                    .patternLine("G G")
                    .patternLine("GGG")
                    .key('G', Items.GOLD_INGOT)
                    .addCriterion("has_gold", hasItem(Items.GOLD_INGOT))
                    .build(consumer);

            CustomRecipeBuilder.customRecipe(GemstoneChangeRecipe.SERIALIZER).build(consumer, location("gemstone_change").toString());
            CustomRecipeBuilder.customRecipe(ContainerChargeRecipe.SERIALIZER).build(consumer, location("container_charge").toString());

            for (Gemstone gemstone : Gemstone.values())
            {
                if (gemstone.generateCustomOre())
                {
                    CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(gemstone.getOre()), gemstone, 1.0F, 200)
                            .addCriterion("has_ore", hasItem(gemstone.getOre()))
                            .build(consumer, location("smelting/" + gemstone.getString()));
                }
                if (gemstone.generateCustomBlock())
                {
                    ShapedRecipeBuilder.shapedRecipe(gemstone.getBlock())
                            .patternLine("ggg")
                            .patternLine("ggg")
                            .patternLine("ggg")
                            .key('g', AnalyzedFilteringIngredient.wrap(Ingredient.fromItems(gemstone.getItem())))
                            .addCriterion("has_item", hasItem(gemstone.getItem()))
                            .build(consumer);

                    ShapelessRecipeBuilder.shapelessRecipe(gemstone.getItem(), 9)
                            .addIngredient(Ingredient.fromItems(gemstone.getBlock()))
                            .addCriterion("has_item", hasItem(gemstone.getItem()))
                            .build(consumer, location(gemstone.getString() + "_from_block"));
                }
            }
        }
    }
}

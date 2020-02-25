package gigaherz.elementsofpower;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import gigaherz.elementsofpower.analyzer.AnalyzerItem;
import gigaherz.elementsofpower.analyzer.gui.AnalyzerContainer;
import gigaherz.elementsofpower.analyzer.gui.AnalyzerScreen;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.client.NbtToModel;
import gigaherz.elementsofpower.client.WandUseManager;
import gigaherz.elementsofpower.client.renderers.MagicContainerOverlay;
import gigaherz.elementsofpower.client.renderers.BallEntityRenderer;
import gigaherz.elementsofpower.client.renderers.EssenceEntityRenderer;
import gigaherz.elementsofpower.client.renderers.EssentializerTileEntityRender;
import gigaherz.elementsofpower.cocoons.*;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.EssenceOverrides;
import gigaherz.elementsofpower.database.GemstoneExaminer;
import gigaherz.elementsofpower.database.StockConversions;
import gigaherz.elementsofpower.entities.BallEntity;
import gigaherz.elementsofpower.entities.EssenceEntity;
import gigaherz.elementsofpower.essentializer.EssentializerBlock;
import gigaherz.elementsofpower.essentializer.EssentializerTileEntity;
import gigaherz.elementsofpower.essentializer.gui.EssentializerContainer;
import gigaherz.elementsofpower.essentializer.gui.EssentializerScreen;
import gigaherz.elementsofpower.gemstones.*;
import gigaherz.elementsofpower.items.*;
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
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.data.*;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//import gigaherz.elementsofpower.progression.DiscoveryHandler;

@Mod(ElementsOfPowerMod.MODID)
public class ElementsOfPowerMod
{
    public static final String MODID = "elementsofpower";

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }

    // FIXME: Remove once spawn eggs can take a supplier
    // To be used only during loading.
    private final NonNullLazy<EntityType<BallEntity>> spellBallInit = NonNullLazy.of(() -> EntityType.Builder.<BallEntity>create(BallEntity::new, EntityClassification.MISC)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).build(location("spell_ball").toString()));
    private final NonNullLazy<EntityType<EssenceEntity>> essenceInit = NonNullLazy.of(() -> EntityType.Builder.<EssenceEntity>create(EssenceEntity::new, EntityClassification.AMBIENT)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).build(location("essence").toString()));

    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry.ChannelBuilder
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
        modEventBus.addGenericListener(EntityType.class, this::registerEntities);
        modEventBus.addGenericListener(TileEntityType.class, this::registerTileEntities);
        modEventBus.addGenericListener(ContainerType.class, this::registerContainerTypes);
        modEventBus.addGenericListener(IRecipeSerializer.class, this::registerRecipes);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::loadComplete);

        modEventBus.addListener(this::imcEnqueue);

        modEventBus.addListener(this::gatherData);

        MinecraftForge.EVENT_BUS.addListener(this::playerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);

        LootFunctionManager.registerFunction(ApplyOrbSizeFunction.Serializer.INSTANCE);
    }

    private void imcEnqueue(InterModEnqueueEvent event)
    {
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("headband").setSize(1).setEnabled(true).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_ICON, () -> new Tuple<>("headband", location("gui/headband_slot_background")));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("necklace").setSize(1).setEnabled(true).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_ICON, () -> new Tuple<>("necklace", location("gui/necklace_slot_background")));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("ring").setSize(2).setEnabled(true).setHidden(false));
        //InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_ICON, () -> new Tuple<>("ring", location("gui/ring_slot_background")));
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new EssentializerBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL).lightValue(1)).setRegistryName("essentializer"),
                new DustBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.DUST).noDrops().doesNotBlockMovement().notSolid().hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("dust"),
                new MistBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.MIST).noDrops().doesNotBlockMovement().notSolid().hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("mist"),
                new LightBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.LIGHT).noDrops().doesNotBlockMovement().notSolid().hardnessAndResistance(15.0F).lightValue(15).sound(SoundType.METAL)).setRegistryName("light"),
                new CushionBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.CUSHION).noDrops().doesNotBlockMovement().notSolid().hardnessAndResistance(15.0F).sound(SoundType.METAL).variableOpacity()).setRegistryName("cushion")
        );
        for(Gemstone type : Gemstone.values())
        {
            if (type.generateCustomBlock())
            {
                event.getRegistry().register(
                        new GemstoneBlock(type, Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F).sound(SoundType.METAL)).setRegistryName(type.getName() + "_block")
                );
            }
        }
        for(Gemstone type : Gemstone.values())
        {
            if (type.generateCustomOre())
            {
                event.getRegistry().registerAll(
                        new GemstoneBlock(type, Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL)).setRegistryName(type.getName() + "_ore")
                );
            }
        }
        for(Element type : Element.values())
        {
            event.getRegistry().registerAll(
                    new CocoonBlock(type, Block.Properties.create(Material.IRON).hardnessAndResistance(1F).sound(SoundType.METAL).lightValue(11).tickRandomly()).setRegistryName(type.getName() + "_cocoon")
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
        for(Gemstone type : Gemstone.values())
        {
            event.getRegistry().register(new GemstoneItem(type, new Item.Properties().group(tabGemstones).maxStackSize(1)).setRegistryName(type.getName()));
        }
        for(Gemstone type : Gemstone.values())
        {
            if (type.generateCustomBlock())
                event.getRegistry().register(new BlockItem(type.getBlock(), new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_block"));
        }
        for(Gemstone type : Gemstone.values())
        {
            if (type.generateCustomOre())
                event.getRegistry().register(new BlockItem(type.getOre(), new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_ore"));
        }
        for(Gemstone type : Gemstone.values())
        {
            if (type.generateSpelldust())
                event.getRegistry().register(new SpelldustItem(type, new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_spelldust"));
        }
        for(Element type : Element.values())
        {
            event.getRegistry().register(
                    new MagicOrbItem(type, new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_orb")
            );
        }
        for(Element type : Element.values())
        {
            event.getRegistry().register(
                    new BlockItem(type.getCocoon(), new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_cocoon")
            );
        }
    }

    public void registerEntities(RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry().registerAll(
            spellBallInit.get().setRegistryName("ball"),
            essenceInit.get().setRegistryName("essence")
        );
    }

    public void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
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

    public void clientSetup(FMLClientSetupEvent event)
    {
        ModelLoaderRegistry.registerLoader(location("nbt_to_model"), NbtToModel.Loader.INSTANCE);

        RenderingRegistry.registerEntityRenderingHandler(EssenceEntity.TYPE, EssenceEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(BallEntity.TYPE, BallEntityRenderer::new);

        ClientRegistry.bindTileEntityRenderer(EssentializerTileEntity.TYPE, EssentializerTileEntityRender::new);

        ScreenManager.registerFactory(AnalyzerContainer.TYPE, AnalyzerScreen::new);
        ScreenManager.registerFactory(EssentializerContainer.TYPE, EssentializerScreen::new);

        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.DUST, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.MIST, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.CUSHION, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(ElementsOfPowerBlocks.LIGHT, RenderType.translucent());

        MinecraftForge.EVENT_BUS.register(new WandUseManager());
        MinecraftForge.EVENT_BUS.register(new MagicContainerOverlay());

        WandUseManager.instance.initialize();
    }

    public void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().registerAll(
                ContainerChargeRecipe.SERIALIZER.setRegistryName("container_charge"),
                GemstoneChangeRecipe.SERIALIZER.setRegistryName("gemstone_change")
        );
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        channel.messageBuilder(UpdateSpellSequence.class, messageNumber++).encoder(UpdateSpellSequence::encode).decoder(UpdateSpellSequence::new).consumer(UpdateSpellSequence::handle).add();
        channel.messageBuilder(SynchronizeSpellcastState.class, messageNumber++).encoder(SynchronizeSpellcastState::encode).decoder(SynchronizeSpellcastState::new).consumer(SynchronizeSpellcastState::handle).add();
        channel.messageBuilder(UpdateEssentializerAmounts.class, messageNumber++).encoder(UpdateEssentializerAmounts::encode).decoder(UpdateEssentializerAmounts::new).consumer(UpdateEssentializerAmounts::handle).add();
        channel.messageBuilder(UpdateEssentializerTileEntity.class, messageNumber++).encoder(UpdateEssentializerTileEntity::encode).decoder(UpdateEssentializerTileEntity::new).consumer(UpdateEssentializerTileEntity::handle).add();
        channel.messageBuilder(AddVelocityToPlayer.class, messageNumber++).encoder(AddVelocityToPlayer::encode).decoder(AddVelocityToPlayer::new).consumer(AddVelocityToPlayer::handle).add();
        channel.messageBuilder(SyncEssenceConversions.class, messageNumber++).encoder(SyncEssenceConversions::encode).decoder(SyncEssenceConversions::new).consumer(SyncEssenceConversions::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);

        MagicContainerCapability.register();
        SpellcastEntityData.register();
        //DiscoveryHandler.init();

        CraftingHelper.register(AnalyzedFilteringIngredient.ID, AnalyzedFilteringIngredient.Serializer.INSTANCE);
    }

    // TODO: TAGS
    /*public void init(FMLCommonSetupEvent event)
    {
        OreDictionary.registerOre("blockAgate", gemstoneBlock.getStack(GemstoneBlockType.Agate));
        OreDictionary.registerOre("blockAmethyst", gemstoneBlock.getStack(GemstoneBlockType.Amethyst));
        OreDictionary.registerOre("blockCitrine", gemstoneBlock.getStack(GemstoneBlockType.Citrine));
        OreDictionary.registerOre("blockRuby", gemstoneBlock.getStack(GemstoneBlockType.Ruby));
        OreDictionary.registerOre("blockSapphire", gemstoneBlock.getStack(GemstoneBlockType.Sapphire));
        OreDictionary.registerOre("blockSerendibite", gemstoneBlock.getStack(GemstoneBlockType.Serendibite));

        OreDictionary.registerOre("oreAgate", gemstoneOre.getStack(GemstoneBlockType.Agate));
        OreDictionary.registerOre("oreAmethyst", gemstoneOre.getStack(GemstoneBlockType.Amethyst));
        OreDictionary.registerOre("oreCitrine", gemstoneOre.getStack(GemstoneBlockType.Citrine));
        OreDictionary.registerOre("oreRuby", gemstoneOre.getStack(GemstoneBlockType.Ruby));
        OreDictionary.registerOre("oreSapphire", gemstoneOre.getStack(GemstoneBlockType.Sapphire));
        OreDictionary.registerOre("oreSerendibite", gemstoneOre.getStack(GemstoneBlockType.Serendibite));

        OreDictionary.registerOre("gemRuby", gemstone.getStack(Gemstone.Ruby));
        OreDictionary.registerOre("gemSapphire", gemstone.getStack(Gemstone.Sapphire));
        OreDictionary.registerOre("gemCitrine", gemstone.getStack(Gemstone.Citrine));
        OreDictionary.registerOre("gemAgate", gemstone.getStack(Gemstone.Agate));
        OreDictionary.registerOre("gemQuartz", gemstone.getStack(Gemstone.Quartz));
        OreDictionary.registerOre("gemSerendibite", gemstone.getStack(Gemstone.Serendibite));
        OreDictionary.registerOre("gemEmerald", gemstone.getStack(Gemstone.Emerald));
        OreDictionary.registerOre("gemAmethyst", gemstone.getStack(Gemstone.Amethyst));
        OreDictionary.registerOre("gemDiamond", gemstone.getStack(Gemstone.Diamond));

        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Ruby));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Sapphire));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Citrine));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Agate));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Quartz));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Serendibite));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Emerald));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Amethyst));
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Diamond));
    }*/

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        for(Biome biome : ForgeRegistries.BIOMES)
        {
            for(Gemstone g : Gemstone.values)
            {
                if (g.generateCustomOre())
                {
                    int numPerChunk = 1 + getBiomeBonus(g.getElement(),biome);
                    biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE
                                    .withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, g.getOre().getDefaultState(), 8))
                                    .func_227228_a_(Placement.COUNT_RANGE.func_227446_a_(new CountRangeConfig(numPerChunk, 0, 0, 16))));
                }
            }

            biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, CocoonFeature.INSTANCE.withConfiguration(NoFeatureConfig.NO_FEATURE_CONFIG)
                    .func_227228_a_(CocoonPlacement.INSTANCE.func_227446_a_(NoPlacementConfig.NO_PLACEMENT_CONFIG)));
        }
    }

    private int getBiomeBonus(@Nullable Element e, Biome biome)
    {
        if (e == null)
            return 1;
        switch(e)
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

    public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getPlayer().isServerWorld())
            channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)event.getPlayer()), new SyncEssenceConversions());
    }

    public void serverStarting(FMLServerAboutToStartEvent event)
    {
        event.getServer().getResourceManager().addReloadListener(
                new ReloadListener<Void>()
                 {
                     @Override
                     protected Void prepare(IResourceManager resourceManagerIn, IProfiler profilerIn)
                     {
                         return null;
                     }

                     @Override
                     protected void apply(Void splashList, IResourceManager resourceManagerIn, IProfiler profilerIn)
                     {
                         EssenceConversions.SERVER.clear();
                         StockConversions.addStockConversions();
                         EssenceOverrides.loadOverrides();
                         EssenceConversions.registerEssencesForRecipes();
                         channel.send(PacketDistributor.ALL.with(null), new SyncEssenceConversions());
                     }
                 }
        );
    }

    public void gatherData(GatherDataEvent event)
    {
        DataGenerator gen = event.getGenerator();

        if (event.includeServer())
        {
            gen.addProvider(new Recipes(gen));
            gen.addProvider(new LootTables(gen));
        }
        if (event.includeClient())
        {
            gen.addProvider(new BlockStates(gen, event));
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
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
            map.forEach((p_218436_2_, p_218436_3_) -> {
                LootTableManager.func_227508_a_(validationtracker, p_218436_2_, p_218436_3_);
            });
        }

        public static class BlockTables extends BlockLootTables
        {
            @Override
            protected void addTables()
            {
                for(Element e : Element.values)
                {
                    this.registerLootTable(e.getCocoon(), BlockTables::dropWithOrbs);
                }
                this.registerLootTable(ElementsOfPowerBlocks.ESSENTIALIZER, dropping(ElementsOfPowerBlocks.ESSENTIALIZER));
                for(Gemstone g : Gemstone.values)
                {
                    if(g.generateCustomBlock())
                        this.registerLootTable(g.getBlock(), dropping(g.getBlock()));
                    if(g.generateCustomOre())
                        this.registerLootTable(g.getOre(), (block) -> droppingItemWithFortune(block, g.getItem()));
                }
            }

            protected static LootTable.Builder dropWithOrbs(Block block) {
                LootTable.Builder builder = LootTable.builder();
                for(Element e : Element.values)
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
            densityBlock(block, densityProperty, (density) -> location("block/" +  Objects.requireNonNull(block.getRegistryName()).getPath() + "_" + density));
        }

        private void densityBlock(Block block, IntegerProperty densityProperty, Function<Integer, ResourceLocation> texMapper)
        {
            Map<Integer, ModelFile> densityModels = Maps.asMap(
                    new HashSet<>(densityProperty.getAllowedValues()),
                    density -> models().cubeAll(location(Objects.requireNonNull(block.getRegistryName()).getPath() + "_" + density).getPath(), texMapper.apply(density)));

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

            CustomRecipeBuilder.func_218656_a(ContainerChargeRecipe.SERIALIZER).build(consumer, location("gemstone_change").toString());
            CustomRecipeBuilder.func_218656_a(GemstoneChangeRecipe.SERIALIZER).build(consumer, location("container_charge").toString());

            for (Gemstone gemstone : Gemstone.values())
            {
                if (gemstone.generateCustomOre())
                {
                    CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(gemstone.getOre()), gemstone, 1.0F, 200)
                            .addCriterion("has_ore", hasItem(gemstone.getOre()))
                            .build(consumer, location("smelting/" + gemstone.getName()));
                }
                if(gemstone.generateCustomBlock())
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
                            .build(consumer, location(gemstone.getName() + "_from_block"));
                }
            }
        }
    }
}
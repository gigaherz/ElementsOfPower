package gigaherz.elementsofpower;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
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
import gigaherz.elementsofpower.database.ConversionCache;
import gigaherz.elementsofpower.database.InternalConversionProcess;
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
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.SlotTypeMessage;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public static String fixDescription(String description)
    {
        return description.endsWith(":NOFML\uFFFDr") ? description.substring(0, description.length() - 8)+ "\uFFFDr" : description;
    }

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

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, ConfigManager.COMMON_SPEC);
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new EssentializerBlock(Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL).setLightLevel(b -> 1)).setRegistryName("essentializer"),
                new DustBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.DUST).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s, w, p) -> true).setBlocksVision((s, w, p) -> false)
                        .hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("dust"),
                new MistBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.MIST).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s, w, p) -> false).setBlocksVision((s, w, p) -> false)
                        .hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("mist"),
                new LightBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.LIGHT).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s, w, p) -> false).setBlocksVision((s, w, p) -> false)
                        .hardnessAndResistance(15.0F).setLightLevel(b -> 15).sound(SoundType.METAL)).setRegistryName("light"),
                new CushionBlock(Block.Properties.create(ElementsOfPowerBlocks.BlockMaterials.CUSHION).noDrops().doesNotBlockMovement().notSolid()
                        .setSuffocates((s, w, p) -> false).setBlocksVision((s, w, p) -> false)
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
        Arrays.stream(Element.values).forEach(e ->
                RenderTypeLookup.setRenderLayer(e.getCocoon(), layer -> layer == RenderType.getTranslucent() || layer == RenderType.getSolid())
        );

        MinecraftForge.EVENT_BUS.register(new WandUseManager());
        MinecraftForge.EVENT_BUS.register(new MagicContainerOverlay());

        WandUseManager.instance.initialize();
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        int messageNumber = 0;
        CHANNEL.messageBuilder(UpdateSpellSequence.class, messageNumber++, NetworkDirection.PLAY_TO_SERVER).encoder(UpdateSpellSequence::encode).decoder(UpdateSpellSequence::new).consumer(UpdateSpellSequence::handle).add();
        CHANNEL.messageBuilder(SynchronizeSpellcastState.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SynchronizeSpellcastState::encode).decoder(SynchronizeSpellcastState::new).consumer(SynchronizeSpellcastState::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerAmounts.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerAmounts::encode).decoder(UpdateEssentializerAmounts::new).consumer(UpdateEssentializerAmounts::handle).add();
        CHANNEL.messageBuilder(UpdateEssentializerTileEntity.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(UpdateEssentializerTileEntity::encode).decoder(UpdateEssentializerTileEntity::new).consumer(UpdateEssentializerTileEntity::handle).add();
        CHANNEL.messageBuilder(AddVelocityToPlayer.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(AddVelocityToPlayer::encode).decoder(AddVelocityToPlayer::new).consumer(AddVelocityToPlayer::handle).add();
        CHANNEL.messageBuilder(SyncEssenceConversions.class, messageNumber++, NetworkDirection.PLAY_TO_CLIENT).encoder(SyncEssenceConversions::encode).decoder(SyncEssenceConversions::new).consumer(SyncEssenceConversions::handle).add();
        LOGGER.debug("Final message number: " + messageNumber);

        MagicContainerCapability.register();
        PlayerCombinedMagicContainers.register();
        SpellcastEntityData.register();
        //DiscoveryHandler.init();
        InternalConversionProcess.init();

        CraftingHelper.register(AnalyzedFilteringIngredient.ID, AnalyzedFilteringIngredient.Serializer.INSTANCE);

        GlobalEntityTypeAttributes.put(EssenceEntity.TYPE, EssenceEntity.prepareAttributes().create());

        CocoonEventHandling.enable();
    }

    private void addStuffToBiomes(BiomeLoadingEvent event)
    {
        RegistryKey<Biome> biome = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, event.getName());
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
                        event.getGeneration().withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Feature.ORE
                                .withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, g.getOre().getDefaultState(), numPerVein))
                                .range(16).square());
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

            event.getGeneration().withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, CocoonFeature.INSTANCE.withConfiguration(cfg)
                    .withPlacement(CocoonPlacement.INSTANCE.configure(NoPlacementConfig.NO_PLACEMENT_CONFIG)));
        }
    }

    private int getBiomeBonus(@Nullable Element e, RegistryKey<Biome> biome)
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

    public void registerCommands(RegisterCommandsEvent event)
    {
        LiteralArgumentBuilder<CommandSource> cmd = Commands.literal("elementsofpower")
                .then(Commands.literal("dumpMissingItems")
                        .requires(cs -> cs.hasPermissionLevel(4)) //permission
                        .executes(ctx -> {
                                    ConversionCache.dumpItemsWithoutEssences(ctx.getSource().getWorld());
                                    ctx.getSource().sendFeedback(new StringTextComponent("Missing essences list dumped to disk."), true);
                                    return 0;
                                }
                        )
                )
                .then(Commands.literal("dumpEssences")
                        .requires(cs -> cs.hasPermissionLevel(4)) //permission
                        .executes(ctx -> {
                                    ConversionCache.dumpEssences(ctx.getSource().getWorld());
                                    ctx.getSource().sendFeedback(new StringTextComponent("Missing essences list dumped to disk."), true);
                                    return 0;
                                }
                        )
                );
        cmd = InternalConversionProcess.registerSubcommands(cmd);
        event.getDispatcher().register(cmd);
    }

    public static boolean isInternalRecipeScannerEnabled()
    {
        // FIXME: Make it work not slow.
        return false; // ConfigManager.COMMON.disableAequivaleoSupport.get() || !ModList.get().isLoaded("aequivaleo");
    }

    public void gatherData(GatherDataEvent event)
    {
        ElementsofPowerDataGen.gatherData(event);
    }
}

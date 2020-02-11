package gigaherz.elementsofpower;

import gigaherz.elementsofpower.analyzer.ItemAnalyzer;
import gigaherz.elementsofpower.analyzer.gui.ContainerAnalyzer;
import gigaherz.elementsofpower.analyzer.gui.GuiAnalyzer;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.client.ClientProxy;
import gigaherz.elementsofpower.client.WandUseManager;
import gigaherz.elementsofpower.client.renderers.MagicContainerOverlay;
import gigaherz.elementsofpower.client.renderers.RenderBall;
import gigaherz.elementsofpower.client.renderers.RenderEssence;
import gigaherz.elementsofpower.cocoons.BlockCocoon;
import gigaherz.elementsofpower.cocoons.TileCocoon;
import gigaherz.elementsofpower.common.IModProxy;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.EssenceOverrides;
import gigaherz.elementsofpower.database.StockConversions;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.essentializer.BlockEssentializer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.essentializer.gui.ContainerEssentializer;
import gigaherz.elementsofpower.essentializer.gui.GuiEssentializer;
import gigaherz.elementsofpower.gemstones.*;
import gigaherz.elementsofpower.items.*;
import gigaherz.elementsofpower.network.*;
import gigaherz.elementsofpower.spelldust.ItemSpelldust;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.spells.blocks.BlockCushion;
import gigaherz.elementsofpower.spells.blocks.BlockDust;
import gigaherz.elementsofpower.spells.blocks.BlockLight;
import gigaherz.elementsofpower.spells.blocks.BlockMist;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.data.CookingRecipeBuilder;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.attribute.AclEntry;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.function.Consumer;

//import gigaherz.elementsofpower.progression.DiscoveryHandler;

@Mod.EventBusSubscriber
@Mod(ElementsOfPowerMod.MODID)
public class ElementsOfPowerMod
{
    public static final String MODID = "elementsofpower";

    public static final String CHANNEL = "ElementsOfPower";

    public static ElementsOfPowerMod instance;

    public static IModProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new IModProxy(){});

    // Block templates
    @ObjectHolder("elementsofpower:essentializer")
    public static Block essentializer;
    @ObjectHolder("elementsofpower:dust")
    public static Block dust;
    @ObjectHolder("elementsofpower:mist")
    public static Block mist;
    @ObjectHolder("elementsofpower:light")
    public static Block light;
    @ObjectHolder("elementsofpower:cushion")
    public static Block cushion;
    @ObjectHolder("elementsofpower:fire_cocoon")
    public static Block fireCocoon;
    @ObjectHolder("elementsofpower:water_cocoon")
    public static Block waterCocoon;
    @ObjectHolder("elementsofpower:air_cocoon")
    public static Block airCocoon;
    @ObjectHolder("elementsofpower:earth_cocoon")
    public static Block earthCocoon;
    @ObjectHolder("elementsofpower:light_cocoon")
    public static Block lightCocoon;
    @ObjectHolder("elementsofpower:darkness_cocoon")
    public static Block darknessCocoon;
    @ObjectHolder("elementsofpower:life_cocoon")
    public static Block lifeCocoon;
    @ObjectHolder("elementsofpower:death_cocoon")
    public static Block deathCocoon;
    @ObjectHolder("elementsofpower:ruby_ore")
    public static BlockGemstone rubyOre;
    @ObjectHolder("elementsofpower:ruby_block")
    public static BlockGemstone rubyBlock;
    @ObjectHolder("elementsofpower:sapphire_ore")
    public static BlockGemstone sapphireOre;
    @ObjectHolder("elementsofpower:sapphire_block")
    public static BlockGemstone sapphireBlock;
    @ObjectHolder("elementsofpower:citrine_ore")
    public static BlockGemstone citrineOre;
    @ObjectHolder("elementsofpower:citrine_block")
    public static BlockGemstone citrineBlock;
    @ObjectHolder("elementsofpower:agate_ore")
    public static BlockGemstone agateOre;
    @ObjectHolder("elementsofpower:agate_block")
    public static BlockGemstone agateBlock;
    @ObjectHolder("elementsofpower:serendibite_ore")
    public static BlockGemstone serendibiteOre;
    @ObjectHolder("elementsofpower:serendibite_block")
    public static BlockGemstone serendibiteBlock;
    @ObjectHolder("elementsofpower:amethyst_ore")
    public static BlockGemstone amethystOre;
    @ObjectHolder("elementsofpower:amethyst_block")
    public static BlockGemstone amethystBlock;

    // Block Materials
    public static Material materialCushion = (new Material.Builder(MaterialColor.BLACK)).doesNotBlockMovement().notOpaque().notSolid().replaceable().pushDestroys().build();

    // Item templates
    @ObjectHolder("elementsofpower:orb")
    public static ItemMagicOrb orb;
    @ObjectHolder("elementsofpower:wand")
    public static ItemWand wand;
    @ObjectHolder("elementsofpower:staff")
    public static ItemWand staff;
    @ObjectHolder("elementsofpower:ring")
    public static ItemBauble ring;
    @ObjectHolder("elementsofpower:headband")
    public static ItemBauble headband;
    @ObjectHolder("elementsofpower:necklace")
    public static ItemBauble necklace;
    @ObjectHolder("elementsofpower:analyzer")
    public static ItemAnalyzer analyzer;
    @ObjectHolder("elementsofpower:spelldust")
    public static ItemSpelldust spelldust;
    @ObjectHolder("elementsofpower:ruby")
    public static ItemGemstone ruby;
    @ObjectHolder("elementsofpower:sapphire")
    public static ItemGemstone sapphire;
    @ObjectHolder("elementsofpower:citrine")
    public static ItemGemstone citrine;
    @ObjectHolder("elementsofpower:agate")
    public static ItemGemstone agate;
    @ObjectHolder("elementsofpower:quartz")
    public static ItemGemstone quartz;
    @ObjectHolder("elementsofpower:serendibite")
    public static ItemGemstone serendibite;
    @ObjectHolder("elementsofpower:emerald")
    public static ItemGemstone emerald;
    @ObjectHolder("elementsofpower:amethyst")
    public static ItemGemstone amethyst;
    @ObjectHolder("elementsofpower:diamond")
    public static ItemGemstone diamond;
    @ObjectHolder("elementsofpower:creativite")
    public static ItemGemstone creativite;
    @ObjectHolder("elementsofpower:unknown_ruby")
    public static ItemGemstone unknown_ruby;
    @ObjectHolder("elementsofpower:unknown_sapphire")
    public static ItemGemstone unknown_sapphire;
    @ObjectHolder("elementsofpower:unknown_citrine")
    public static ItemGemstone unknown_citrine;
    @ObjectHolder("elementsofpower:unknown_agate")
    public static ItemGemstone unknown_agate;
    @ObjectHolder("elementsofpower:unknown_serendibite")
    public static ItemGemstone unknown_serendibite;
    @ObjectHolder("elementsofpower:unknown_amethyst")
    public static ItemGemstone unknown_amethyst;
    @ObjectHolder("elementsofpower:unknown_creativite")
    public static ItemGemstone unknown_creativite;
    @ObjectHolder("elementsofpower:fire_cocoon")
    public static Item fireCocoonItem;
    @ObjectHolder("elementsofpower:water_cocoon")
    public static Item waterCocoonItem;
    @ObjectHolder("elementsofpower:air_cocoon")
    public static Item airCocoonItem;
    @ObjectHolder("elementsofpower:earth_cocoon")
    public static Item earthCocoonItem;
    @ObjectHolder("elementsofpower:light_cocoon")
    public static Item lightCocoonItem;
    @ObjectHolder("elementsofpower:darkness_cocoon")
    public static Item darknessCocoonItem;
    @ObjectHolder("elementsofpower:life_cocoon")
    public static Item lifeCocoonItem;
    @ObjectHolder("elementsofpower:death_cocoon")
    public static Item deathCocoonItem;

    //@ItemStackHolder(value = "gbook:guidebook", nbt = "{Book:\"" + "elementsofpowerxml/guidebook.xml\"}")
    public ItemStack guidebookStack;

    // To be used only during loading.
    private final Lazy<EntityType<EntityBall>> spellBallInit = Lazy.of(() -> EntityType.Builder.<EntityBall>create(EntityBall::new, EntityClassification.MISC)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).build(location("spell_ball").toString()));
    private final Lazy<EntityType<EntityEssence>> essenceInit = Lazy.of(() -> EntityType.Builder.<EntityEssence>create(EntityEssence::new, EntityClassification.AMBIENT)
            .setTrackingRange(80).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true).build(location("essence").toString()));

    // Handlers
    private static final String PROTOCOL_VERSION = "1.0";
    public static SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(location("general"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static Logger logger;

    public final static Format prettyNumberFormatter = new DecimalFormat("#.#");
    public final static Format prettyNumberFormatter2 = new DecimalFormat("#0.0");
    private String overrides;

    public static ItemGroup tabMagic = new ItemGroup(MODID)
    {
        @Override
        public ItemStack createIcon()
        {
            return wand.getStack(Gemstone.Diamond, Quality.Common);
        }
    };

    public static ItemGroup tabGemstones = new ItemGroup(MODID)
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(Gemstone.Ruby);
        }
    };

    public ElementsOfPowerMod()
    {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(Block.class, this::registerBlocks);
        modEventBus.addGenericListener(Item.class, this::registerItems);

        modEventBus.addListener(this::clientSetup);

    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                new BlockEssentializer(Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL).lightValue(1)).setRegistryName("essentializer"),
                new BlockDust(Block.Properties.create(Material.CLAY).hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("dust"),
                new BlockMist(Block.Properties.create(Material.SNOW).hardnessAndResistance(0.1F).sound(SoundType.CLOTH).variableOpacity()).setRegistryName("mist"),
                new BlockLight(Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL)).setRegistryName("light"),
                new BlockCushion(Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL)).setRegistryName("cushion")
        );
        for(Gemstone type : Gemstone.values())
        {
            event.getRegistry().registerAll(
                    new BlockGemstone(type, Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F).sound(SoundType.METAL)).setRegistryName(type.getName() + "_block"),
                    new BlockGemstone(type, Block.Properties.create(Material.IRON).hardnessAndResistance(15.0F).sound(SoundType.METAL)).setRegistryName(type.getName() + "_ore")
            );
        }
        for(Element type : Element.values())
        {
            event.getRegistry().registerAll(
                    new BlockCocoon(type, Block.Properties.create(Material.IRON).hardnessAndResistance(1F).sound(SoundType.METAL).lightValue(5).tickRandomly()).setRegistryName(type.getName() + "_cocoon")
            );
        }
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                new BlockItem(essentializer, new Item.Properties().group(tabMagic)).setRegistryName(essentializer.getRegistryName()),

                new ItemWand(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("wand"),
                new ItemStaff(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("staff"),
                new ItemBauble(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("ring"),
                new ItemBauble(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("headband"),
                new ItemBauble(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("necklace"),
                new ItemAnalyzer(new Item.Properties().group(tabMagic).maxStackSize(1)).setRegistryName("analyzer"),
                new ItemSpelldust(new Item.Properties().group(tabMagic)).setRegistryName("spelldust"),

                new SpawnEggItem(essenceInit.get(), 0x0000FF, 0xFFFF00, new Item.Properties().group(tabMagic)).setRegistryName("essence")
        );
        for(Gemstone type : Gemstone.values())
        {
            event.getRegistry().register(new ItemGemstone(type, new Item.Properties().group(tabGemstones).maxStackSize(1)).setRegistryName(type.getName()));
            if (type.generateCustomUnexamined())
                event.getRegistry().register(new ItemGemstone(type, new Item.Properties().group(tabGemstones)).setRegistryName("unknown_" + type.getName()));
            if (type.generateCustomBlock())
                event.getRegistry().register(new BlockItem(type.getBlock(), new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_block"));
            if (type.generateCustomOre())
                event.getRegistry().register(new BlockItem(type.getOre(), new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_ore"));
        }
        for(Element type : Element.values())
        {
            event.getRegistry().registerAll(
                    new ItemMagicOrb(type, new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_orb"),
                    new BlockItem(type.getBlock(), new Item.Properties().group(tabMagic)).setRegistryName(type.getName() + "_cocoon")
            );
        }
    }

    public void registerEntities(RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry().registerAll(
            spellBallInit.get(), essenceInit.get()
        );
    }

    public void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event)
    {
        event.getRegistry().registerAll(
                TileEntityType.Builder.create(TileEssentializer::new, essentializer).build(null).setRegistryName("essentializer"),
                TileEntityType.Builder.create(TileCocoon::new,
                        Arrays.stream(Element.values()).map(Element::getBlock).toArray(Block[]::new)
                        ).build(null).setRegistryName("cocoon")
        );
    }

    public void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event)
    {
        event.getRegistry().registerAll(
                new ContainerType<>(ContainerEssentializer::new).setRegistryName("essentializer"),
                IForgeContainerType.create(ContainerAnalyzer::new).setRegistryName("analyzer")
        );
    }

    public static void commonSetup(FMLCommonSetupEvent event)
    {

    }

    public void clientSetup(FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityEssence.TYPE, RenderEssence::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBall.TYPE, RenderBall::new);

        ScreenManager.registerFactory(ContainerAnalyzer.TYPE, GuiAnalyzer::new);
        ScreenManager.registerFactory(ContainerEssentializer.TYPE, GuiEssentializer::new);

        RenderTypeLookup.setRenderLayer(dust, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(mist, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(cushion, RenderType.translucent());
        RenderTypeLookup.setRenderLayer(light, RenderType.translucent());

        MinecraftForge.EVENT_BUS.register(new WandUseManager());
        MinecraftForge.EVENT_BUS.register(new MagicContainerOverlay());

        WandUseManager.instance.initialize();
    }

    public static void registerBook(InterModEnqueueEvent event)
    {
        InterModComms.sendTo("gbook", "registerBook", () -> ElementsOfPowerMod.location("xml/guidebook.xml"));
    }

    public static void registerRecipes(GatherDataEvent event)
    {
        new RecipeProvider(event.getGenerator()){
            @Override
            protected void registerRecipes(Consumer<IFinishedRecipe> consumer)
            {
                for(Gemstone gemstone : Gemstone.values())
                    CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(gemstone.getOre()), gemstone, 1.0F, 200).build(consumer);
            }
        };
    }

    public void preInit(FMLCommonSetupEvent event)
    {
        //ConfigManager.init(event.getSuggestedConfigurationFile());

        overrides = FMLPaths.CONFIGDIR + File.separator + "elementsofpower_essences.json";

        CapabilityMagicContainer.register();

        registerNetwork();

        SpellcastEntityData.register();
        //DiscoveryHandler.init();
    }

    public void init(FMLCommonSetupEvent event)
    {
        /*OreDictionary.registerOre("blockAgate", gemstoneBlock.getStack(GemstoneBlockType.Agate));
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
        OreDictionary.registerOre("magicGemstone", gemstone.getStack(Gemstone.Diamond));*/

        registerWorldGenerators();

        StockConversions.registerEssenceSources();
        EssenceOverrides.loadOverrides();

        proxy.init();
    }

    public void postInit(FMLLoadCompleteEvent event)
    {
        EssenceConversions.registerEssencesForRecipes();
    }

    private void registerNetwork()
    {
        int messageNumber = 0;
        channel.messageBuilder(SpellSequenceUpdate.class, messageNumber++).encoder(SpellSequenceUpdate::encode).decoder(SpellSequenceUpdate::new).consumer(SpellSequenceUpdate::handle);
        channel.messageBuilder(SpellcastSync.class, messageNumber++).encoder(SpellcastSync::encode).decoder(SpellcastSync::new).consumer(SpellcastSync::handle);
        channel.messageBuilder(EssentializerAmountsUpdate.class, messageNumber++).encoder(EssentializerAmountsUpdate::encode).decoder(EssentializerAmountsUpdate::new).consumer(EssentializerAmountsUpdate::handle);
        channel.messageBuilder(EssentializerTileUpdate.class, messageNumber++).encoder(EssentializerTileUpdate::encode).decoder(EssentializerTileUpdate::new).consumer(EssentializerTileUpdate::handle);
        channel.messageBuilder(AddVelocityPlayer.class, messageNumber++).encoder(AddVelocityPlayer::encode).decoder(AddVelocityPlayer::new).consumer(AddVelocityPlayer::handle);
        logger.debug("Final message number: " + messageNumber);
    }

    private void registerWorldGenerators()
    {
        /*if (ConfigManager.EnableGemstoneOregen)
            GameRegistry.registerWorldGenerator(new BlockGemstoneOre.Generator(), 1);
        if (ConfigManager.EnableCocoonGeneration)
            GameRegistry.registerWorldGenerator(new BlockCocoon.Generator(), 1);*/
    }

    private static ItemStack copyStack(ItemStack original, int quantity)
    {
        ItemStack copy = original.copy();
        copy.setCount(quantity);
        return copy;
    }

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }
}

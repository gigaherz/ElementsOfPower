package gigaherz.elementsofpower;

import gigaherz.common.BlockRegistered;
import gigaherz.common.RenamingHelper;
import gigaherz.elementsofpower.analyzer.ItemAnalyzer;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.cocoons.BlockCocoon;
import gigaherz.elementsofpower.cocoons.TileCocoon;
import gigaherz.elementsofpower.common.GuiHandler;
import gigaherz.elementsofpower.common.IModProxy;
import gigaherz.elementsofpower.common.MaterialCushion;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.EssenceOverrides;
import gigaherz.elementsofpower.database.StockConversions;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.essentializer.BlockEssentializer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.gemstones.*;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import gigaherz.elementsofpower.items.ItemRing;
import gigaherz.elementsofpower.items.ItemStaff;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.network.*;
import gigaherz.elementsofpower.progression.DiscoveryHandler;
import gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import gigaherz.elementsofpower.spelldust.ItemSpelldust;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.spells.blocks.BlockCushion;
import gigaherz.elementsofpower.spells.blocks.BlockDust;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;

@Mod.EventBusSubscriber
@Mod(modid = ElementsOfPower.MODID,
        name = ElementsOfPower.MODNAME, version = ElementsOfPower.VERSION,
        acceptedMinecraftVersions = "[1.11.0,1.12.0)",
        updateJSON = "https://raw.githubusercontent.com/gigaherz/ElementsOfPower/master/update.json")
public class ElementsOfPower
{
    public static final String MODID = "elementsofpower";
    public static final String MODNAME = "Elements Of Power";
    public static final String VERSION = "@VERSION@";

    public static final String CHANNEL = "ElementsOfPower";

    // The instance of your mod that Forge uses.
    @Mod.Instance(ElementsOfPower.MODID)
    public static ElementsOfPower instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "gigaherz.elementsofpower.client.ClientProxy", serverSide = "gigaherz.elementsofpower.server.ServerProxy")
    public static IModProxy proxy;

    // Block templates
    public static BlockRegistered essentializer;
    public static BlockRegistered dust;
    public static BlockRegistered mist;
    public static BlockRegistered cushion;
    public static BlockRegistered cocoon;

    public static BlockGemstoneOre gemstoneOre;
    public static BlockGemstone gemstoneBlock;

    // Block Materials
    public static Material materialCushion = new MaterialCushion(MapColor.BLACK);

    // Item templates
    public static ItemMagicOrb magicOrb;

    public static ItemWand magicWand;
    public static ItemWand magicStaff;
    public static ItemRing magicRing;

    public static ItemGemstone gemstone;

    public static ItemAnalyzer analyzer;

    public static ItemSpelldust spelldust;

    @GameRegistry.ItemStackHolder(value = "gbook:guidebook", nbt = "{Book:\"" + MODID + ":xml/guidebook.xml\"}")
    public ItemStack guidebookStack;

    // Handlers
    public static SimpleNetworkWrapper channel;

    public static GuiHandler guiHandler = new GuiHandler();

    public static Logger logger;
    public static Configuration config;
    public static String overrides;

    public final static Format prettyNumberFormatter = new DecimalFormat("#.#");
    public final static Format prettyNumberFormatter2 = new DecimalFormat("#0.0");

    private static RenamingHelper helper = new RenamingHelper();

    public static CreativeTabs tabMagic = new CreativeTabs(MODID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return magicWand.getStack(Gemstone.Diamond, Quality.Common);
        }
    };

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(
                essentializer = new BlockEssentializer("essentializer"),
                dust = new BlockDust("dust"),
                mist = new BlockDust("mist"),
                cushion = new BlockCushion("cushion"),
                cocoon = new BlockCocoon("cocoon"),
                gemstoneBlock = new BlockGemstone("gemstone_block"),
                gemstoneOre = new BlockGemstoneOre("gemstone_ore")
        );
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                essentializer.createItemBlock(),
                cocoon.createItemBlock(),
                gemstoneBlock.createItemBlock(),
                gemstoneOre.createItemBlock(),

                magicOrb = new ItemMagicOrb("magic_orb"),
                magicWand = new ItemWand("magic_wand"),
                magicStaff = new ItemStaff("magic_staff"),
                magicRing = new ItemRing("magic_ring"),
                gemstone = new ItemGemstone("gemstone"),
                analyzer = new ItemAnalyzer("analyzer"),
                spelldust = new ItemSpelldust("spelldust")
        );
    }

    @Mod.EventHandler()
    public void conflictResolver(FMLMissingMappingsEvent event)
    {
        helper.process(event);
    }

    private void registerTileEntities()
    {
        GameRegistry.registerTileEntityWithAlternatives(TileEssentializer.class, essentializer.getRegistryName().toString(), "essentializerTile");
        GameRegistry.registerTileEntityWithAlternatives(TileCocoon.class, cocoon.getRegistryName().toString(), "cocoonTile");
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        ConfigManager.init(event.getSuggestedConfigurationFile());

        overrides = event.getModConfigurationDirectory() + File.separator + "elementsofpower_essences.json";

        helper.addAlternativeName(gemstoneBlock, location("gemstoneBlock"));
        helper.addAlternativeName(gemstoneOre, location("gemstoneOre"));
        helper.addAlternativeName(magicOrb, location("magicOrb"));
        helper.addAlternativeName(magicWand, location("magicWand"));
        helper.addAlternativeName(magicStaff, location("magicStaff"));
        helper.addAlternativeName(magicRing, location("magicRing"));

        CapabilityMagicContainer.register();

        registerTileEntities();

        registerOreDictionaryNames();

        registerNetwork();

        logger.info("Registering extended entity properties...");

        SpellcastEntityData.register();
        DiscoveryHandler.init();

        logger.info("Performing pre-initialization proxy tasks...");

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("Performing initialization proxy tasks...");

        proxy.init();

        registerEntities();

        registerWorldGenerators();

        registerRecipes();

        // Gui
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        StockConversions.registerEssenceSources();
        EssenceOverrides.loadOverrides();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        EssenceConversions.registerEssencesForRecipes();
    }

    private void registerNetwork()
    {
        logger.info("Registering network channel...");

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SpellSequenceUpdate.Handler.class, SpellSequenceUpdate.class, messageNumber++, Side.SERVER);
        channel.registerMessage(SpellcastSync.Handler.class, SpellcastSync.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(EssentializerAmountsUpdate.Handler.class, EssentializerAmountsUpdate.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(EssentializerTileUpdate.Handler.class, EssentializerTileUpdate.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(AddVelocityPlayer.Handler.class, AddVelocityPlayer.class, messageNumber++, Side.CLIENT);
        logger.debug("Final message number: " + messageNumber);
    }

    private void registerOreDictionaryNames()
    {
        logger.info("Registering ore dictionary names...");

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
    }

    private void registerEntities()
    {
        // Entities
        logger.info("Registering entities...");

        int entityId = 1;
        EntityRegistry.registerModEntity(location("spell_ball"), EntityBall.class, "SpellBall", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(location("essence"), EntityEssence.class, "Essence", entityId++, this, 80, 3, true, 0x0000FF, 0xFFFF00);
        logger.debug("Next entity id: " + entityId);
    }

    private void registerWorldGenerators()
    {
        // Worldgen
        if (ConfigManager.EnableGemstoneOregen)
            GameRegistry.registerWorldGenerator(new BlockGemstoneOre.Generator(), 1);
        if (ConfigManager.EnableCocoonGeneration)
            GameRegistry.registerWorldGenerator(new BlockCocoon.Generator(), 1);
    }

    private void registerRecipes()
    {
        // Recipes
        logger.info("Registering recipes...");

        GameRegistry.addRecipe(new ShapedOreRecipe(gemstoneBlock.getStack(GemstoneBlockType.Agate), "aaa", "aaa", "aaa", 'a', "gemAgate"));
        GameRegistry.addRecipe(new ShapedOreRecipe(gemstoneBlock.getStack(GemstoneBlockType.Amethyst), "aaa", "aaa", "aaa", 'a', "gemAmethyst"));
        GameRegistry.addRecipe(new ShapedOreRecipe(gemstoneBlock.getStack(GemstoneBlockType.Citrine), "aaa", "aaa", "aaa", 'a', "gemCitrine"));
        GameRegistry.addRecipe(new ShapedOreRecipe(gemstoneBlock.getStack(GemstoneBlockType.Ruby), "aaa", "aaa", "aaa", 'a', "gemRuby"));
        GameRegistry.addRecipe(new ShapedOreRecipe(gemstoneBlock.getStack(GemstoneBlockType.Sapphire), "aaa", "aaa", "aaa", 'a', "gemSapphire"));
        GameRegistry.addRecipe(new ShapedOreRecipe(gemstoneBlock.getStack(GemstoneBlockType.Serendibite), "aaa", "aaa", "aaa", 'a', "gemSerendibite"));

        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemstone.getStack(Gemstone.Agate), 9), "blockAgate"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemstone.getStack(Gemstone.Amethyst), 9), "blockAmethyst"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemstone.getStack(Gemstone.Citrine), 9), "blockCitrine"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemstone.getStack(Gemstone.Ruby), 9), "blockRuby"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemstone.getStack(Gemstone.Sapphire), 9), "blockSapphire"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemstone.getStack(Gemstone.Serendibite), 9), "blockSerendibite"));

        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Agate), gemstone.getStack(Gemstone.Agate), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Amethyst), gemstone.getStack(Gemstone.Amethyst), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Citrine), gemstone.getStack(Gemstone.Citrine), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Ruby), gemstone.getStack(Gemstone.Ruby), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Sapphire), gemstone.getStack(Gemstone.Sapphire), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Serendibite), gemstone.getStack(Gemstone.Serendibite), 0);

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(analyzer),
                "glg",
                "i  ",
                "psp",
                'g', "ingotGold",
                'l', "paneGlass",
                'i', "ingotIron",
                's', "slabWood",
                'p', "plankWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(essentializer, 1),
                "IQI",
                "ONO",
                "IOI",
                'I', Items.IRON_INGOT,
                'O', Blocks.OBSIDIAN,
                'Q', "magicGemstone",
                'N', Items.NETHER_STAR));
        GameRegistry.addShapedRecipe(new ItemStack(magicWand),
                " G",
                "S ",
                'G', Items.GOLD_INGOT,
                'S', Items.STICK);
        GameRegistry.addRecipe(new ItemStack(magicStaff),
                " GW",
                " SG",
                "S  ",
                'W', new ItemStack(magicWand, 1, OreDictionary.WILDCARD_VALUE),
                'G', Items.GOLD_INGOT,
                'S', Items.STICK);
        GameRegistry.addRecipe(new ItemStack(magicRing),
                " GG",
                "G G",
                " G ",
                'G', Items.GOLD_INGOT);

        if (guidebookStack != null)
            GameRegistry.addShapelessRecipe(guidebookStack, Items.BOOK, new ItemStack(magicOrb, 1, OreDictionary.WILDCARD_VALUE));

        GameRegistry.addRecipe(new GemstoneChangeRecipe());
        GameRegistry.addRecipe(new ContainerChargeRecipe());

        RecipeSorter.register("gemstoneChangeRecipe", GemstoneChangeRecipe.class, RecipeSorter.Category.SHAPELESS, "");
        RecipeSorter.register("containerChargeRecipe", ContainerChargeRecipe.class, RecipeSorter.Category.SHAPELESS, "");
    }

    private ItemStack copyStack(ItemStack original, int quantity)
    {
        ItemStack copy = original.copy();
        copy.setCount(quantity);
        return copy;
    }

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }

    public void test()
    {
        ItemStack stack = null;
    }
}

package gigaherz.elementsofpower;

import gigaherz.elementsofpower.analyzer.ItemAnalyzer;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.cocoons.BlockCocoon;
import gigaherz.elementsofpower.cocoons.TileCocoon;
import gigaherz.elementsofpower.common.BlockRegistered;
import gigaherz.elementsofpower.common.GuiHandler;
import gigaherz.elementsofpower.common.ISideProxy;
import gigaherz.elementsofpower.common.MaterialCushion;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.EssenceOverrides;
import gigaherz.elementsofpower.database.StockConversions;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.essentializer.BlockEssentializer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.gemstones.*;
import gigaherz.elementsofpower.guidebook.ItemGuidebook;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import gigaherz.elementsofpower.items.ItemRing;
import gigaherz.elementsofpower.items.ItemStaff;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.network.*;
import gigaherz.elementsofpower.progression.DiscoveryHandler;
import gigaherz.elementsofpower.recipes.ContainerChargeRecipe;
import gigaherz.elementsofpower.recipes.GemstoneChangeRecipe;
import gigaherz.elementsofpower.spelldust.BlockSpelldust;
import gigaherz.elementsofpower.spelldust.ItemSpelldust;
import gigaherz.elementsofpower.spells.blocks.BlockCushion;
import gigaherz.elementsofpower.spells.blocks.BlockDust;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

@Mod(modid = ElementsOfPower.MODID,
        name = ElementsOfPower.MODNAME, version = ElementsOfPower.VERSION,
        dependencies = "required-after:Forge@[12.16.0.1825,)",
        acceptedMinecraftVersions = "[1.9.4,1.11.0)",
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
    public static ISideProxy proxy;

    // Block templates
    public static BlockRegistered essentializer;
    public static BlockRegistered dust;
    public static BlockRegistered mist;
    public static BlockRegistered cushion;
    public static BlockRegistered cocoon;
    public static BlockRegistered spell_wire;

    public static BlockGemstoneOre gemstoneOre;
    public static BlockGemstone gemstoneBlock;

    // Block Materials
    public static Material materialCushion;

    // Item templates
    public static ItemMagicOrb magicOrb;

    public static ItemWand magicWand;
    public static ItemWand magicStaff;
    public static ItemRing magicRing;

    public static ItemGemstone gemstone;

    public static ItemAnalyzer analyzer;

    public static ItemGuidebook guidebook;

    public static ItemSpelldust spelldust;

    // Subitems
    public static ItemStack fire;
    public static ItemStack water;
    public static ItemStack air;
    public static ItemStack earth;

    public static ItemStack light;
    public static ItemStack darkness;
    public static ItemStack life;
    public static ItemStack death;

    public static ItemStack gemRuby;
    public static ItemStack gemSapphire;
    public static ItemStack gemCitrine;
    public static ItemStack gemAgate;
    public static ItemStack gemQuartz;
    public static ItemStack gemSerendibite;
    public static ItemStack gemEmerald;
    public static ItemStack gemAmethyst;
    public static ItemStack gemDiamond;

    public static ItemStack blockAgate;
    public static ItemStack blockAmethyst;
    public static ItemStack blockCitrine;
    public static ItemStack blockRuby;
    public static ItemStack blockSapphire;
    public static ItemStack blockSerendibite;

    public static ItemStack oreAgate;
    public static ItemStack oreAmethyst;
    public static ItemStack oreCitrine;
    public static ItemStack oreRuby;
    public static ItemStack oreSapphire;
    public static ItemStack oreSerendibite;

    // Handlers
    public static SimpleNetworkWrapper channel;

    public static GuiHandler guiHandler = new GuiHandler();

    public static Logger logger;
    public static Configuration config;
    public static String overrides;

    public final static Format prettyNumberFormatter = new DecimalFormat("#.#");
    public final static Format prettyNumberFormatter2 = new DecimalFormat("#0.0");

    public static CreativeTabs tabMagic = new CreativeTabs(MODID)
    {
        @Override
        public Item getTabIconItem()
        {
            return magicWand;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        ConfigManager.init(event.getSuggestedConfigurationFile());

        overrides = event.getModConfigurationDirectory() + File.separator + "elementsofpower_essences.json";

        CapabilityMagicContainer.register();

        registerItems();

        registerBlocks();

        loadTemplateStacks();

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

    private void registerItems()
    {
        logger.info("Initializing items...");

        magicOrb = new ItemMagicOrb("magicOrb");
        GameRegistry.register(magicOrb);

        magicWand = new ItemWand("magicWand");
        GameRegistry.register(magicWand);

        magicStaff = new ItemStaff("magicStaff");
        GameRegistry.register(magicStaff);

        magicRing = new ItemRing("magicRing");
        GameRegistry.register(magicRing);

        gemstone = new ItemGemstone("gemstone");
        GameRegistry.register(gemstone);

        analyzer = new ItemAnalyzer("analyzer");
        GameRegistry.register(analyzer);

        guidebook = new ItemGuidebook("guidebook");
        GameRegistry.register(guidebook);

        spelldust = new ItemSpelldust("spelldust");
        GameRegistry.register(spelldust);
    }

    private void registerBlocks()
    {
        logger.info("Initializing blocks...");

        essentializer = new BlockEssentializer("essentializer");
        GameRegistry.register(essentializer);
        GameRegistry.register(essentializer.createItemBlock());
        GameRegistry.registerTileEntity(TileEssentializer.class, "essentializerTile");

        dust = new BlockDust("dust");
        GameRegistry.register(dust);

        mist = new BlockDust("mist");
        GameRegistry.register(mist);

        spell_wire = new BlockSpelldust("spell_wire");
        GameRegistry.register(spell_wire);

        materialCushion = new MaterialCushion(MapColor.BLACK);
        cushion = new BlockCushion("cushion");
        GameRegistry.register(cushion);

        cocoon = new BlockCocoon("cocoon");
        GameRegistry.register(cocoon);
        GameRegistry.register(cocoon.createItemBlock());
        GameRegistry.registerTileEntity(TileCocoon.class, "cocoonTile");

        gemstoneBlock = new BlockGemstone("gemstoneBlock");
        GameRegistry.register(gemstoneBlock);
        GameRegistry.register(gemstoneBlock.createItemBlock());

        gemstoneOre = new BlockGemstoneOre("gemstoneOre");
        GameRegistry.register(gemstoneOre);
        GameRegistry.register(gemstoneOre.createItemBlock());
    }

    private void loadTemplateStacks()
    {
        logger.info("Generating template stacks...");

        fire = magicOrb.getStack(Element.Fire);
        water = magicOrb.getStack(Element.Water);
        air = magicOrb.getStack(Element.Air);
        earth = magicOrb.getStack(Element.Earth);
        light = magicOrb.getStack(Element.Light);
        darkness = magicOrb.getStack(Element.Darkness);
        life = magicOrb.getStack(Element.Life);
        death = magicOrb.getStack(Element.Death);

        gemRuby = gemstone.getStack(Gemstone.Ruby);
        gemSapphire = gemstone.getStack(Gemstone.Sapphire);
        gemCitrine = gemstone.getStack(Gemstone.Citrine);
        gemAgate = gemstone.getStack(Gemstone.Agate);
        gemQuartz = gemstone.getStack(Gemstone.Quartz);
        gemSerendibite = gemstone.getStack(Gemstone.Serendibite);
        gemEmerald = gemstone.getStack(Gemstone.Emerald);
        gemAmethyst = gemstone.getStack(Gemstone.Amethyst);
        gemDiamond = gemstone.getStack(Gemstone.Diamond);

        blockAgate = gemstoneBlock.getStack(GemstoneBlockType.Agate);
        blockAmethyst = gemstoneBlock.getStack(GemstoneBlockType.Amethyst);
        blockCitrine = gemstoneBlock.getStack(GemstoneBlockType.Citrine);
        blockRuby = gemstoneBlock.getStack(GemstoneBlockType.Ruby);
        blockSapphire = gemstoneBlock.getStack(GemstoneBlockType.Sapphire);
        blockSerendibite = gemstoneBlock.getStack(GemstoneBlockType.Serendibite);

        oreAgate = gemstoneOre.getStack(GemstoneBlockType.Agate);
        oreAmethyst = gemstoneOre.getStack(GemstoneBlockType.Amethyst);
        oreCitrine = gemstoneOre.getStack(GemstoneBlockType.Citrine);
        oreRuby = gemstoneOre.getStack(GemstoneBlockType.Ruby);
        oreSapphire = gemstoneOre.getStack(GemstoneBlockType.Sapphire);
        oreSerendibite = gemstoneOre.getStack(GemstoneBlockType.Serendibite);
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

        OreDictionary.registerOre("gemRuby", gemRuby);
        OreDictionary.registerOre("gemSapphire", gemSapphire);
        OreDictionary.registerOre("gemCitrine", gemCitrine);
        OreDictionary.registerOre("gemAgate", gemAgate);
        OreDictionary.registerOre("gemQuartz", gemQuartz);
        OreDictionary.registerOre("gemSerendibite", gemSerendibite);
        OreDictionary.registerOre("gemEmerald", gemEmerald);
        OreDictionary.registerOre("gemAmethyst", gemAmethyst);
        OreDictionary.registerOre("gemDiamond", gemDiamond);

        OreDictionary.registerOre("magicGemstone", gemRuby);
        OreDictionary.registerOre("magicGemstone", gemSapphire);
        OreDictionary.registerOre("magicGemstone", gemCitrine);
        OreDictionary.registerOre("magicGemstone", gemAgate);
        OreDictionary.registerOre("magicGemstone", gemQuartz);
        OreDictionary.registerOre("magicGemstone", gemSerendibite);
        OreDictionary.registerOre("magicGemstone", gemEmerald);
        OreDictionary.registerOre("magicGemstone", gemAmethyst);
        OreDictionary.registerOre("magicGemstone", gemDiamond);

        OreDictionary.registerOre("blockAgate", blockAgate);
        OreDictionary.registerOre("blockAmethyst", blockAmethyst);
        OreDictionary.registerOre("blockCitrine", blockCitrine);
        OreDictionary.registerOre("blockRuby", blockRuby);
        OreDictionary.registerOre("blockSapphire", blockSapphire);
        OreDictionary.registerOre("blockSerendibite", blockSerendibite);

        OreDictionary.registerOre("oreAgate", oreAgate);
        OreDictionary.registerOre("oreAmethyst", oreAmethyst);
        OreDictionary.registerOre("oreCitrine", oreCitrine);
        OreDictionary.registerOre("oreRuby", oreRuby);
        OreDictionary.registerOre("oreSapphire", oreSapphire);
        OreDictionary.registerOre("oreSerendibite", oreSerendibite);
    }

    private void registerEntities()
    {
        // Entities
        logger.info("Registering entities...");

        int entityId = 1;
        EntityRegistry.registerModEntity(EntityBall.class, "SpellBall", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityEssence.class, "Essence", entityId++, this, 80, 3, true, 0x0000FF, 0xFFFF00);
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

        GameRegistry.addRecipe(new ShapedOreRecipe(blockAgate, "aaa", "aaa", "aaa", 'a', "gemAgate"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockAmethyst, "aaa", "aaa", "aaa", 'a', "gemAmethyst"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockCitrine, "aaa", "aaa", "aaa", 'a', "gemCitrine"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockRuby, "aaa", "aaa", "aaa", 'a', "gemRuby"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockSapphire, "aaa", "aaa", "aaa", 'a', "gemSapphire"));
        GameRegistry.addRecipe(new ShapedOreRecipe(blockSerendibite, "aaa", "aaa", "aaa", 'a', "gemSerendibite"));

        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemAgate, 9), "blockAgate"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemAmethyst, 9), "blockAmethyst"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemCitrine, 9), "blockCitrine"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemRuby, 9), "blockRuby"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemSapphire, 9), "blockSapphire"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(copyStack(gemSerendibite, 9), "blockSerendibite"));

        FurnaceRecipes.instance().addSmeltingRecipe(oreAgate, gemAgate, 0);
        FurnaceRecipes.instance().addSmeltingRecipe(oreAmethyst, gemAmethyst, 0);
        FurnaceRecipes.instance().addSmeltingRecipe(oreCitrine, gemCitrine, 0);
        FurnaceRecipes.instance().addSmeltingRecipe(oreRuby, gemRuby, 0);
        FurnaceRecipes.instance().addSmeltingRecipe(oreSapphire, gemSapphire, 0);
        FurnaceRecipes.instance().addSmeltingRecipe(oreSerendibite, gemSerendibite, 0);

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
        GameRegistry.addShapelessRecipe(new ItemStack(guidebook), Items.BOOK, new ItemStack(magicOrb, 1, OreDictionary.WILDCARD_VALUE));
        GameRegistry.addRecipe(new GemstoneChangeRecipe());
        GameRegistry.addRecipe(new ContainerChargeRecipe());

        RecipeSorter.register("gemstoneChangeRecipe", GemstoneChangeRecipe.class, RecipeSorter.Category.SHAPELESS, "");
        RecipeSorter.register("containerChargeRecipe", ContainerChargeRecipe.class, RecipeSorter.Category.SHAPELESS, "");
    }

    private ItemStack copyStack(ItemStack original, int quantity)
    {
        ItemStack copy = original.copy();
        copy.stackSize = quantity;
        return copy;
    }

    public static ResourceLocation location(String location)
    {
        return new ResourceLocation(MODID, location);
    }
}

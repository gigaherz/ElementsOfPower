package gigaherz.elementsofpower;

import gigaherz.elementsofpower.analyzer.ItemAnalyzer;
import gigaherz.elementsofpower.blocks.BlockCushion;
import gigaherz.elementsofpower.blocks.BlockDust;
import gigaherz.elementsofpower.blocks.BlockGemstone;
import gigaherz.elementsofpower.blocks.BlockGemstoneOre;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.cocoons.BlockCocoon;
import gigaherz.elementsofpower.cocoons.TileCocoon;
import gigaherz.elementsofpower.database.EssenceConversions;
import gigaherz.elementsofpower.database.EssenceOverrides;
import gigaherz.elementsofpower.database.StockConversions;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import gigaherz.elementsofpower.essentializer.BlockEssentializer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.gemstones.ContainerChargeRecipe;
import gigaherz.elementsofpower.gemstones.GemstoneBlockType;
import gigaherz.elementsofpower.gemstones.GemstoneChangeRecipe;
import gigaherz.elementsofpower.gui.GuiHandler;
import gigaherz.elementsofpower.guidebook.ItemGuidebook;
import gigaherz.elementsofpower.items.*;
import gigaherz.elementsofpower.materials.MaterialCushion;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import gigaherz.elementsofpower.progression.DiscoveryHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumParticleTypes;
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
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;

@Mod(modid = ElementsOfPower.MODID, name = ElementsOfPower.MODNAME, version = ElementsOfPower.VERSION)
public class ElementsOfPower
{
    public static final String MODID = "elementsofpower";
    public static final String MODNAME = "Elements Of Power";
    public static final String VERSION = "@VERSION@";

    public static final String CHANNEL = "ElementsOfPower";

    // The instance of your mod that Forge uses.
    @Mod.Instance(value = ElementsOfPower.MODID)
    public static ElementsOfPower instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "gigaherz.elementsofpower.client.ClientProxy", serverSide = "gigaherz.elementsofpower.server.ServerProxy")
    public static ISideProxy proxy;

    // Block templates
    public static Block essentializer;
    public static Block dust;
    public static Block mist;
    public static Block cushion;
    public static Block cocoon;

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

    public static int SMALL_CLOUD_PARTICLE_ID;
    public static EnumParticleTypes SMALL_CLOUD_PARTICLE;

    void registerParticle()
    {
        SMALL_CLOUD_PARTICLE_ID = -1;
        for (EnumParticleTypes t : EnumParticleTypes.values())
        {
            SMALL_CLOUD_PARTICLE_ID = Math.max(SMALL_CLOUD_PARTICLE_ID, t.getParticleID() + 1);
        }
        SMALL_CLOUD_PARTICLE = EnumHelper.addEnum(EnumParticleTypes.class, "SMALL_CLOUD",
                new Class<?>[]{String.class, int.class, boolean.class},
                new Object[]{"small_cloud", SMALL_CLOUD_PARTICLE_ID, false});

        // Client-side rendering registered in: proxy.registerParticle();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        overrides = event.getModConfigurationDirectory() + File.separator + "elementsofpower_essences.json";

        CapabilityMagicContainer.register();

        registerParticle();

        // Initialize Block Materials
        logger.info("Initializing block materials...");
        materialCushion = new MaterialCushion(MapColor.blackColor);

        // Block and Item registration
        logger.info("Initializing blocks and items...");

        magicOrb = new ItemMagicOrb();
        GameRegistry.registerItem(magicOrb, "magicOrb");

        magicWand = new ItemWand();
        GameRegistry.registerItem(magicWand, "magicWand");

        magicStaff = new ItemStaff();
        GameRegistry.registerItem(magicStaff, "magicStaff");

        magicRing = new ItemRing();
        GameRegistry.registerItem(magicRing, "magicRing");

        essentializer = new BlockEssentializer();
        GameRegistry.registerBlock(essentializer, "essentializer");
        GameRegistry.registerTileEntity(TileEssentializer.class, "essentializerTile");

        dust = new BlockDust();
        GameRegistry.registerBlock(dust, "dust");

        mist = new BlockDust();
        GameRegistry.registerBlock(mist, "mist");

        cushion = new BlockCushion();
        GameRegistry.registerBlock(cushion, "cushion");

        cocoon = new BlockCocoon();
        GameRegistry.registerBlock(cocoon, "cocoon");
        GameRegistry.registerTileEntity(TileCocoon.class, "cocoonTile");

        gemstoneBlock = new BlockGemstone();
        GameRegistry.registerBlock(gemstoneBlock, BlockGemstone.Item.class, "gemstoneBlock");

        gemstoneOre = new BlockGemstoneOre();
        GameRegistry.registerBlock(gemstoneOre, BlockGemstoneOre.ItemForm.class, "gemstoneOre");

        gemstone = new ItemGemstone();
        GameRegistry.registerItem(gemstone, "gemstone");

        analyzer = new ItemAnalyzer();
        GameRegistry.registerItem(analyzer, "analyzer");

        guidebook = new ItemGuidebook();
        GameRegistry.registerItem(guidebook, "guidebook");

        // Template stacks
        logger.info("Generating template stacks...");

        fire = magicOrb.getStack(1, 0);
        water = magicOrb.getStack(1, 1);
        air = magicOrb.getStack(1, 2);
        earth = magicOrb.getStack(1, 3);
        light = magicOrb.getStack(1, 4);
        darkness = magicOrb.getStack(1, 5);
        life = magicOrb.getStack(1, 6);
        death = magicOrb.getStack(1, 7);

        gemRuby = gemstone.getStack(1, 0);
        gemSapphire = gemstone.getStack(1, 1);
        gemCitrine = gemstone.getStack(1, 2);
        gemAgate = gemstone.getStack(1, 3);
        gemQuartz = gemstone.getStack(1, 4);
        gemSerendibite = gemstone.getStack(1, 5);
        gemEmerald = gemstone.getStack(1, 6);
        gemAmethyst = gemstone.getStack(1, 7);
        gemDiamond = gemstone.getStack(1, 8);

        blockAgate = gemstoneBlock.getStack(1, GemstoneBlockType.Agate);
        blockAmethyst = gemstoneBlock.getStack(1, GemstoneBlockType.Amethyst);
        blockCitrine = gemstoneBlock.getStack(1, GemstoneBlockType.Citrine);
        blockRuby = gemstoneBlock.getStack(1, GemstoneBlockType.Ruby);
        blockSapphire = gemstoneBlock.getStack(1, GemstoneBlockType.Sapphire);
        blockSerendibite = gemstoneBlock.getStack(1, GemstoneBlockType.Serendibite);

        oreAgate = gemstoneOre.getStack(1, GemstoneBlockType.Agate);
        oreAmethyst = gemstoneOre.getStack(1, GemstoneBlockType.Amethyst);
        oreCitrine = gemstoneOre.getStack(1, GemstoneBlockType.Citrine);
        oreRuby = gemstoneOre.getStack(1, GemstoneBlockType.Ruby);
        oreSapphire = gemstoneOre.getStack(1, GemstoneBlockType.Sapphire);
        oreSerendibite = gemstoneOre.getStack(1, GemstoneBlockType.Serendibite);

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

        // Network channels
        logger.info("Registering network channel...");

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SpellSequenceUpdate.Handler.class, SpellSequenceUpdate.class, messageNumber++, Side.SERVER);
        channel.registerMessage(SpellcastSync.Handler.class, SpellcastSync.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(EssentializerAmountsUpdate.Handler.class, EssentializerAmountsUpdate.class, messageNumber++, Side.CLIENT);
        logger.debug("Final message number: " + messageNumber);

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

        // Entities
        logger.info("Registering entities...");

        int entityId = 1;
        EntityRegistry.registerModEntity(EntityBall.class, "SpellBall", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityEssence.class, "Essence", entityId++, this, 80, 3, true, 0x0000FF, 0xFFFF00);
        logger.debug("Next entity id: " + entityId);

        // Worldgen

        GameRegistry.registerWorldGenerator(new BlockGemstoneOre.Generator(), 1);
        GameRegistry.registerWorldGenerator(new BlockCocoon.Generator(), 1);

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
                'I', Items.iron_ingot,
                'O', Blocks.obsidian,
                'Q', "magicGemstone",
                'N', Items.nether_star));
        GameRegistry.addShapedRecipe(new ItemStack(magicWand),
                " G",
                "S ",
                'G', Items.gold_ingot,
                'S', Items.stick);
        GameRegistry.addRecipe(new ItemStack(magicStaff),
                " GW",
                " SG",
                "S  ",
                'W', new ItemStack(magicWand, 1, OreDictionary.WILDCARD_VALUE),
                'G', Items.gold_ingot,
                'S', Items.stick);
        GameRegistry.addRecipe(new ItemStack(magicRing),
                " GG",
                "G G",
                " G ",
                'G', Items.gold_ingot);
        GameRegistry.addRecipe(new GemstoneChangeRecipe());
        GameRegistry.addRecipe(new ContainerChargeRecipe());

        // Gui
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        StockConversions.registerEssenceSources();
        EssenceOverrides.loadOverrides();
    }

    private ItemStack copyStack(ItemStack original, int quantity)
    {
        ItemStack copy = original.copy();
        copy.stackSize = quantity;
        return copy;
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        EssenceConversions.registerEssencesForRecipes();
    }
}

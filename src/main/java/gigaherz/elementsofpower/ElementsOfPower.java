package gigaherz.elementsofpower;

import gigaherz.common.BlockRegistered;
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
import gigaherz.elementsofpower.items.*;
import gigaherz.elementsofpower.network.*;
import gigaherz.elementsofpower.spelldust.ItemSpelldust;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.spells.blocks.BlockCushion;
import gigaherz.elementsofpower.spells.blocks.BlockDust;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Format;

//import gigaherz.elementsofpower.progression.DiscoveryHandler;

@Mod.EventBusSubscriber
@Mod(modid = ElementsOfPower.MODID,
        name = ElementsOfPower.MODNAME, version = ElementsOfPower.VERSION,
        acceptedMinecraftVersions = "[1.12.0,1.13.0)",
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
    public static ItemMagicOrb orb;
    public static ItemWand wand;
    public static ItemWand staff;
    public static ItemBauble ring;
    public static ItemBauble headband;
    public static ItemBauble necklace;
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

    public static CreativeTabs tabMagic = new CreativeTabs(MODID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return wand.getStack(Gemstone.Diamond, Quality.Common);
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

        GameRegistry.registerTileEntity(TileEssentializer.class, essentializer.getRegistryName());
        GameRegistry.registerTileEntity(TileCocoon.class, cocoon.getRegistryName());

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

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                essentializer.createItemBlock(),
                cocoon.createItemBlock(),
                gemstoneBlock.createItemBlock(),
                gemstoneOre.createItemBlock(),

                orb = new ItemMagicOrb("orb"),
                wand = new ItemWand("wand"),
                staff = new ItemStaff("staff"),
                ring = new ItemRing("ring"),
                headband = new ItemHeadband("headband"),
                necklace = new ItemNecklace("necklace"),
                gemstone = new ItemGemstone("gemstone"),
                analyzer = new ItemAnalyzer("analyzer"),
                spelldust = new ItemSpelldust("spelldust")
        );

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
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Agate), gemstone.getStack(Gemstone.Agate), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Amethyst), gemstone.getStack(Gemstone.Amethyst), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Citrine), gemstone.getStack(Gemstone.Citrine), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Ruby), gemstone.getStack(Gemstone.Ruby), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Sapphire), gemstone.getStack(Gemstone.Sapphire), 0);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Serendibite), gemstone.getStack(Gemstone.Serendibite), 0);

        /*
        event.getRegistry().registerAll(

                new ShapedOreRecipe(null, gemstoneBlock.getStack(GemstoneBlockType.Agate), "aaa", "aaa", "aaa", 'a', "gemAgate"),
                new ShapedOreRecipe(null, gemstoneBlock.getStack(GemstoneBlockType.Amethyst), "aaa", "aaa", "aaa", 'a', "gemAmethyst"),
                new ShapedOreRecipe(null, gemstoneBlock.getStack(GemstoneBlockType.Citrine), "aaa", "aaa", "aaa", 'a', "gemCitrine"),
                new ShapedOreRecipe(null, gemstoneBlock.getStack(GemstoneBlockType.Ruby), "aaa", "aaa", "aaa", 'a', "gemRuby"),
                new ShapedOreRecipe(null, gemstoneBlock.getStack(GemstoneBlockType.Sapphire), "aaa", "aaa", "aaa", 'a', "gemSapphire"),
                new ShapedOreRecipe(null, gemstoneBlock.getStack(GemstoneBlockType.Serendibite), "aaa", "aaa", "aaa", 'a', "gemSerendibite"),
                new ShapelessOreRecipe(null, copyStack(gemstone.getStack(Gemstone.Agate), 9), "blockAgate"),
                new ShapelessOreRecipe(null, copyStack(gemstone.getStack(Gemstone.Amethyst), 9), "blockAmethyst"),
                new ShapelessOreRecipe(null, copyStack(gemstone.getStack(Gemstone.Citrine), 9), "blockCitrine"),
                new ShapelessOreRecipe(null, copyStack(gemstone.getStack(Gemstone.Ruby), 9), "blockRuby"),
                new ShapelessOreRecipe(null, copyStack(gemstone.getStack(Gemstone.Sapphire), 9), "blockSapphire"),
                new ShapelessOreRecipe(null, copyStack(gemstone.getStack(Gemstone.Serendibite), 9), "blockSerendibite"),

                new ShapedOreRecipe(null, new ItemStack(analyzer),
                        "glg",
                        "i  ",
                        "psp",
                        'g', "ingotGold",
                        'l', "paneGlass",
                        'i', "ingotIron",
                        's', "slabWood",
                        'p', "plankWood"),
                new ShapedOreRecipe(null, new ItemStack(essentializer, 1),
                        "IQI",
                        "ONO",
                        "IOI",
                        'I', Items.IRON_INGOT,
                        'O', Blocks.OBSIDIAN,
                        'Q', "magicGemstone",
                        'N', Items.NETHER_STAR),
                new ShapedOreRecipe(null, new ItemStack(wand),
                        " G",
                        "S ",
                        'G', Items.GOLD_INGOT,
                        'S', Items.STICK),
                new ShapedOreRecipe(null, new ItemStack(staff),
                        " GW",
                        " SG",
                        "S  ",
                        'W', new ItemStack(wand, 1, OreDictionary.WILDCARD_VALUE),
                        'G', Items.GOLD_INGOT,
                        'S', Items.STICK),
                new ShapedOreRecipe(null, new ItemStack(ring),
                        " GG",
                        "G G",
                        " G ",
                        'G', Items.GOLD_INGOT),

                //if (guidebookStack != null)
                //    GameRegistry.addShapelessRecipe(guidebookStack, Items.BOOK, new ItemStack(orb, 1, OreDictionary.WILDCARD_VALUE));

                new GemstoneChangeRecipe(),
                new ContainerChargeRecipe()
        );
        */
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        ConfigManager.init(event.getSuggestedConfigurationFile());

        overrides = event.getModConfigurationDirectory() + File.separator + "elementsofpower_essences.json";

        CapabilityMagicContainer.register();

        registerNetwork();

        SpellcastEntityData.register();
        //DiscoveryHandler.init();
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event)
    {

        int entityId = 1;
        event.getRegistry().registerAll(
                EntityEntryBuilder.create().name("SpellBall")
                        .id(location("spell_ball"), entityId++)
                        .entity(EntityBall.class).factory(EntityBall::new)
                        .tracker(80, 3, true).build(),

                EntityEntryBuilder.create().name("Essence")
                        .id(location("essence"), entityId++)
                        .entity(EntityEssence.class).factory(EntityEssence::new)
                        .tracker(80, 3, true)
                        .egg(0x0000FF, 0xFFFF00).build()
        );
        logger.debug("Next entity id: " + entityId);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        registerWorldGenerators();

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
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(SpellSequenceUpdate.Handler.class, SpellSequenceUpdate.class, messageNumber++, Side.SERVER);
        channel.registerMessage(SpellcastSync.Handler.class, SpellcastSync.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(EssentializerAmountsUpdate.Handler.class, EssentializerAmountsUpdate.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(EssentializerTileUpdate.Handler.class, EssentializerTileUpdate.class, messageNumber++, Side.CLIENT);
        channel.registerMessage(AddVelocityPlayer.Handler.class, AddVelocityPlayer.class, messageNumber++, Side.CLIENT);
        logger.debug("Final message number: " + messageNumber);
    }

    private void registerWorldGenerators()
    {
        if (ConfigManager.EnableGemstoneOregen)
            GameRegistry.registerWorldGenerator(new BlockGemstoneOre.Generator(), 1);
        if (ConfigManager.EnableCocoonGeneration)
            GameRegistry.registerWorldGenerator(new BlockCocoon.Generator(), 1);
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

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

    @GameRegistry.ObjectHolder(MODID + ":essentializer")
    public static BlockRegistered essentializer;
    @GameRegistry.ObjectHolder(MODID + ":dust")
    public static BlockRegistered dust;
    @GameRegistry.ObjectHolder(MODID + ":mist")
    public static BlockRegistered mist;
    @GameRegistry.ObjectHolder(MODID + ":cushion")
    public static BlockRegistered cushion;
    @GameRegistry.ObjectHolder(MODID + ":cocoon")
    public static BlockRegistered cocoon;
    @GameRegistry.ObjectHolder(MODID + ":gemstone_ore")
    public static BlockGemstoneOre gemstoneOre;
    @GameRegistry.ObjectHolder(MODID + ":gemstone_block")
    public static BlockGemstone gemstoneBlock;

    // Block Materials
    public static Material materialCushion = new MaterialCushion(MapColor.BLACK);

    // Item templates
    @GameRegistry.ObjectHolder(MODID + ":orb")
    public static ItemMagicOrb orb;
    @GameRegistry.ObjectHolder(MODID + ":wand")
    public static ItemWand wand;
    @GameRegistry.ObjectHolder(MODID + ":staff")
    public static ItemWand staff;
    @GameRegistry.ObjectHolder(MODID + ":ring")
    public static ItemBauble ring;
    @GameRegistry.ObjectHolder(MODID + ":headband")
    public static ItemBauble headband;
    @GameRegistry.ObjectHolder(MODID + ":necklace")
    public static ItemBauble necklace;
    @GameRegistry.ObjectHolder(MODID + ":gemstone")
    public static ItemGemstone gemstone;
    @GameRegistry.ObjectHolder(MODID + ":analyzer")
    public static ItemAnalyzer analyzer;
    @GameRegistry.ObjectHolder(MODID + ":spelldust")
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
                new BlockEssentializer("essentializer"),
                new BlockDust("dust"),
                new BlockDust("mist"),
                new BlockCushion("cushion"),
                new BlockCocoon("cocoon"),
                new BlockGemstone("gemstone_block"),
                new BlockGemstoneOre("gemstone_ore")
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

                new ItemMagicOrb("orb"),
                new ItemWand("wand"),
                new ItemStaff("staff"),
                new ItemRing("ring"),
                new ItemHeadband("headband"),
                new ItemNecklace("necklace"),
                new ItemGemstone("gemstone"),
                new ItemAnalyzer("analyzer"),
                new ItemSpelldust("spelldust")
        );
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Agate), gemstone.getStack(Gemstone.Agate), 1.0F);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Amethyst), gemstone.getStack(Gemstone.Amethyst), 1.0F);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Citrine), gemstone.getStack(Gemstone.Citrine), 1.0F);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Ruby), gemstone.getStack(Gemstone.Ruby), 1.0F);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Sapphire), gemstone.getStack(Gemstone.Sapphire), 1.0F);
        FurnaceRecipes.instance().addSmeltingRecipe(gemstoneOre.getStack(GemstoneBlockType.Serendibite), gemstone.getStack(Gemstone.Serendibite), 1.0F);
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

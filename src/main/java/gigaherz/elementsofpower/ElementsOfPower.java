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
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_QUADS;

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


    @Mod.EventBusSubscriber(value = Side.CLIENT, modid = MODID)
    public static class Test
    {
        public static final Test INSTANCE = new Test();

        @SubscribeEvent
        public static void worldLast(RenderWorldLastEvent event)
        {
            INSTANCE.render(event.getPartialTicks());
        }


        private static Star[] stars = new Star[4096];
        static
        {
            generateStars();
        }

        private static void generateStars()
        {
            final Random random = new Random(0);
            final int radius = 100;
            final double threshold = 0.15;

            for (int i = 0; i < stars.length; i++)
            {
                Star star = new Star();
                double u = 2 * Math.PI * random.nextFloat();
                double v = Math.acos(1 - 2 * random.nextFloat()) - Math.PI*0.5;
                double size = (1 + 2 * random.nextFloat()) * .05f;

                // prevents aliasing artifacts, making small stars fainter but not smaller
                star.alpha = Math.min(size * (1/threshold), 1);
                if (size < threshold)
                    size = threshold;

                star.points = new Vector3d[]{
                        new Vector3d(-size*0.5,-size*0.5,radius),
                        new Vector3d(-size * 2,0,radius),
                        new Vector3d(-size*0.5,size*0.5,radius),
                        new Vector3d(0,0,radius),

                        new Vector3d(-size*0.5,size*0.5,radius),
                        new Vector3d(0,size*2,radius),
                        new Vector3d(size*0.5,size*0.5,radius),
                        new Vector3d(0,0,radius),

                        new Vector3d(size*0.5,size*0.5,radius),
                        new Vector3d(size*2,0,radius),
                        new Vector3d(size*0.5,-size*0.5,radius),
                        new Vector3d(0,0,radius),

                        new Vector3d(size*0.5,-size*0.5,radius),
                        new Vector3d(0,-size*2,radius),
                        new Vector3d(-size*0.5,-size*0.5,radius),
                        new Vector3d(0,0,radius),
                };

                Matrix3d matrix1 = new Matrix3d();
                matrix1.rotY(u);
                Matrix3d matrix2 = new Matrix3d();
                matrix2.rotX(v);
                matrix1.mul(matrix2);

                Arrays.stream(star.points).forEach(matrix1::transform);

                stars[i] = star;
            }
        }

        private BufferBuilder bufferbuilder;
        public void render(float partialTicks)
        {
            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.depthMask(false);
            if (bufferbuilder == null)
            {
                bufferbuilder = new BufferBuilder(stars.length * 8);
                bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                for (Star star : stars)
                {
                    for (Vector3d point : star.points)
                    {
                        bufferbuilder.pos(point.x, point.y, point.z).color(255, 255, 255, (int) (255 * star.alpha)).endVertex();
                    }
                }
                bufferbuilder.finishDrawing();
            }
            draw(bufferbuilder);

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
        }

        public void draw(BufferBuilder bufferBuilderIn)
        {
            if (bufferBuilderIn.getVertexCount() > 0)
            {
                VertexFormat vertexformat = bufferBuilderIn.getVertexFormat();
                int i = vertexformat.getSize();
                ByteBuffer bytebuffer = bufferBuilderIn.getByteBuffer();
                List<VertexFormatElement> list = vertexformat.getElements();

                for (int j = 0; j < list.size(); ++j)
                {
                    VertexFormatElement vertexformatelement = list.get(j);
                    bytebuffer.position(vertexformat.getOffset(j));

                    // moved to VertexFormatElement.preDraw
                    vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
                }

                GlStateManager.glDrawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount());
                int i1 = 0;

                for (int j1 = list.size(); i1 < j1; ++i1)
                {
                    VertexFormatElement vertexformatelement1 = list.get(i1);

                    // moved to VertexFormatElement.postDraw
                    vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
                }
            }
        }

        private static class Star
        {
            public Vector3d[] points;
            public double alpha;
        }
    }
}

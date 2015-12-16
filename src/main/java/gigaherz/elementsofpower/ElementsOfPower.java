package gigaherz.elementsofpower;

import com.google.common.base.Predicate;
import gigaherz.elementsofpower.blocks.BlockCushion;
import gigaherz.elementsofpower.blocks.BlockDust;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.entities.*;
import gigaherz.elementsofpower.essentializer.BlockEssentializer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.gui.GuiHandler;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.materials.MaterialCushion;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.storage.TestEntityData;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import org.apache.logging.log4j.Logger;

@Mod(modid = ElementsOfPower.MODID, name = ElementsOfPower.MODNAME, version = ElementsOfPower.VERSION)
public class ElementsOfPower
{
    public static final String MODID = "elementsofpower";
    public static final String MODNAME = "Elements Of Power";
    public static final String VERSION = "@VERSION@";

    public static final String CHANNEL = "ElementsOfPower";

    // Block templates
    public static Block essentializer;
    public static Block dust;
    public static Block cushion;

    // Block Materials
    public static Material materialCushion;

    // Item templates
    public static ItemMagicOrb magicOrb;

    public static Item magicContainer;

    public static ItemWand magicWand;

    // Subitems
    public static ItemStack wandLapis;
    public static ItemStack wandEmerald;
    public static ItemStack wandDiamond;
    public static ItemStack wandCreative;

    public static ItemStack staffLapis;
    public static ItemStack staffEmerald;
    public static ItemStack staffDiamond;
    public static ItemStack staffCreative;

    public static ItemStack fire;
    public static ItemStack water;
    public static ItemStack air;
    public static ItemStack earth;

    public static ItemStack light;
    public static ItemStack darkness;
    public static ItemStack life;
    public static ItemStack death;

    // The instance of your mod that Forge uses.
    @Mod.Instance(value = ElementsOfPower.MODID)
    public static ElementsOfPower instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "gigaherz.elementsofpower.client.ClientProxy", serverSide = "gigaherz.elementsofpower.server.ServerProxy")
    public static ISideProxy proxy;

    public static SimpleNetworkWrapper channel;

    private GuiHandler guiHandler = new GuiHandler();

    public static Logger logger;

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

        // Initialize Block Materials
        logger.info("Initializing block materials...");
        materialCushion = new MaterialCushion(MapColor.blackColor);

        // Block and Item registration
        logger.info("Initializing blocks and items...");

        magicOrb = new ItemMagicOrb();
        GameRegistry.registerItem(magicOrb, "magicOrb");

        magicContainer = new ItemMagicContainer();
        GameRegistry.registerItem(magicContainer, "magicContainer");

        magicWand = new ItemWand();
        GameRegistry.registerItem(magicWand, "magicWand");

        essentializer = new BlockEssentializer();
        GameRegistry.registerBlock(essentializer, "essentializer");
        GameRegistry.registerTileEntity(TileEssentializer.class, "essentializerTile");

        dust = new BlockDust();
        GameRegistry.registerBlock(dust, "dust");

        cushion = new BlockCushion();
        GameRegistry.registerBlock(cushion, "cushion");

        // Template stacks
        logger.info("Generating template stacks...");

        wandLapis = magicWand.getStack(1, 0);
        wandEmerald = magicWand.getStack(1, 1);
        wandDiamond = magicWand.getStack(1, 2);
        wandCreative = magicWand.getStack(1, 3);
        staffLapis = magicWand.getStack(1, 4);
        staffEmerald = magicWand.getStack(1, 5);
        staffDiamond = magicWand.getStack(1, 6);
        staffCreative = magicWand.getStack(1, 7);
        fire = magicOrb.getStack(1, 0);
        water = magicOrb.getStack(1, 1);
        air = magicOrb.getStack(1, 2);
        earth = magicOrb.getStack(1, 3);
        light = magicOrb.getStack(1, 4);
        darkness = magicOrb.getStack(1, 5);
        life = magicOrb.getStack(1, 6);
        death = magicOrb.getStack(1, 7);

        // Network channels
        logger.info("Registering network channel...");

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
        channel.registerMessage(SpellSequenceUpdate.Handler.class, SpellSequenceUpdate.class, 0, Side.SERVER);

        logger.info("Performing pre-initialization proxy tasks...");

        TestEntityData.initHandling();

        // Client-side code
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
        EntityRegistry.registerModEntity(EntityAirball.class, "Airball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityDustball.class, "Earthball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityLavaball.class, "Lavaball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityLifeball.class, "Lifeball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityFrostball.class, "Frostball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityWaterball.class, "Waterball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityFlameball.class, "Flameball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityFireBeam.class, "Flamebeam", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityTeleporter.class, "Teleporter", entityId++, this, 80, 3, true);
        logger.debug("Next entity id: " + entityId);

        // Recipes
        logger.info("Registering recipes...");

        GameRegistry.addRecipe(new ItemStack(essentializer, 1),
                "IQI",
                "ONO",
                "IOI",
                'I', Items.iron_ingot,
                'O', Blocks.obsidian,
                'Q', Items.quartz,
                'N', Items.nether_star);
        GameRegistry.addRecipe(wandLapis,
                " G",
                "S ",
                'G', new ItemStack(Items.dye, 1, 4),
                'S', Items.stick);
        GameRegistry.addRecipe(wandEmerald,
                " G",
                "S ",
                'G', Items.emerald,
                'S', Items.stick);
        GameRegistry.addRecipe(wandDiamond,
                " G",
                "S ",
                'G', Items.diamond,
                'S', Items.stick);
        GameRegistry.addRecipe(staffLapis,
                " GW",
                " SG",
                "S  ",
                'W', wandLapis,
                'G', Blocks.quartz_block,
                'S', Items.stick);
        GameRegistry.addRecipe(staffEmerald,
                " GW",
                " SG",
                "S  ",
                'W', wandEmerald,
                'G', Blocks.quartz_block,
                'S', Items.stick);
        GameRegistry.addRecipe(staffDiamond,
                " GW",
                " SG",
                "S  ",
                'W', wandDiamond,
                'G', Blocks.quartz_block,
                'S', Items.stick);

        // Gui
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        MagicDatabase.initialize();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        MagicDatabase.postInitialize();
    }
}

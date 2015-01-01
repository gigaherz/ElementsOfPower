package gigaherz.elementsofpower;

import net.minecraft.block.Block;
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
import net.minecraftforge.fml.common.registry.GameRegistry;


@Mod(modid = ElementsOfPower.MODID, name = ElementsOfPower.MODNAME, version = ElementsOfPower.VERSION)
public class ElementsOfPower {
    public static final String MODID = "ElementsOfPower";
    public static final String MODNAME = "Elements Of Power";
    public static final String VERSION = "1.0";

    private final static int firstItemId = 24400;
    private final static int firstBlockId = 2450;

    private final static int defaultEssentializerId = firstBlockId + 1;
    private final static int defaultMagicOrbId = firstItemId + 1;
    private static final int defaultLapisContainerId = firstItemId + 2;
    private static final int defaultEmeraldContainerId = firstItemId + 3;
    private static final int defaultDiamondContainerId = firstItemId + 4;
    private static final int defaultWandId = firstItemId + 5;
    private static final int defaultStaffId = firstItemId + 6;

    // Block templates
    public static Block essentializer;

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
    @SidedProxy(clientSide = "gigaherz.elementsofpower.client.ClientProxy", serverSide = "gigaherz.elementsofpower.CommonProxy")
    public static CommonProxy proxy;

    private GuiHandler guiHandler = new GuiHandler();

    public static final CreativeTabs tabMagic = new CreativeTabs(MODID.toLowerCase()) {
        @Override
        public Item getTabIconItem() {
            return magicWand;
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //Configuration config;
        //config = new Configuration(event.getSuggestedConfigurationFile());
        //config.load();
        //config.save();

        magicOrb = new ItemMagicOrb();
        GameRegistry.registerItem(magicOrb, "magicOrb");

        magicContainer = new ItemMagicContainer();
        GameRegistry.registerItem(magicContainer, "magicContainer");

        magicWand = new ItemWand();
        GameRegistry.registerItem(magicWand, "magicWand");

        essentializer = new Essentializer();
        GameRegistry.registerBlock(essentializer, "essentializer");

        GameRegistry.registerTileEntity(EssentializerTile.class, "essentializerTile");

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

        MagicDatabase.preInitialize();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();

        // Recipes
        GameRegistry.addRecipe(new ItemStack(essentializer, 1),
                "IOI",
                "ODO",
                "IOI",
                'I', Items.iron_ingot,
                'O', Blocks.obsidian,
                'D', Items.diamond);
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
                'G', new ItemStack(Items.dye, 1, 4),
                'S', Items.stick);
        GameRegistry.addRecipe(staffEmerald,
                " GW",
                " SG",
                "S  ",
                'W', wandEmerald,
                'G', Items.emerald,
                'S', Items.stick);
        GameRegistry.addRecipe(staffDiamond,
                " GW",
                " SG",
                "S  ",
                'W', wandDiamond,
                'G', Items.diamond,
                'S', Items.stick);
        // Gui
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        MagicDatabase.initialize();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Stub Method
        MagicDatabase.postInitialize();
    }
}

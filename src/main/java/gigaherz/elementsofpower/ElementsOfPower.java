package gigaherz.elementsofpower;

import gigaherz.elementsofpower.blocks.BlockDust;
import gigaherz.elementsofpower.blocks.BlockEssentializer;
import gigaherz.elementsofpower.blocks.TileEssentializer;
import gigaherz.elementsofpower.entities.*;
import gigaherz.elementsofpower.items.ItemMagicContainer;
import gigaherz.elementsofpower.items.ItemMagicOrb;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.models.ModelRegistrationHelper;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
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
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;


@Mod(modid = ElementsOfPower.MODID, name = ElementsOfPower.MODNAME, version = ElementsOfPower.VERSION)
public class ElementsOfPower {
    public static final String MODID = "ElementsOfPower";
    public static final String MODNAME = "Elements Of Power";
    public static final String VERSION = "1.0";

    public static final String CHANNEL = "ElementsOfPower";

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
    public static Block dust;

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

    public static SimpleNetworkWrapper channel;

    public static ModelRegistrationHelper modelRegistrationHelper;

    private GuiHandler guiHandler = new GuiHandler();

    public static final CreativeTabs tabMagic = new CreativeTabs(MODID.toLowerCase()) {
        @Override
        public Item getTabIconItem() {
            return magicWand;
        }
    };

    private void registerNetworkStuff() {
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
        channel.registerMessage(SpellSequenceUpdate.Handler.class, SpellSequenceUpdate.class, 0, Side.SERVER);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        modelRegistrationHelper = new ModelRegistrationHelper();

        proxy.registerGuiOverlay();

        proxy.registerCustomBakedModels();

        // Block and Item registration
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

        // Template stacks
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

        registerNetworkStuff();

        // Item decomposing database
        MagicDatabase.preInitialize();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

        int entityId = 1;
        EntityRegistry.registerModEntity(EntityFrostball.class, "Frostball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityWaterball.class, "Waterball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityFlameball.class, "Flameball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityAirball.class, "Airball", entityId++, this, 80, 3, true);
        EntityRegistry.registerModEntity(EntityEarthball.class, "Earthball", entityId, this, 80, 3, true);

        proxy.registerRenderers();

        // Recipes
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
    public void postInit(FMLPostInitializationEvent event) {
        // Stub Method
        MagicDatabase.postInitialize();
    }
}

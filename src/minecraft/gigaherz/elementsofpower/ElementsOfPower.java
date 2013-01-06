package gigaherz.elementsofpower;

import gigaherz.elementsofpower.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "ElementsOfPower", name = "ElementsOfPower", version = "0.1.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
        clientPacketHandlerSpec = @SidedPacketHandler(channels = { ElementsOfPower.ChannelName }, packetHandler = ClientProxy.class),
        serverPacketHandlerSpec = @SidedPacketHandler(channels = { ElementsOfPower.ChannelName }, packetHandler = CommonProxy.class))
public class ElementsOfPower
{
    public final static String ChannelName = "ElementsOfPower";

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

    public static Item lapisContainer;
    public static Item emeraldContainer;
    public static Item diamondContainer;

    public static ItemWand magicWand;
    public static ItemWand magicStaff;

    // Subitems
    public static ItemStack wandLapis;
    public static ItemStack wandEmerald;
    public static ItemStack wandDiamond;

    public static ItemStack staffLapis;
    public static ItemStack staffEmerald;
    public static ItemStack staffDiamond;

    public static ItemStack fire;
    public static ItemStack water;
    public static ItemStack air;
    public static ItemStack earth;

    public static ItemStack light;
    public static ItemStack darkness;
    public static ItemStack life;
    public static ItemStack death;

    // The instance of your mod that Forge uses.
    @Instance("ElementsOfPower")
    public static ElementsOfPower instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "gigaherz.elementsofpower.client.ClientProxy", serverSide = "gigaherz.elementsofpower.CommonProxy")
    public static CommonProxy proxy;

    private GuiHandler guiHandler = new GuiHandler();

    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration config;
        Property prop;
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        prop = config.getItem("magicOrb", defaultMagicOrbId);
        magicOrb = new ItemMagicOrb(prop.getInt());
        prop = config.getItem("lapisContainer", defaultLapisContainerId);
        lapisContainer = new ItemMagicContainer(prop.getInt()).setItemName("magicLapisContainer").setIconCoord(14, 8);
        prop = config.getItem("emeraldContainer", defaultEmeraldContainerId);
        emeraldContainer = new ItemMagicContainer(prop.getInt()).setItemName("magicEmeraldContainer").setIconCoord(10, 11);
        prop = config.getItem("diamondContainer", defaultDiamondContainerId);
        diamondContainer = new ItemMagicContainer(prop.getInt()).setItemName("magicDiamondContainer").setIconCoord(7, 3);
        prop = config.getItem("magicWand", defaultWandId);
        magicWand = (ItemWand)new ItemWand(prop.getInt()).setItemName("magicWand").setIconCoord(0, 3).setCreativeTab(CreativeTabs.tabTools);
        prop = config.getItem("magicStaff", defaultStaffId);
        magicStaff = (ItemWand)new ItemWand(prop.getInt()).setItemName("magicStaff").setIconCoord(1, 3).setCreativeTab(CreativeTabs.tabTools);
        prop = config.getBlock("essentializer", defaultEssentializerId);
        essentializer = new Essentializer("essentializer", prop.getInt(), Material.iron, CreativeTabs.tabMisc)
        .setHardness(15.0F).setStepSound(Block.soundMetalFootstep);
        wandLapis = magicWand.getStack(1, 0);
        wandEmerald = magicWand.getStack(1, 1);
        wandDiamond = magicWand.getStack(1, 2);
        staffLapis = magicStaff.getStack(1, 0);
        staffEmerald = magicStaff.getStack(1, 1);
        staffDiamond = magicStaff.getStack(1, 2);
        fire = magicOrb.getStack(1, 0);
        water = magicOrb.getStack(1, 1);
        air = magicOrb.getStack(1, 2);
        earth = magicOrb.getStack(1, 3);
        light = magicOrb.getStack(1, 4);
        darkness = magicOrb.getStack(1, 5);
        life = magicOrb.getStack(1, 6);
        death = magicOrb.getStack(1, 7);
        MagicDatabase.preInitialize(config);
        config.save();
    }

    @Init
    public void load(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        GameRegistry.registerTileEntity(EssentializerTile.class, "essentializerTile");
        MinecraftForge.setBlockHarvestLevel(essentializer, "pickaxe", 0);
        // Registration
        GameRegistry.registerBlock(essentializer, essentializer.getBlockName());
        LanguageRegistry.addName(essentializer, "Essentializer");
        // Containers
        LanguageRegistry.addName(lapisContainer, "Magic-imbued Lapis Lazuli");
        LanguageRegistry.addName(emeraldContainer, "Magic-imbued Emerald");
        LanguageRegistry.addName(diamondContainer, "Magic-imbued Diamond");
        LanguageRegistry.addName(wandLapis, "Lapis Wand");
        LanguageRegistry.addName(wandEmerald, "Emerald Wand");
        LanguageRegistry.addName(wandDiamond, "Diamond Wand");
        LanguageRegistry.addName(staffLapis, "Lapis Staff");
        LanguageRegistry.addName(staffEmerald, "Emerald Staff");
        LanguageRegistry.addName(staffDiamond, "Diamond Staff");
        // Magics
        LanguageRegistry.addName(water, "Fire");
        LanguageRegistry.addName(water, "Water");
        LanguageRegistry.addName(air, "Air");
        LanguageRegistry.addName(earth, "Earth");
        LanguageRegistry.addName(light, "Light");
        LanguageRegistry.addName(darkness, "Darkness");
        LanguageRegistry.addName(life, "Life");
        LanguageRegistry.addName(death, "Death");
        // Recipes
        GameRegistry.addRecipe(new ItemStack(essentializer, 1),
                "IOI",
                "ODO",
                "IOI",
                'I', Item.ingotIron,
                'O', Block.obsidian,
                'D', Item.diamond);
        GameRegistry.addRecipe(wandLapis,
                " G",
                "S ",
                'G', new ItemStack(Item.dyePowder, 1, 4),
                'S', Item.stick);
        GameRegistry.addRecipe(wandEmerald,
                " G",
                "S ",
                'G', Item.emerald,
                'S', Item.stick);
        GameRegistry.addRecipe(wandDiamond,
                " G",
                "S ",
                'G', Item.diamond,
                'S', Item.stick);
        GameRegistry.addRecipe(staffLapis,
                " GW",
                " SG",
                "S  ",
                'W', wandLapis,
                'G', new ItemStack(Item.dyePowder, 1, 4),
                'S', Item.stick);
        GameRegistry.addRecipe(staffEmerald,
                " GW",
                " SG",
                "S  ",
                'W', wandEmerald,
                'G', Item.emerald,
                'S', Item.stick);
        GameRegistry.addRecipe(staffDiamond,
                " GW",
                " SG",
                "S  ",
                'W', wandDiamond,
                'G', Item.diamond,
                'S', Item.stick);
        // Gui
        NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
        MagicDatabase.initialize();        
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event)
    {
        // Stub Method
        MagicDatabase.postInitialize();
    }
}

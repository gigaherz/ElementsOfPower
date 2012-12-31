package gigaherz.elementsofpower;

import gigaherz.elementsofpower.client.ClientProxy;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.Loader;
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
	private static final int defaultLapisContainerId = firstItemId+2;
	private static final int defaultEmeraldContainerId = firstItemId+3;
	private static final int defaultDiamondContainerId = firstItemId+4;

    // Item templates
    public static MagicOrb magicOrb;

	public static Item lapisContainer;
	public static Item emeraldContainer;
	public static Item diamondContainer;

    // Block templates
    public static Block essentializer;

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
        magicOrb = new MagicOrb(prop.getInt());
        
        prop = config.getItem("lapisContainer", defaultLapisContainerId);
        lapisContainer = new ItemMagicContainer(prop.getInt()).setIconCoord(14, 8);

        prop = config.getItem("emeraldContainer", defaultEmeraldContainerId);
        lapisContainer = new ItemMagicContainer(prop.getInt()).setIconCoord(10, 11);

        prop = config.getItem("diamondContainer", defaultDiamondContainerId);
        lapisContainer = new ItemMagicContainer(prop.getInt()).setIconCoord(7, 3);
        
        prop = config.getBlock("essentializer", defaultEssentializerId);
        essentializer = new Essentializer("essentializer", prop.getInt(), Material.iron, CreativeTabs.tabMisc)
        	.setHardness(15.0F).setStepSound(Block.soundMetalFootstep);
        
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
        String str1 = "IOI";
        String str2 = "ODO";
        String str3 = "IOI";
        GameRegistry.addRecipe(new ItemStack(essentializer, 1),
                str1, str2, str3,
                'I', Item.ingotIron,
                'O', Block.obsidian,
                'D', Item.diamond);
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

package gigaherz.elementsofpower;

import gigaherz.elementsofpower.client.ClientPacketHandler;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

@Mod(modid = "WorkerCommand", name = "WorkerCommand", version = "0.1.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
        clientPacketHandlerSpec = @SidedPacketHandler(channels = {"WorkerCommand" }, packetHandler = ClientPacketHandler.class),
        serverPacketHandlerSpec = @SidedPacketHandler(channels = {"WorkerCommand" }, packetHandler = ServerPacketHandler.class))
public class ElementsOfPower
{
    private final static int firstItemId = 24400;
    private final static int firstBlockId = 2450;

    private final static int defaultWorkerBlockId = firstBlockId + 1;
    private final static int defaultCommandCircuitId = firstItemId + 1;

    public static final Configuration Config = new Configuration(new File(Loader.instance().getConfigDir(), "WorkerCommand/WorkerCommand.cfg"));

    // Item templates
    private static CommandCircuit circuit;

    // Block templates
    public static Block worker;

    // Tier 1
    public static ItemStack planter;
    public static ItemStack harvester;
    public static ItemStack woodcutter;

    // Tier 2
    public static ItemStack fertilizer;
    public static ItemStack tiller;

    // Tier 3
    public static ItemStack miner;
    public static ItemStack filler;

    // The instance of your mod that Forge uses.
    @Instance("WorkerCommand")
    public static ElementsOfPower instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "gigaherz.workercommand.client.ClientProxy", serverSide = "gigaherz.workercommand.CommonProxy")
    public static CommonProxy proxy;

    private GuiHandler guiHandler = new GuiHandler();

    @PreInit
    public void preInit(FMLPreInitializationEvent event)
    {
        Config.load();
        Property prop;
        prop = Config.getItem("gigaherz.workercommand.CommandCircuit", defaultCommandCircuitId);
        circuit = new CommandCircuit(prop.getInt());
        prop = Config.getBlock("gigaherz.workercommand.Worker", defaultWorkerBlockId);
        worker = new Essentializer("worker", prop.getInt(), Material.iron, CreativeTabs.tabRedstone)
        .setHardness(0.5F).setStepSound(Block.soundMetalFootstep);
        planter = circuit.getStack(1, 1);
        harvester = circuit.getStack(1, 2);
        woodcutter = circuit.getStack(1, 3);
        fertilizer = circuit.getStack(1, 4);
        tiller = circuit.getStack(1, 5);
        miner = circuit.getStack(1, 6);
        filler = circuit.getStack(1, 7);
        Config.save();
    }

    @Init
    public void load(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        GameRegistry.registerTileEntity(EssentializerTile.class, "workerTile");
        MinecraftForge.setBlockHarvestLevel(worker, "pickaxe", 0);
        // Registration
        GameRegistry.registerBlock(worker, worker.getBlockName());
        LanguageRegistry.addName(worker, "Worker");
        // Tier 1
        LanguageRegistry.addName(planter, "Planter Command Circuit");
        LanguageRegistry.addName(harvester, "Harvester Command Circuit");
        LanguageRegistry.addName(woodcutter, "Woodcutter Command Circuit");
        // Tier 2
        LanguageRegistry.addName(fertilizer, "Fertilizer Command Circuit");
        LanguageRegistry.addName(tiller, "Tiller Command Circuit");
        // Tier 3
        LanguageRegistry.addName(miner, "Miner Command Circuit");
        LanguageRegistry.addName(filler, "Filler Command Circuit");
        
        // Recipes
        
        // Gui
        NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event)
    {
        // Stub Method
    }
}

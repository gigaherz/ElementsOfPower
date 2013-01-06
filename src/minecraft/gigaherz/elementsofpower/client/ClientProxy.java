package gigaherz.elementsofpower.client;

import cpw.mods.fml.client.FMLClientHandler;
import gigaherz.elementsofpower.CommonProxy;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy
{
	public static final StaffItemRenderer staffRenderer = new StaffItemRenderer();
	public static final WandItemRenderer wandRenderer = new WandItemRenderer();
	
    @Override
    public void registerRenderers()
    {
        MinecraftForgeClient.preloadTexture(ITEMS_PNG);
        MinecraftForgeClient.preloadTexture(BLOCK_PNG);
        MinecraftForgeClient.preloadTexture(STAFF_PNG);
        
        MinecraftForgeClient.registerItemRenderer(ElementsOfPower.magicStaff.itemID, staffRenderer);
        MinecraftForgeClient.registerItemRenderer(ElementsOfPower.magicWand.itemID, wandRenderer);
    }
}

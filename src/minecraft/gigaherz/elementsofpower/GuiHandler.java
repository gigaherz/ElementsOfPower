package gigaherz.elementsofpower;

import gigaherz.elementsofpower.client.EssentializerGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof EssentializerTile)
        {
            return new EssentializerContainer((EssentializerTile) tileEntity, player.inventory);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof EssentializerTile)
        {
            return new EssentializerGui(player.inventory, (EssentializerTile) tileEntity);
        }

        return null;
    }
}

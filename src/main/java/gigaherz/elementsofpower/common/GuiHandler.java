package gigaherz.elementsofpower.common;

import gigaherz.elementsofpower.analyzer.ItemAnalyzer;
import gigaherz.elementsofpower.analyzer.gui.ContainerAnalyzer;
import gigaherz.elementsofpower.analyzer.gui.GuiAnalyzer;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.essentializer.gui.ContainerEssentializer;
import gigaherz.elementsofpower.essentializer.gui.GuiEssentializer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    public static final int GUI_ESSENTIALIZER = 0;
    public static final int GUI_ANALYZER = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id)
        {
            case GUI_ESSENTIALIZER:
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

                if (tileEntity instanceof TileEssentializer)
                {
                    return new ContainerEssentializer((TileEssentializer) tileEntity, player.inventory);
                }
                break;
            case GUI_ANALYZER:
                if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemAnalyzer)
                    return new ContainerAnalyzer(player);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        switch (id)
        {
            case GUI_ESSENTIALIZER:
                TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

                if (tileEntity instanceof TileEssentializer)
                {
                    return new GuiEssentializer(player.inventory, (TileEssentializer) tileEntity);
                }
                break;
            case GUI_ANALYZER:
                if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof ItemAnalyzer)
                    return new GuiAnalyzer(player);
        }

        return null;
    }
}

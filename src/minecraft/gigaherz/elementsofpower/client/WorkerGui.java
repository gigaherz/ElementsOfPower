package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.EssentializerContainer;
import gigaherz.elementsofpower.EssentializerTile;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

public class WorkerGui extends GuiContainer
{
    protected EssentializerTile worker;

    public WorkerGui(InventoryPlayer playerInventory, EssentializerTile tileEntity)
    {
        super(new EssentializerContainer(tileEntity, playerInventory));
        this.worker = tileEntity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        fontRenderer.drawString("Worker", 60, 6, 4210752);
        //fontRenderer.drawString("Power: " + grinder.powerFlow + " J / T", 70, 60, 0xFFFFFF);
        fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2 , 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        int picture = mc.renderEngine.getTexture("/gigaherz/workercommand/worker.png");
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.renderEngine.bindTexture(picture);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        /*
        if (this.grinder.isPowered())
        {
            this.drawTexturedModalRect(x + 86, y + 54, 176, 0, 8, 8);
        }

        int progress = this.grinder.getProgressTimeScaled(24);
        this.drawTexturedModalRect(x + 79, y + 34, 176, 14, progress + 1, 16);
        */
    }
}
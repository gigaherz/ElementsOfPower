package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.blocks.ContainerEssentializer;
import gigaherz.elementsofpower.blocks.TileEssentializer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiEssentializer extends GuiContainer
{
    protected InventoryPlayer player;
    protected TileEssentializer tile;
    protected ResourceLocation guiTextureLocation;

    public GuiEssentializer(InventoryPlayer playerInventory, TileEssentializer tileEntity)
    {
        super(new ContainerEssentializer(tileEntity, playerInventory));
        this.player = playerInventory;
        this.tile = tileEntity;
        guiTextureLocation = new ResourceLocation(ElementsOfPower.MODID, "textures/gui/essentializer.png");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.tile.getCommandSenderName()), 8, 6, 0x404040);
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.player.getCommandSenderName()), 8, ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        mc.renderEngine.bindTexture(guiTextureLocation);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
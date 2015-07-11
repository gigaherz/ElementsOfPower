package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.blocks.ContainerEssentializer;
import gigaherz.elementsofpower.blocks.TileEssentializer;
import gigaherz.elementsofpower.slots.SlotMagic;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
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
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.tile.getName()), 8, 6, 0x404040);
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.player.getName()), 8, ySize - 96 + 2, 0x404040);
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

    /**
     * Called when the mouse is clicked.
     */
    @Override
    protected void mouseClicked(int x, int y, int button) throws java.io.IOException
    {
        if (button != 0 && button != 1)
        {
            super.mouseClicked(x, y, button);
            return;
        }

        Slot slot = null;

        for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i)
        {
            Slot s = (Slot) this.inventorySlots.inventorySlots.get(i);

            if (this.isPointInRegion(s.xDisplayPosition, s.yDisplayPosition, 16, 16, x, y))
            {
                if (s instanceof SlotMagic)
                {
                    slot = s;
                    break;
                }
            }
        }

        if (slot == null)
        {
            super.mouseClicked(x, y, button);
            return;
        }

        ContainerEssentializer container = (ContainerEssentializer) this.inventorySlots;
        container.clickedMagic(slot, button);
    }
}
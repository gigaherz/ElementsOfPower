package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.EssentializerContainer;
import gigaherz.elementsofpower.EssentializerTile;
import gigaherz.elementsofpower.slots.SlotMagic;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class EssentializerGui extends GuiContainer
{
    protected EssentializerTile tile;

    public EssentializerGui(InventoryPlayer playerInventory, EssentializerTile tileEntity)
    {
        super(new EssentializerContainer(tileEntity, playerInventory));
        this.tile = tileEntity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        fontRenderer.drawString("Essentializer", 8, 6, 4210752);
        fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2 , 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        int picture = mc.renderEngine.getTexture("/gigaherz/elementsofpower/essentializer.png");
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.renderEngine.bindTexture(picture);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawSlotInventory(Slot slot)
    {
        super.drawSlotInventory(slot);

        if (!(slot instanceof SlotMagic))
        {
            return;
        }

        int x = slot.xDisplayPosition;
        int y = slot.yDisplayPosition;
        ItemStack stack = slot.getStack();
        this.zLevel = 102.0F;
        itemRenderer.zLevel = 102.0F;
        int num = tile.getInputEssencesOfType(slot.slotNumber);

        if (num > 0)
        {
            String text = "" + num;
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            this.fontRenderer.drawStringWithShadow(text, x - 2, y - 2, 0xFFEE66);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        itemRenderer.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int x, int y, int button)
    {
        if (button != 0 && button != 1)
        {
            super.mouseClicked(x, y, button);
            return;
        }

        Slot slot = null;

        for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i)
        {
            Slot s = (Slot)this.inventorySlots.inventorySlots.get(i);

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

        EssentializerContainer container = (EssentializerContainer) this.inventorySlots;
        container.clickedMagic(slot, button);
    }
}
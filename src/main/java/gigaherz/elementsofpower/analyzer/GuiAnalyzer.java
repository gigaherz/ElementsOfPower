package gigaherz.elementsofpower.analyzer;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.items.ItemGemstone;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiAnalyzer extends GuiContainer
{
    EntityPlayer player;

    static final ResourceLocation guiTextureLocation = new ResourceLocation(ElementsOfPower.MODID, "textures/gui/analyzer.png");
    static final String guiTitle = "text." + ElementsOfPower.MODID + ".analyzer";

    public GuiAnalyzer(EntityPlayer playerInventory)
    {
        super(new ContainerAnalyzer(playerInventory));
        this.player = playerInventory;
        ySize = 176;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        mc.renderEngine.bindTexture(guiTextureLocation);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y)
    {
        String name = StatCollector.translateToLocal(guiTitle);
        mc.fontRendererObj.drawString(name, (xSize - mc.fontRendererObj.getStringWidth(name)) / 2, 6, 0x404040);
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.player.inventory.getName()), 8, ySize - 96 + 3, 0x404040);

        Slot slotAnalyze = inventorySlots.inventorySlots.get(0);
        ItemStack stack = slotAnalyze.getStack();
        if(stack != null)
        {
            Item item = stack.getItem();
            if (item instanceof ItemGemstone)
            {
                mc.fontRendererObj.drawString("Item: Gemstone", 32, 18, 0xffffff);

                ItemGemstone gemstone = (ItemGemstone)item;
                Gemstone gem = gemstone.getGemstone(stack);
                Quality q = gemstone.getQuality(stack);

                mc.fontRendererObj.drawString("Gemstone type: " + gem.toString(), 32, 30, 0xffffff);
                mc.fontRendererObj.drawString("Quality level: " + q.toString(), 32, 40, 0xffffff);
                mc.fontRendererObj.drawString("Effective Capacity:", 32, 50, 0xffffff);
                mc.fontRendererObj.drawString(gemstone.getCapacity(stack).toShortString(), 40, 60, 0xffffff);
            }
            else
            {
                mc.fontRendererObj.drawString("Item: " + stack.getDisplayName(), 32, 18, 0xffffff);

                mc.fontRendererObj.drawSplitString("Does not look like a useful gemstone.", 32, 30, 166-32, 0xffffff);
            }
        }
    }
}

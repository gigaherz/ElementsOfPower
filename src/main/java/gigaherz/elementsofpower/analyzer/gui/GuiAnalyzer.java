package gigaherz.elementsofpower.analyzer.gui;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.ItemGemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiAnalyzer extends GuiContainer
{
    private EntityPlayer player;

    public static final ResourceLocation GUI_TEXTURE_LOCATION = ElementsOfPower.location("textures/gui/analyzer.png");
    private static final String guiTitle = "text." + ElementsOfPower.MODID + ".analyzer";

    public GuiAnalyzer(EntityPlayer playerInventory)
    {
        super(new ContainerAnalyzer(playerInventory));
        this.player = playerInventory;
        ySize = 176;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        mc.renderEngine.bindTexture(GUI_TEXTURE_LOCATION);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y)
    {
        String name = I18n.format(guiTitle);
        mc.fontRenderer.drawString(name, (xSize - mc.fontRenderer.getStringWidth(name)) / 2, 6, 0x404040);
        mc.fontRenderer.drawString(I18n.format(this.player.inventory.getName()), 8, ySize - 96 + 3, 0x404040);

        Slot slotAnalyze = inventorySlots.inventorySlots.get(0);
        ItemStack stack = slotAnalyze.getStack();
        if (stack.getCount() > 0)
        {
            Gemstone gem = null;
            Quality q = null;
            MagicAmounts am = null;

            Item item = stack.getItem();
            if (item instanceof ItemGemstone)
            {
                mc.fontRenderer.drawString("Item: Gemstone", 32, 18, 0xffffff);

                ItemGemstone gemstone = (ItemGemstone) item;
                gem = gemstone.getGemstone(stack);
                q = gemstone.getQuality(stack);
                am = gemstone.getCapacity(stack);
            }

            if (gem != null)
            {
                mc.fontRenderer.drawString("Gemstone type: " + gem.toString(), 32, 30, 0xffffff);
                mc.fontRenderer.drawString("Quality level: " + (q != null ? q.toString() : "Unknown"), 32, 40, 0xffffff);
                if (!am.isEmpty())
                {
                    mc.fontRenderer.drawString("Effective Capacity:", 32, 50, 0xffffff);
                    mc.fontRenderer.drawString(am.toShortString(), 40, 60, 0xffffff);
                }
            }
            else
            {
                mc.fontRenderer.drawString("Item: " + stack.getDisplayName(), 32, 18, 0xffffff);

                mc.fontRenderer.drawSplitString("Does not look like a useful gemstone.", 32, 30, 166 - 32, 0xffffff);
            }
        }
    }
}

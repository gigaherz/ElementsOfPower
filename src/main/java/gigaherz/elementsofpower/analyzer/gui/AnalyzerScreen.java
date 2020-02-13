package gigaherz.elementsofpower.analyzer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class AnalyzerScreen extends ContainerScreen<AnalyzerContainer>
{
    private PlayerEntity player;

    public static final ResourceLocation GUI_TEXTURE_LOCATION = ElementsOfPowerMod.location("textures/gui/analyzer.png");

    public AnalyzerScreen(AnalyzerContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.player = playerInventory.player;
        ySize = 176;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        minecraft.textureManager.bindTexture(GUI_TEXTURE_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.blit(x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y)
    {
        String name = title.getFormattedText();
        font.drawString(name, (xSize - font.getStringWidth(name)) / 2, 6, 0x404040);
        font.drawString(this.player.inventory.getName().getFormattedText(), 8, ySize - 96 + 3, 0x404040);

        Slot slotAnalyze = container.inventorySlots.get(0);
        ItemStack stack = slotAnalyze.getStack();
        if (stack.getCount() > 0)
        {
            Gemstone gem = null;
            Quality q = null;
            MagicAmounts am = null;

            Item item = stack.getItem();
            if (item instanceof GemstoneItem)
            {
                font.drawString("Item: Gemstone", 32, 18, 0xffffff);

                GemstoneItem gemstone = (GemstoneItem) item;
                gem = gemstone.getGemstone();
                q = gemstone.getQuality(stack);

                am = MagicContainerCapability.getContainer(stack).map(IMagicContainer::getCapacity).orElse(MagicAmounts.EMPTY);
            }

            if (gem != null)
            {
                font.drawString("Gemstone type: " + gem.toString(), 32, 30, 0xffffff);
                font.drawString("Quality level: " + (q != null ? q.toString() : "Unknown"), 32, 40, 0xffffff);
                if (!am.isEmpty())
                {
                    font.drawString("Effective Capacity:", 32, 50, 0xffffff);
                    font.drawString(am.toShortString(), 40, 60, 0xffffff);
                }
            }
            else
            {
                font.drawString("Item: " + stack.getDisplayName(), 32, 18, 0xffffff);

                font.drawSplitString("Does not look like a useful gemstone.", 32, 30, 166 - 32, 0xffffff);
            }
        }
    }
}

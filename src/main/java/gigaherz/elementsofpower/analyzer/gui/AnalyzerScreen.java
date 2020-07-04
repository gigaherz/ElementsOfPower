package gigaherz.elementsofpower.analyzer.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.client.MagicTooltips;
import gigaherz.elementsofpower.magic.MagicAmounts;
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
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        minecraft.textureManager.bindTexture(GUI_TEXTURE_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.blit(matrixStack, x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        super.func_230451_b_(matrixStack, mouseX, mouseY);

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
                font.drawString(matrixStack, "Item: Gemstone", 32, 18, 0xffffff);

                GemstoneItem gemstone = (GemstoneItem) item;
                gem = gemstone.getGemstone();
                q = gemstone.getQuality(stack);

                am = MagicContainerCapability.getContainer(stack).map(IMagicContainer::getCapacity).orElse(MagicAmounts.EMPTY);
            }

            if (gem != null)
            {
                font.drawString(matrixStack, "Gemstone type: " + gem.toString(), 32, 30, 0xffffff);
                font.drawString(matrixStack, "Quality level: " + (q != null ? q.toString() : "Unknown"), 32, 40, 0xffffff);
                if (!am.isEmpty())
                {
                    font.drawString(matrixStack, "Effective Capacity:", 32, 50, 0xffffff);
                    font.drawString(matrixStack, String.format("%s, %s, %s, %s",
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(0)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(1)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(2)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(3))), 40, 60, 0xffffff);
                    font.drawString(matrixStack, String.format("%s, %s, %s, %s",
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(4)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(5)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(6)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(7))), 40, 70, 0xffffff);
                }
            }
            else
            {
                font.drawString(matrixStack, "Item: " + stack.getDisplayName(), 32, 18, 0xffffff);

                //TODO: font.drawSplitString(matrixStack,"Does not look like a useful gemstone.", 32, 30, 166 - 32, 0xffffff);
                font.drawString(matrixStack,"Does not look like a useful gemstone.", 32, 30, 0xffffff);
            }
        }
    }
}


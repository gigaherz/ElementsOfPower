package dev.gigaherz.elementsofpower.analyzer.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.client.MagicTooltips;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnalyzerScreen extends AbstractContainerScreen<AnalyzerContainer>
{

    public static final ResourceLocation GUI_TEXTURE_LOCATION = ElementsOfPowerMod.location("textures/gui/analyzer.png");

    public AnalyzerScreen(AnalyzerContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageHeight = 176;
        inventoryLabelY = 82;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderTexture(0, GUI_TEXTURE_LOCATION);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        this.blit(matrixStack, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
    {
        super.renderLabels(matrixStack, mouseX, mouseY);

        Slot slotAnalyze = menu.slots.get(0);
        ItemStack stack = slotAnalyze.getItem();
        if (stack.getCount() > 0)
        {
            Gemstone gem = null;
            Quality q = null;
            MagicAmounts am = null;

            Item item = stack.getItem();
            if (item instanceof GemstoneItem)
            {
                font.draw(matrixStack, "Item: Gemstone", 32, 18, 0xffffff);

                GemstoneItem gemstone = (GemstoneItem) item;
                gem = gemstone.getGemstone();
                q = gemstone.getQuality(stack);

                am = MagicContainerCapability.getContainer(stack).map(IMagicContainer::getCapacity).orElse(MagicAmounts.EMPTY);
            }

            if (gem != null)
            {
                font.draw(matrixStack, "Gemstone type: " + gem.toString(), 32, 30, 0xffffff);
                font.draw(matrixStack, "Quality level: " + (q != null ? q.toString() : "Unknown"), 32, 40, 0xffffff);
                if (!am.isEmpty())
                {
                    font.draw(matrixStack, "Effective Capacity:", 32, 50, 0xffffff);
                    font.draw(matrixStack, String.format("%s, %s, %s, %s",
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(0)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(1)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(2)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(3))), 40, 60, 0xffffff);
                    font.draw(matrixStack, String.format("%s, %s, %s, %s",
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(4)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(5)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(6)),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.get(7))), 40, 70, 0xffffff);
                }
            }
            else
            {
                font.draw(matrixStack, "Item: " + stack.getHoverName(), 32, 18, 0xffffff);

                //TODO: font.drawSplitString(matrixStack,"Does not look like a useful gemstone.", 32, 30, 166 - 32, 0xffffff);
                font.draw(matrixStack, "Does not look like a useful gemstone.", 32, 30, 0xffffff);
            }
        }
    }
}


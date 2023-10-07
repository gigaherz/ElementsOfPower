package dev.gigaherz.elementsofpower.analyzer.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.client.MagicTooltips;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnalyzerScreen extends AbstractContainerScreen<AnalyzerMenu>
{

    public static final ResourceLocation GUI_TEXTURE_LOCATION = ElementsOfPowerMod.location("textures/gui/analyzer.png");

    public AnalyzerScreen(AnalyzerMenu container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageHeight = 176;
        inventoryLabelY = 82;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(GUI_TEXTURE_LOCATION, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderLabels(graphics, mouseX, mouseY);

        Slot slotAnalyze = menu.slots.get(0);
        ItemStack stack = slotAnalyze.getItem();
        if (stack.getCount() > 0)
        {
            Gemstone gem = null;
            Quality q = null;
            MagicAmounts am = null;

            Item item = stack.getItem();

            graphics.drawString(font, Component.translatable("text.elementsofpower.analyzer.item", stack.getHoverName()), 32, 18, 0xffffff, false);

            if (item instanceof GemstoneItem gemstone)
            {
                gem = gemstone.getGemstone();
                q = gemstone.getQuality(stack);

                am = MagicContainerCapability.getContainer(stack).map(IMagicContainer::getCapacity).orElse(MagicAmounts.EMPTY);
            }

            if (gem != null)
            {
                graphics.drawString(font, Component.translatable("text.elementsofpower.analyzer.gem", Component.translatable("item.elementsofpower." + gem.getSerializedName())),
                        32, 30, 0xffffff, false);
                graphics.drawString(font, Component.translatable("text.elementsofpower.analyzer.quality", (q != null ? q.toString() : "Unknown")),
                        32, 40, 0xffffff, false);
                if (!am.isEmpty())
                {
                    graphics.drawString(font, Component.translatable("text.elementsofpower.analyzer.capacity"), 32, 50, 0xffffff, false);
                    graphics.drawString(font, String.format("%s, %s, %s, %s",
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.fire()),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.water()),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.air()),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.earth())), 40, 60, 0xffffff, false);
                    graphics.drawString(font, String.format("%s, %s, %s, %s",
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.light()),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.time()),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.life()),
                            MagicTooltips.PRETTY_NUMBER_FORMATTER.format(am.chaos())), 40, 70, 0xffffff, false);
                }
            }
            else
            {
                graphics.drawWordWrap(font, Component.translatable("text.elementsofpower.analyzer.not_a_gemstone"), 32, 30,  134,0xffffff);
            }
        }
    }
}


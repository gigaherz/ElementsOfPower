package dev.gigaherz.elementsofpower.essentializer.menu;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.MagicTooltips;
import dev.gigaherz.elementsofpower.client.StackRenderingHelper;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class EssentializerScreen extends AbstractContainerScreen<EssentializerMenu>
{
    public static final ResourceLocation GUI_TEXTURE_LOCATION = ElementsOfPowerMod.location("textures/gui/essentializer.png");


    protected Inventory player;

    public final static int[] MAGIC_ORBS = {
            50, 32,
            68, 16,
            92, 16,
            110, 32,
            50, 56,
            68, 72,
            92, 72,
            110, 56,
    };

    final static int[] TRANSFER_RECTS = {
            66, 39, 182, 8, 14, 10,
            75, 32, 177, 1, 10, 12,
            91, 32, 201, 1, 10, 12,
            96, 39, 192, 8, 14, 10,
            66, 55, 198, 8, 14, 10,
            75, 60, 201, 5, 10, 12,
            91, 60, 177, 5, 10, 12,
            96, 55, 176, 8, 14, 10,
    };

    final static float[] COLORS = {
            255 / 255.0f, 62 / 255.0f, 0 / 255.0f, // FF3E00
            0 / 255.0f, 93 / 255.0f, 255 / 255.0f, // 005DFF
            255 / 255.0f, 237 / 255.0f, 150 / 255.0f, // FFED96
            127 / 255.0f, 51 / 255.0f, 0 / 255.0f, // 7F3300
            255 / 255.0f, 255 / 255.0f, 255 / 255.0f, // FFFFFF
            0 / 255.0f, 0 / 255.0f, 0 / 255.0f, // 000000
            94 / 255.0f, 255 / 255.0f, 225 / 255.0f, // 5EFFE1
            66 / 255.0f, 0 / 255.0f, 0 / 255.0f, // 420000
    };

    public EssentializerScreen(EssentializerMenu container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        this.player = playerInventory;
        imageHeight = 176;
        this.inventoryLabelY = this.imageHeight - 94;
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

        Objects.requireNonNull(minecraft);

        RenderSystem.enableBlend();

        float opaqueLevel = EssentializerBlockEntity.MAX_CONVERT_PER_TICK * 20; // approx 3s fadeout

        MagicAmounts am = menu.getMagicHolder().getRemainingToConvert();
        if (!am.isEmpty())
        {
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                float alpha = (float) (0.9 + 0.1 * Math.sin(Math.PI * 8 * am.get(i) / opaqueLevel))
                        * Math.min(1, am.get(i) / opaqueLevel);

                float r = COLORS[i * 3];
                float g = COLORS[i * 3 + 1];
                float b = COLORS[i * 3 + 2];
                RenderSystem.setShaderColor(r, g, b, alpha);

                int x0 = TRANSFER_RECTS[i * 6];
                int y0 = TRANSFER_RECTS[i * 6 + 1];
                int x1 = TRANSFER_RECTS[i * 6 + 2];
                int y1 = TRANSFER_RECTS[i * 6 + 3];
                int sx = TRANSFER_RECTS[i * 6 + 4];
                int sy = TRANSFER_RECTS[i * 6 + 5];

                graphics.blit(GUI_TEXTURE_LOCATION, x0, y0, x1, y1, sx, sy);
            }
        }

        RenderSystem.depthMask(false);

        RenderSystem.disableBlend();

        am = menu.getMagicHolder().getContainedMagic();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int alpha = am.get(i) > 0 ? 0xFFFFFFFF : 0x3FFFFFFF;

            int x0 = MAGIC_ORBS[i * 2];
            int y0 = MAGIC_ORBS[i * 2 + 1];

            ItemStack stack = new ItemStack(Element.values[i].getOrb());

            //RenderSystem.setShaderColor(1, 1, 1, alpha / 255.0f);
            StackRenderingHelper.renderItemStack(minecraft.getItemRenderer(), graphics.pose(), stack, x0, y0, 0, alpha);
        }

        var poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.scale(1 / 1.5f, 1 / 1.5f, 1);
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int x0 = MAGIC_ORBS[i * 2];
            int y0 = MAGIC_ORBS[i * 2 + 1];

            float count = (int) am.get(i);

            if (count <= 0)
                continue;

            String formatted = formatQuantityWithSuffix(count);

            float x1 = (x0 + 16) * 1.5f - font.width(formatted);
            float y1 = (y0 + 10.5f) * 1.5f;

            graphics.drawString(font, formatted, x1, y1, 0xFFFFFFFF, true);
        }
        poseStack.popPose();

        drawOrbTooltips(graphics, mouseX, mouseY);

        RenderSystem.depthMask(true);
    }

    public static String formatQuantityWithSuffix(float count)
    {
        String suffix = "";
        if (count >= 1100 || count <= -1100)
        {
            suffix = "k";
            count /= 1000;
        }

        return MagicTooltips.PRETTY_NUMBER_FORMATTER.format(count) + suffix;
    }

    private void drawOrbTooltips(GuiGraphics graphics, int mx, int my)
    {
        int x0 = (width - imageWidth) / 2;
        int y0 = (height - imageHeight) / 2;

        MagicAmounts am = menu.getMagicHolder().getContainedMagic();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int x = MAGIC_ORBS[i * 2];
            int y = MAGIC_ORBS[i * 2 + 1];
            int rx = mx - x0 - x;
            int ry = my - y0 - y;

            if (rx < 0 || ry < 0 || rx > 16 || ry > 16)
                continue;

            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(MagicAmounts.getMagicName(i));
            tooltip.add(Component.literal(MagicTooltips.PRETTY_NUMBER_FORMATTER_2.format(am.get(i)) + " / " + EssentializerBlockEntity.MAX_ESSENTIALIZER_MAGIC).withStyle(ChatFormatting.GRAY));

            graphics.renderComponentTooltip(font, tooltip, mx - x0, my - y0);
        }
    }
}
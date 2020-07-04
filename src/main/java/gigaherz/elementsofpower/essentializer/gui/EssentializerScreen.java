package gigaherz.elementsofpower.essentializer.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.client.MagicTooltips;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.essentializer.EssentializerTileEntity;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Objects;

public class EssentializerScreen extends ContainerScreen<EssentializerContainer>
{
    public static final ResourceLocation GUI_TEXTURE_LOCATION = ElementsOfPowerMod.location("textures/gui/essentializer.png");


    protected PlayerInventory player;

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

    public EssentializerScreen(EssentializerContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        this.player = playerInventory;
        ySize = 176;
        this.field_238745_s_ = this.ySize - 94;
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
        Objects.requireNonNull(minecraft).textureManager.bindTexture(GUI_TEXTURE_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.blit(matrixStack, x, y, 0, 0, xSize, ySize);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY)
    {
        super.func_230451_b_(matrixStack, mouseX, mouseY);

        Objects.requireNonNull(minecraft);

        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();

        float opaqueLevel = EssentializerTileEntity.MAX_CONVERT_PER_TICK * 20; // approx 3s fadeout

        MagicAmounts am = container.getMagicHolder().getRemainingToConvert();
        if (!am.isEmpty())
        {
            Objects.requireNonNull(minecraft).textureManager.bindTexture(GUI_TEXTURE_LOCATION);
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                float alpha = (float) (0.9 + 0.1 * Math.sin(Math.PI * 8 * am.get(i) / opaqueLevel))
                        * Math.min(1, am.get(i) / opaqueLevel);

                float r = COLORS[i * 3];
                float g = COLORS[i * 3 + 1];
                float b = COLORS[i * 3 + 2];
                RenderSystem.color4f(r, g, b, alpha);

                int x0 = TRANSFER_RECTS[i * 6];
                int y0 = TRANSFER_RECTS[i * 6 + 1];
                int x1 = TRANSFER_RECTS[i * 6 + 2];
                int y1 = TRANSFER_RECTS[i * 6 + 3];
                int sx = TRANSFER_RECTS[i * 6 + 4];
                int sy = TRANSFER_RECTS[i * 6 + 5];

                this.blit(matrixStack, x0, y0, x1, y1, sx, sy);
            }
        }

        RenderSystem.depthMask(false);

        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();

        am = container.getMagicHolder().getContainedMagic();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int alpha = am.get(i) > 0 ? 0xFFFFFFFF : 0x3FFFFFFF;

            int x0 = MAGIC_ORBS[i * 2];
            int y0 = MAGIC_ORBS[i * 2 + 1];

            ItemStack stack = new ItemStack(Element.values[i].getOrb());

            RenderSystem.color4f(1, 1, 1, alpha / 255.0f);
            minecraft.getItemRenderer().renderItemAndEffectIntoGUI(stack, x0, y0);
        }

        RenderSystem.pushMatrix();
        RenderSystem.scaled(1 / 1.5, 1 / 1.5, 1);
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int x0 = MAGIC_ORBS[i * 2];
            int y0 = MAGIC_ORBS[i * 2 + 1];

            float count = (int) am.get(i);

            if (count <= 0)
                continue;

            String formatted = formatQuantityWithSuffix(count);

            float x1 = (x0 + 16) * 1.5f - font.getStringWidth(formatted);
            float y1 = (y0 + 10.5f) * 1.5f;

            font.drawStringWithShadow(matrixStack, formatted, x1, y1, 0xFFFFFFFF);
        }
        RenderSystem.popMatrix();

        drawOrbTooltips(matrixStack, mouseX, mouseY);

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

    private void drawOrbTooltips(MatrixStack matrixStack, int mx, int my)
    {
        int x0 = (width - xSize) / 2;
        int y0 = (height - ySize) / 2;

        MagicAmounts am = container.getMagicHolder().getContainedMagic();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int x = MAGIC_ORBS[i * 2];
            int y = MAGIC_ORBS[i * 2 + 1];
            int rx = mx - x0 - x;
            int ry = my - y0 - y;

            if (rx < 0 || ry < 0 || rx > 16 || ry > 16)
                continue;

            List<ITextProperties> tooltip = Lists.newArrayList();
            tooltip.add(MagicAmounts.getMagicName(i));
            tooltip.add(new StringTextComponent(MagicTooltips.PRETTY_NUMBER_FORMATTER_2.format(am.get(i)) + " / " + EssentializerTileEntity.MAX_ESSENTIALIZER_MAGIC).func_240699_a_(TextFormatting.GRAY));

            renderTooltip(matrixStack, tooltip, mx - x0, my - y0);
        }
    }
}
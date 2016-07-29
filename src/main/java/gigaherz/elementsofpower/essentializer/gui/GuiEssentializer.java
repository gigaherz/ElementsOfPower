package gigaherz.elementsofpower.essentializer.gui;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.renderers.StackRenderingHelper;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.gemstones.Element;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiEssentializer extends GuiContainer
{
    public static final ResourceLocation GUI_TEXTURE_LOCATION = ElementsOfPower.location("textures/gui/essentializer.png");

    protected InventoryPlayer player;
    protected TileEssentializer tile;

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

    public GuiEssentializer(InventoryPlayer playerInventory, TileEssentializer tileEntity)
    {
        super(new ContainerEssentializer(tileEntity, playerInventory));
        this.player = playerInventory;
        this.tile = tileEntity;
        ySize = 176;
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
        mc.fontRendererObj.drawString(I18n.format(this.tile.getName()), 8, 6, 0x404040);
        mc.fontRendererObj.drawString(I18n.format(this.player.getName()), 8, ySize - 96 + 3, 0x404040);

        float opaqueLevel = TileEssentializer.MaxConvertPerTick * 20; // approx 3s fadeout

        MagicAmounts am = tile.remainingToConvert;
        if (am != null)
        {
            mc.renderEngine.bindTexture(GUI_TEXTURE_LOCATION);
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                float alpha = (float) (0.9 + 0.1 * Math.sin(Math.PI * 8 * am.amounts[i] / opaqueLevel))
                        * Math.min(1, am.amounts[i] / opaqueLevel);

                float r = COLORS[i * 3];
                float g = COLORS[i * 3 + 1];
                float b = COLORS[i * 3 + 2];
                GlStateManager.color(r, g, b, alpha);

                int x0 = TRANSFER_RECTS[i * 6];
                int y0 = TRANSFER_RECTS[i * 6 + 1];
                int x1 = TRANSFER_RECTS[i * 6 + 2];
                int y1 = TRANSFER_RECTS[i * 6 + 3];
                int sx = TRANSFER_RECTS[i * 6 + 4];
                int sy = TRANSFER_RECTS[i * 6 + 5];

                this.drawTexturedModalRect(x0, y0, x1, y1, sx, sy);
            }
        }

        GlStateManager.depthMask(false);

        ItemModelMesher mesher = mc.getRenderItem().getItemModelMesher();

        am = tile.containedMagic;
        if (am == null)
            am = new MagicAmounts();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int alpha = am.amounts[i] > 0 ? 0xFFFFFFFF : 0x3FFFFFFF;

            int x0 = MAGIC_ORBS[i * 2];
            int y0 = MAGIC_ORBS[i * 2 + 1];

            ItemStack stack = ElementsOfPower.magicOrb.getStack((int) am.amounts[i], Element.values[i]);

            StackRenderingHelper.renderItemStack(mesher, mc.renderEngine, x0, y0, stack, alpha);
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(1 / 1.5, 1 / 1.5, 1);
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int x0 = MAGIC_ORBS[i * 2];
            int y0 = MAGIC_ORBS[i * 2 + 1];

            float count = (int) am.amounts[i];
            String suffix = "";
            if (count >= 900)
            {
                suffix = "k";
                count /= 1000;
            }

            String formatted = ElementsOfPower.prettyNumberFormatter.format(count) + suffix;

            float x1 = (x0 + 16) * 1.5f - mc.fontRendererObj.getStringWidth(formatted);
            float y1 = (y0 + 10.5f) * 1.5f;

            mc.fontRendererObj.drawString(formatted, x1, y1, 0xFFFFFFFF, true);
        }
        GlStateManager.popMatrix();

        drawOrbTooltips(x, y);

        GlStateManager.depthMask(true);
    }

    private void drawOrbTooltips(int mx, int my)
    {
        int x0 = (width - xSize) / 2;
        int y0 = (height - ySize) / 2;

        MagicAmounts am = tile.containedMagic;
        if (am == null)
            am = new MagicAmounts();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int x = MAGIC_ORBS[i * 2];
            int y = MAGIC_ORBS[i * 2 + 1];
            int rx = mx - x0 - x;
            int ry = my - y0 - y;

            if (rx < 0 || ry < 0 || rx > 16 || ry > 16)
                continue;

            List<String> tooltip = Lists.newArrayList();
            tooltip.add(MagicAmounts.getMagicName(i));
            tooltip.add(TextFormatting.GRAY + ElementsOfPower.prettyNumberFormatter2.format(am.amounts[i]) + " / " + TileEssentializer.MaxEssentializerMagic);

            drawHoveringText(tooltip, mx - x0, my - y0);
        }
    }
}
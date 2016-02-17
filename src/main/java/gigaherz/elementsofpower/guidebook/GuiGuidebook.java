package gigaherz.elementsofpower.guidebook;

import gigaherz.elementsofpower.renders.RenderingStuffs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiGuidebook extends GuiScreen
{
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");

    private static final int bookWidth = 248;
    private static final int bookHeight = 136;
    private static final int innerMargin = 16;
    private static final int outerMargin = 8;
    private static final int pageWidth = bookWidth/2 - innerMargin - outerMargin;
    private static final int pageHeight = bookHeight;

    private GuiButton buttonDone;
    private GuiButton buttonNextPage;
    private GuiButton buttonPreviousPage;

    private int currentPage = 0;
    private static final int pageCount = 10;

    @Override
    public void initGui()
    {
        this.buttonList.clear();


        int cx = this.width/2;
        int cy = this.height/2 - 10 + bookHeight/2;
        this.buttonList.add(this.buttonNextPage = new NextPageButton(1, cx + bookWidth /2 - 23, cy, true));
        this.buttonList.add(this.buttonPreviousPage = new NextPageButton(2, cx - bookWidth /2, cy, false));

        this.buttonList.add(this.buttonDone = new GuiButton(0, 0, this.height - 20, 80, 20, I18n.format("gui.done")));

        updateButtonStates();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == buttonDone.id)
            {
                this.mc.displayGuiScreen(null);
            }
            else if (button.id == buttonNextPage.id)
            {
                if (currentPage + 1 < pageCount)
                    currentPage++;
            }
            else if (button.id == buttonPreviousPage.id)
            {
                if (currentPage > 0)
                    currentPage--;
            }

            updateButtonStates();
        }
    }

    private void updateButtonStates()
    {
        buttonNextPage.enabled = currentPage + 1 < pageCount;
        buttonPreviousPage.enabled = currentPage > 0;

        buttonNextPage.visible = buttonNextPage.enabled;
        buttonPreviousPage.visible = buttonPreviousPage.enabled;
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackgroundModel();

        drawCurrentPages();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCurrentPages()
    {
        int cx = this.width/2;
        int cy = this.height/2 - 10;

        int top = cy - 16 - pageHeight/2;
        {
            int left = cx - pageWidth - innerMargin;

            drawPage(left, top, currentPage*2);

            fontRendererObj.drawString("" + (currentPage+1) + "/" + pageCount, left,top+pageHeight + 4, 0xFF000000);
        }

        {
            int left = cx + innerMargin;

            drawPage(left, top, currentPage*2 + 1);
        }
    }

    private void drawPage(int left, int top, int page)
    {
        if (page == 0)
        {
            top += addStringWrapping(left, top, "This book is a work in progress.", 0xFF606060);
        }
        else if (page == 1)
        {
            top += pageHeight/3;
            top += addStringWrapping(left, top, "Welcome to");
            top += addStringWrapping(left, top, "Elements of Power");
        }
        else if (page == 2)
        {
            top += addStringWrapping(left, top,
                    "In front of you is the power of raw elemental magic. " +
                    "In the world exist the following elements: "
            );
            top += 2;
            top += addStringWrapping(left + 6, top, "* Fire");
            top += addStringWrapping(left + 6, top, "* Water");
            top += addStringWrapping(left + 6, top, "* Air");
            top += addStringWrapping(left + 6, top, "* Earth");
            top += addStringWrapping(left + 6, top, "* Light");
            top += addStringWrapping(left + 6, top, "* Darkness");
            top += addStringWrapping(left + 6, top, "* Life");
            top += addStringWrapping(left + 6, top, "* Death");
        }
        else if (page == 3)
        {
            top += addStringWrapping(left, top,
                    "Each element has an associated effect and shape."
            );
        }
    }

    private int addStringWrapping(int left, int top, String s)
    {
        return addStringWrapping(left, top, s, 0xFF000000);
    }

    private int addStringWrapping(int left, int top, String s, int color)
    {
        fontRendererObj.drawSplitString(s, left, top, pageWidth,color);
        return fontRendererObj.splitStringWidth(s, pageWidth);
    }

    private void drawBackgroundModel()
    {
        IFlexibleBakedModel modelBook = RenderingStuffs.loadModel("elementsofpower:gui/book.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();

        GlStateManager.pushMatrix();

        GlStateManager.translate(this.width / 2, this.height / 2 - 10, 50);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(-130, 1, 0, 0);
        GlStateManager.scale(2.0f, 2.0f, 2.5f);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        RenderHelper.enableStandardItemLighting();

        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        renderModel(modelBook);

        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();

        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
    }

    public static void renderModel(IFlexibleBakedModel model)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());
        for (BakedQuad quad : model.getGeneralQuads())
        {
            worldrenderer.addVertexData(quad.getVertexData());
        }
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    static class NextPageButton extends GuiButton
    {
        private final boolean field_146151_o;

        public NextPageButton(int p_i46316_1_, int p_i46316_2_, int p_i46316_3_, boolean p_i46316_4_)
        {
            super(p_i46316_1_, p_i46316_2_, p_i46316_3_, 23, 13, "");
            this.field_146151_o = p_i46316_4_;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(bookGuiTextures);
                int i = 0;
                int j = 192;

                if (flag)
                {
                    i += 23;
                }

                if (!this.field_146151_o)
                {
                    j += 13;
                }

                this.drawTexturedModalRect(this.xPosition, this.yPosition, i, j, 23, 13);
            }
        }
    }
}

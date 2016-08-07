package gigaherz.elementsofpower.guidebook;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.renderers.ModelHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuiGuidebook extends GuiScreen
{
    private static final ResourceLocation BOOK_GUI_TEXTURES = new ResourceLocation("elementsofpower:textures/gui/book.png");
    public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("elementsofpower:xml/guidebook.xml");

    private GuiButton buttonClose;
    private GuiButton buttonNextPage;
    private GuiButton buttonPreviousPage;
    private GuiButton buttonNextChapter;
    private GuiButton buttonPreviousChapter;
    private GuiButton buttonBack;

    private static float angleSpeed = (1 / 0.35f) / 20;
    private float angleT = 1;

    private boolean closing = false;

    ModelHandle book00 = ModelHandle.of("elementsofpower:gui/book.obj").vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    ModelHandle book30 = ModelHandle.of("elementsofpower:gui/book30.obj").vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    ModelHandle book60 = ModelHandle.of("elementsofpower:gui/book60.obj").vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
    ModelHandle book90 = ModelHandle.of("elementsofpower:gui/book90.obj").vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

    private ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
    private TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

    private BookRenderer book;

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();

        int btnId = 0;

        int left = (this.width - BookRenderer.BOOK_WIDTH) / 2;
        int right = left + BookRenderer.BOOK_WIDTH;
        int top = (this.height - BookRenderer.BOOK_HEIGHT) / 2 - 9;
        int bottom = top + BookRenderer.BOOK_HEIGHT;
        this.buttonList.add(this.buttonBack = new SpriteButton(btnId++, left - 9, top - 5, 2));
        this.buttonList.add(this.buttonClose = new SpriteButton(btnId++, right - 6, top - 6, 3));
        this.buttonList.add(this.buttonPreviousPage = new SpriteButton(btnId++, left + 24, bottom - 13, 1));
        this.buttonList.add(this.buttonNextPage = new SpriteButton(btnId++, right - 42, bottom - 13, 0));
        this.buttonList.add(this.buttonPreviousChapter = new SpriteButton(btnId++, left + 2, bottom - 13, 5));
        this.buttonList.add(this.buttonNextChapter = new SpriteButton(btnId++, right - 23, bottom - 13, 4));
        ElementsOfPower.logger.info("Showing gui with " + btnId + " buttons.");

        updateButtonStates();

        book = new BookRenderer(BOOK_LOCATION, this).parseBook();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == buttonClose.id)
            {
                closing = true;
            }
            else if (button.id == buttonBack.id)
            {
                book.navigateBack();
            }
            else if (button.id == buttonNextPage.id)
            {
                book.nextPage();
            }
            else if (button.id == buttonPreviousPage.id)
            {
                book.prevPage();
            }
            else if (button.id == buttonNextChapter.id)
            {
                book.nextChapter();
            }
            else if (button.id == buttonPreviousChapter.id)
            {
                book.prevChapter();
            }

            updateButtonStates();
        }
    }

    private void updateButtonStates()
    {
        buttonClose.enabled = angleT == 0;
        buttonBack.enabled = angleT == 0 && book.canGoBack();
        buttonNextPage.enabled = angleT == 0 && book.canGoNextPage();
        buttonPreviousPage.enabled = angleT == 0 && book.canGoPrevPage();
        buttonNextChapter.enabled = angleT == 0 && book.canGoNextChapter();
        buttonPreviousChapter.enabled = angleT == 0 && book.canGoPrevChapter();

        buttonClose.visible = buttonClose.enabled;
        buttonBack.visible = buttonBack.enabled;
        buttonNextPage.visible = buttonNextPage.enabled;
        buttonPreviousPage.visible = buttonPreviousPage.enabled;
        buttonNextChapter.visible = buttonNextChapter.enabled;
        buttonPreviousChapter.visible = buttonPreviousChapter.enabled;
    }

    @Override
    public void updateScreen()
    {
        if (closing)
        {
            angleT += angleSpeed;
            if (angleT >= 1)
            {
                this.mc.displayGuiScreen(null);
            }
        }
        else if (angleT > 0)
        {
            angleT = Math.max(0, angleT - angleSpeed);
        }

        updateButtonStates();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_ESCAPE)
        {
            closing = true;
            return;
        }
        else if (keyCode == Keyboard.KEY_BACK)
        {
            book.navigateBack();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackgroundModel(partialTicks);

        if (angleT <= 0)
        {
            book.drawCurrentPages();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (book.mouseClicked(mouseX, mouseY, mouseButton))
            return;

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void drawBackgroundModel(float partialTicks)
    {
        IBakedModel modelBookA, modelBookB;

        float angleX;

        if (closing)
            angleX = (angleT + partialTicks * angleSpeed) * 90;
        else
            angleX = (angleT - partialTicks * angleSpeed) * 90;

        float blend;
        if (angleX <= 0)
        {
            angleX = 0;
            modelBookA = book00.get();
            modelBookB = null;
            blend = 0;
        }
        else if (angleX < 30)
        {
            modelBookA = book00.get();
            modelBookB = book30.get();
            blend = (angleX) / 30.0f;
        }
        else if (angleX < 60)
        {
            modelBookA = book30.get();
            modelBookB = book60.get();
            blend = (angleX - 30) / 30.0f;
        }
        else if (angleX < 90)
        {
            modelBookA = book60.get();
            modelBookB = book90.get();
            blend = (angleX - 60) / 30.0f;
        }
        else
        {
            angleX = 90;
            modelBookA = book90.get();
            modelBookB = null;
            blend = 0;
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();

        GlStateManager.pushMatrix();

        GlStateManager.translate(this.width * 0.5 * (1 + angleX / 130.0f), this.height * 0.5 * (1 + angleX / 110.0f) + BookRenderer.BOOK_HEIGHT / 2 - 4, 50);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(-130, 1, 0, 0);
        GlStateManager.scale(2.0f, 2.0f, 2.5f);
        GlStateManager.scale(1.08f, 1.08f, 1.08f);

        GlStateManager.rotate(angleX * 1.1f, 0, 0, 1);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        RenderHelper.enableStandardItemLighting();

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        if (modelBookB != null)
        {
            renderModelInterpolate(modelBookA, modelBookB, blend);
        }
        else
        {
            renderModel(modelBookA);
        }

        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();

        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
    }

    public static void renderModel(IBakedModel model)
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (BakedQuad quad : model.getQuads(null, null, 0))
        {
            worldrenderer.addVertexData(quad.getVertexData());
        }
        tessellator.draw();
    }

    public static void renderModelInterpolate(IBakedModel modelA, IBakedModel modelB, float blend)
    {
        VertexFormat fmt = DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(GL11.GL_QUADS, fmt);
        List<BakedQuad> generalQuadsA = modelA.getQuads(null, null, 0);
        List<BakedQuad> generalQuadsB = modelB.getQuads(null, null, 0);

        int length = fmt.getNextOffset();

        for (int i = 0; i < generalQuadsA.size(); i++)
        {
            BakedQuad quadA = generalQuadsA.get(i);
            BakedQuad quadB = generalQuadsB.get(i);

            int[] dataA = quadA.getVertexData();
            int[] dataB = quadB.getVertexData();

            int[] blended = Arrays.copyOf(dataA, dataA.length);

            for (int j = 0; j < 4; j++)
            {
                int o = (length / 4) * j;
                for (int k = 0; k < 3; k++)
                {
                    float ax = Float.intBitsToFloat(dataA[o + k]);
                    float bx = Float.intBitsToFloat(dataB[o + k]);
                    blended[o + k] = Float.floatToRawIntBits(ax + blend * (bx - ax));
                }
            }

            worldrenderer.addVertexData(blended);
        }
        tessellator.draw();
    }

    public FontRenderer getFontRenderer()
    {
        return fontRendererObj;
    }

    public ItemModelMesher getMesher()
    {
        return mesher;
    }

    public TextureManager getRenderEngine()
    {
        return renderEngine;
    }

    @SideOnly(Side.CLIENT)
    static class SpriteButton extends GuiButton
    {
        private final int whichIcon;

        private static final int[] xPixel = {5, 5, 4, 4, 4, 4};
        private static final int[] yPixel = {2, 16, 30, 64, 79, 93};
        private static final int[] xSize = {17, 17, 18, 13, 21, 21};
        private static final int[] ySize = {11, 11, 11, 13, 11, 11};

        public SpriteButton(int buttonId, int x, int y, int back)
        {
            super(buttonId, x, y, xSize[back], ySize[back], "");
            this.whichIcon = back;
        }

        /**
         * Draws this button to the screen.
         */
        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                boolean hover =
                        mouseX >= this.xPosition &&
                                mouseY >= this.yPosition &&
                                mouseX < this.xPosition + this.width &&
                                mouseY < this.yPosition + this.height;

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(BOOK_GUI_TEXTURES);
                int x = xPixel[whichIcon];
                int y = yPixel[whichIcon];
                int w = xSize[whichIcon];
                int h = ySize[whichIcon];

                if (hover)
                {
                    x += 25;
                }

                this.drawTexturedModalRect(this.xPosition, this.yPosition, x, y, w, h);
            }
        }
    }
}

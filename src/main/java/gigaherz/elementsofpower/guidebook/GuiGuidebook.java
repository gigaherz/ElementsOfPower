package gigaherz.elementsofpower.guidebook;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.renders.RenderingStuffs;
import gigaherz.elementsofpower.renders.StackRenderingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuiGuidebook extends GuiScreen
{
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");

    private static final int bookWidth = 276;
    private static final int bookHeight = 182;
    private static final int innerMargin = 22;
    private static final int outerMargin = 10;
    private static final int verticalMargin = 18;
    private static final int pageWidth = bookWidth/2 - innerMargin - outerMargin;
    private static final int pageHeight = bookHeight - verticalMargin;

    private GuiButton buttonDone;
    private GuiButton buttonNextPage;
    private GuiButton buttonPreviousPage;

    private int currentPage = 0;
    private static final int pageCount = 10;

    private static float angleSpeed = (1/0.35f) / 20;
    private float angleT = 1;

    boolean closing = false;

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();


        int cx = this.width/2;
        int cy = this.height/2 - 24 + bookHeight/2;
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
                closing = true;
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
        buttonNextPage.enabled = angleT == 0 && currentPage + 1 < pageCount;
        buttonPreviousPage.enabled = angleT == 0 && currentPage > 0;

        buttonNextPage.visible = buttonNextPage.enabled;
        buttonPreviousPage.visible = buttonPreviousPage.enabled;
    }

    @Override
    public void updateScreen()
    {
        if(closing)
        {
            angleT += angleSpeed;
            if(angleT >= 1)
            {
                this.mc.displayGuiScreen(null);
            }
        }
        else if(angleT > 0)
        {
            angleT = Math.max(0, angleT - angleSpeed);
        }

        updateButtonStates();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if(keyCode == Keyboard.KEY_ESCAPE)
        {
            closing = true;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackgroundModel(partialTicks);

        if(angleT <= 0)
        {
            drawCurrentPages();
        }

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

            String cnt = "" + (currentPage+1) + "/" + pageCount;
            int w = fontRendererObj.getStringWidth(cnt);
            fontRendererObj.drawString(cnt, left+(pageWidth-w)/2 ,top+pageHeight + 15, 0xFF000000);
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
            top += addStringWrapping(left, top, "This book is a work in progress.", 0xFF606060, 0);
        }
        else if (page == 1)
        {
            top += pageHeight/3;
            top += addStringWrappingCenter(left, top, "The Book");
            top += addStringWrappingCenter(left, top, "of");
            top += addStringWrappingCenter(left, top, "Elements");
            top += 35;
            top += addStringWrappingCenter(left, top, "by Gigaherz");
        }
        else if (page == 2)
        {
            top += addStringWrapping(left, top,
                    "In front of you is the power of raw elemental magic. " +
                    "In the world exist the following elements: "
            );

            int top_icons = top + 3;
            int spacing_icons = fontRendererObj.FONT_HEIGHT + 4;

            top += 8;
            top += 4 + addStringWrapping(left + 22, top, "Fire");
            top += 4 + addStringWrapping(left + 22, top, "Water");
            top += 4 + addStringWrapping(left + 22, top, "Air");
            top += 4 + addStringWrapping(left + 22, top, "Earth");
            top += 4 + addStringWrapping(left + 22, top, "Light");
            top += 4 + addStringWrapping(left + 22, top, "Darkness");
            top += 4 + addStringWrapping(left + 22, top, "Life");
            top += 4 + addStringWrapping(left + 22, top, "Death");

            ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
            TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                ItemStack stack = ElementsOfPower.magicOrb.getStack(1, i);

                StackRenderingHelper.renderItemStack(mesher, renderEngine, left + 4, top_icons + spacing_icons * i, stack, 0xFFFFFFFF, true);
            }

        }
        else if (page == 3)
        {
            top += addStringWrapping(left, top,
                    "Each element has an associated effect and shape. " +
                            "For example, Fire causes Flames, in the shape of a Sphere around caster."
            );
            top += fontRendererObj.FONT_HEIGHT/2;
            top += addStringWrapping(left, top,
                    "Spells take on the effect of the first element of the sequence. " +
                            "Subsequent elements of the same type will increase the power."
            );
            top += fontRendererObj.FONT_HEIGHT/2;
            top += addStringWrapping(left, top,
                    "The shape of the spell will depend on the last element in the sequence."
            );
        }
        else if (page == 4)
        {
            top += addStringWrapping(left, top,
                    "TBC..."
            );
        }
    }

    private int addStringWrapping(int left, int top, String s)
    {
        return addStringWrapping(left, top, s, 0xFF000000, 0);
    }

    private int addStringWrappingCenter(int left, int top, String s)
    {
        return addStringWrapping(left, top, s, 0xFF000000, 1);
    }

    private int addStringWrappingRight(int left, int top, String s)
    {
        return addStringWrapping(left, top, s, 0xFF000000, 2);
    }

    private int addStringWrapping(int left, int top, String s, int color, int align)
    {
        if (align == 1)
        {
            left += (pageWidth - fontRendererObj.getStringWidth(s))/2;
        }
        else if(align == 2)
        {
            left += pageWidth - fontRendererObj.getStringWidth(s);
        }

        fontRendererObj.drawSplitString(s, left, top, pageWidth,color);
        return fontRendererObj.splitStringWidth(s, pageWidth);
    }

    private void drawBackgroundModel(float partialTicks)
    {
        IFlexibleBakedModel modelBookA, modelBookB;

        float angleX;

        if (closing)
            angleX = (angleT + partialTicks * angleSpeed) * 90;
        else
            angleX = (angleT - partialTicks * angleSpeed) * 90;

        float blend = 0;
        if(angleX <= 0)
        {
            angleX = 0;
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = null;
            blend = 0;
        }
        else if(angleX < 30)
        {
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = RenderingStuffs.loadModel("elementsofpower:gui/book30.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            blend = (angleX)/30.0f;

        }
        else if(angleX < 60)
        {
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book30.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = RenderingStuffs.loadModel("elementsofpower:gui/book60.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            blend = (angleX-30)/30.0f;
        }
        else if(angleX < 90)
        {
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book60.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = RenderingStuffs.loadModel("elementsofpower:gui/book90.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            blend = (angleX-60)/30.0f;
        }
        else
        {
            angleX = 90;
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book90.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = null;
            blend = 0;
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();

        GlStateManager.pushMatrix();

        GlStateManager.translate(this.width * 0.5 * (1 + angleX / 130.0f), this.height * 0.5 * (1 + angleX / 110.0f) + bookHeight/2 - 4, 50);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(-130, 1, 0, 0);
        GlStateManager.scale(2.0f, 2.0f, 2.5f);
        GlStateManager.scale(1.08f, 1.08f, 1.08f);

        GlStateManager.rotate(angleX * 1.1f, 0, 0, 1);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        RenderHelper.enableStandardItemLighting();

        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        if(modelBookB != null)
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

    public static void renderModelInterpolate(IFlexibleBakedModel modelA, IFlexibleBakedModel modelB, float blend)
    {
        VertexFormat fmt = modelA.getFormat();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, fmt);
        List<BakedQuad> generalQuadsA = modelA.getGeneralQuads();
        List<BakedQuad> generalQuadsB = modelB.getGeneralQuads();

        int length = fmt.getNextOffset();

        for (int i = 0; i < generalQuadsA.size(); i++)
        {
            BakedQuad quadA = generalQuadsA.get(i);
            BakedQuad quadB = generalQuadsB.get(i);

            int[] dataA = quadA.getVertexData();
            int[] dataB = quadB.getVertexData();

            int[] blended = Arrays.copyOf(dataA, dataA.length);

            for(int j = 0; j < 4; j++)
            {
                int o = (length/4) * j;
                for(int k = 0; k < 3; k++)
                {
                    float ax = Float.intBitsToFloat(dataA[o+k]);
                    float bx = Float.intBitsToFloat(dataB[o+k]);
                    blended[o+k] = Float.floatToRawIntBits(ax + blend * (bx-ax));
                }
            }

            worldrenderer.addVertexData(blended);
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

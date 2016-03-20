package gigaherz.elementsofpower.guidebook;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.renders.RenderingStuffs;
import gigaherz.elementsofpower.renders.StackRenderingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GuiGuidebook extends GuiScreen
{
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("elementsofpower:textures/gui/book.png");

    private static final int bookWidth = 276;
    private static final int bookHeight = 198;
    private static final int innerMargin = 22;
    private static final int outerMargin = 10;
    private static final int verticalMargin = 18;
    private static final int pageWidth = bookWidth / 2 - innerMargin - outerMargin;
    private static final int pageHeight = bookHeight - verticalMargin;

    private GuiButton buttonClose;
    private GuiButton buttonNextPage;
    private GuiButton buttonPreviousPage;
    private GuiButton buttonNextChapter;
    private GuiButton buttonPreviousChapter;
    private GuiButton buttonBack;

    private int currentChapter = 0;
    private int currentPair = 0;
    private int totalPairs = 0;

    private static float angleSpeed = (1 / 0.35f) / 20;
    private float angleT = 1;

    private boolean closing = false;

    private ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
    private TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

    private ResourceLocation bookLocation = new ResourceLocation("elementsofpower:xml/guidebook.xml");

    private List<ChapterData> chapters = Lists.newArrayList();

    private Map<String, Integer> chaptersByName = Maps.newHashMap();
    private Map<String, PageRef> pagesByName = Maps.newHashMap();

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

        int left = (this.width - bookWidth) / 2;
        int right = left + bookWidth;
        int top = (this.height - bookHeight) / 2 - 9;
        int bottom = top + bookHeight;
        this.buttonList.add(this.buttonBack = new SpriteButton(btnId++, left - 9, top - 5, 2));
        this.buttonList.add(this.buttonClose = new SpriteButton(btnId++, right - 6, top - 6, 3));
        this.buttonList.add(this.buttonPreviousPage = new SpriteButton(btnId++, left + 24, bottom - 13, 1));
        this.buttonList.add(this.buttonNextPage = new SpriteButton(btnId++, right - 42, bottom - 13, 0));
        this.buttonList.add(this.buttonPreviousChapter = new SpriteButton(btnId++, left + 2, bottom - 13, 5));
        this.buttonList.add(this.buttonNextChapter = new SpriteButton(btnId++, right - 23, bottom - 13, 4));
        ElementsOfPower.logger.info("Showing gui with " + btnId + " buttons.");

        updateButtonStates();

        new BookHelper().parseBook();
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
                navigateBack();
            }
            else if (button.id == buttonNextPage.id)
            {
                if (currentPair + 1 < chapters.get(currentChapter).pagePairs)
                {
                    pushHistory();
                    currentPair++;
                }
                else if (currentChapter + 1 < chapters.size())
                {
                    pushHistory();
                    currentPair = 0;
                    currentChapter++;
                }
            }
            else if (button.id == buttonPreviousPage.id)
            {
                if (currentPair > 0)
                {
                    pushHistory();
                    currentPair--;
                }
                else if (currentChapter > 0)
                {
                    pushHistory();
                    currentChapter--;
                    currentPair = chapters.get(currentChapter).pagePairs - 1;
                }
            }
            else if (button.id == buttonNextChapter.id)
            {
                if (currentChapter + 1 < chapters.size())
                {
                    pushHistory();
                    currentPair = 0;
                    currentChapter++;
                }
            }
            else if (button.id == buttonPreviousChapter.id)
            {
                if (currentChapter > 0)
                {
                    pushHistory();
                    currentPair = 0;
                    currentChapter--;
                }
            }

            updateButtonStates();
        }
    }

    private void updateButtonStates()
    {
        buttonClose.enabled = angleT == 0;
        buttonBack.enabled = angleT == 0 &&
                (currentPair > 0 || currentChapter > 0);
        buttonNextPage.enabled = angleT == 0 &&
                (currentPair + 1 < chapters.get(currentChapter).pagePairs || currentChapter + 1 < chapters.size());
        buttonPreviousPage.enabled = angleT == 0 &&
                (currentPair > 0 || currentChapter > 0);
        buttonNextChapter.enabled = angleT == 0 &&
                (currentChapter + 1 < chapters.size());
        buttonPreviousChapter.enabled = angleT == 0 &&
                (currentChapter > 0);

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
            navigateBack();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    java.util.Stack<PageRef> history = new java.util.Stack<>();

    void navigateTo(final PageRef target)
    {
        pushHistory();

        target.resolve();
        currentChapter = Math.max(0, Math.min(chapters.size() - 1, target.chapter));
        currentPair = Math.max(0, Math.min(chapters.get(currentChapter).pagePairs - 1, target.page / 2));
    }

    private void pushHistory()
    {
        history.push(new PageRef(currentChapter, currentPair * 2));
    }

    void navigateBack()
    {
        if (history.size() > 0)
        {
            PageRef target = history.pop();
            target.resolve();
            currentChapter = target.chapter;
            currentPair = target.page / 2;
        }
        else
        {
            currentChapter = 0;
            currentPair = 0;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackgroundModel(partialTicks);

        if (angleT <= 0)
        {
            drawCurrentPages();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            int mX = mouseX;
            int mY = mouseY;

            ChapterData ch = chapters.get(currentChapter);
            PageData pg = ch.pages.get(currentPair * 2);
            for (IPageElement e : pg.elements)
            {
                if (e instanceof Link)
                {
                    Link l = (Link) e;
                    Rectangle b = l.getBounds();
                    if (mX >= b.getX() && mX <= (b.getX() + b.getWidth()) &&
                            mY >= b.getY() && mY <= (b.getY() + b.getHeight()))
                    {
                        l.click();
                        return;
                    }
                }
            }

            if (currentPair * 2 + 1 < ch.pages.size())
            {
                pg = ch.pages.get(currentPair * 2 + 1);
                for (IPageElement e : pg.elements)
                {
                    if (e instanceof Link)
                    {
                        Link l = (Link) e;
                        Rectangle b = l.getBounds();
                        if (mX >= b.getX() && mX <= (b.getX() + b.getWidth()) &&
                                mY >= b.getY() && mY <= (b.getY() + b.getHeight()))
                        {
                            l.click();
                            return;
                        }
                    }
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void drawCurrentPages()
    {
        int left = this.width / 2 - pageWidth - innerMargin;
        int right = this.width / 2 + innerMargin;
        int top = (this.height - pageHeight) / 2 - 9;
        int bottom = top + pageHeight;

        drawPage(left, top, currentPair * 2);
        drawPage(right, top, currentPair * 2 + 1);

        String cnt = "" + ((chapters.get(currentChapter).startPair + currentPair) * 2 + 1) + "/" + (totalPairs * 2);
        addStringWrapping(left, bottom, cnt, 0xFF000000, 1);
    }

    private void drawPage(int left, int top, int page)
    {
        ChapterData ch = chapters.get(currentChapter);
        if (page >= ch.pages.size())
            return;

        PageData pg = ch.pages.get(page);

        for (IPageElement e : pg.elements)
        {
            top += e.apply(left, top);
        }
    }

    private int addStringWrapping(int left, int top, String s, int color, int align)
    {
        if (align == 1)
        {
            left += (pageWidth - fontRendererObj.getStringWidth(s)) / 2;
        }
        else if (align == 2)
        {
            left += pageWidth - fontRendererObj.getStringWidth(s);
        }

        fontRendererObj.drawSplitString(s, left, top, pageWidth, color);
        return fontRendererObj.splitStringWidth(s, pageWidth);
    }

    private void drawBackgroundModel(float partialTicks)
    {
        IBakedModel modelBookA, modelBookB;

        float angleX;

        if (closing)
            angleX = (angleT + partialTicks * angleSpeed) * 90;
        else
            angleX = (angleT - partialTicks * angleSpeed) * 90;

        float blend = 0;
        if (angleX <= 0)
        {
            angleX = 0;
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = null;
            blend = 0;
        }
        else if (angleX < 30)
        {
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = RenderingStuffs.loadModel("elementsofpower:gui/book30.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            blend = (angleX) / 30.0f;
        }
        else if (angleX < 60)
        {
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book30.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = RenderingStuffs.loadModel("elementsofpower:gui/book60.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            blend = (angleX - 30) / 30.0f;
        }
        else if (angleX < 90)
        {
            modelBookA = RenderingStuffs.loadModel("elementsofpower:gui/book60.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            modelBookB = RenderingStuffs.loadModel("elementsofpower:gui/book90.obj", DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            blend = (angleX - 60) / 30.0f;
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

        GlStateManager.translate(this.width * 0.5 * (1 + angleX / 130.0f), this.height * 0.5 * (1 + angleX / 110.0f) + bookHeight / 2 - 4, 50);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate(-130, 1, 0, 0);
        GlStateManager.scale(2.0f, 2.0f, 2.5f);
        GlStateManager.scale(1.08f, 1.08f, 1.08f);

        GlStateManager.rotate(angleX * 1.1f, 0, 0, 1);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        RenderHelper.enableStandardItemLighting();

        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

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
                mc.getTextureManager().bindTexture(bookGuiTextures);
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

    class BookHelper
    {
        private void parseBook()
        {
            try
            {
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(bookLocation);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(res.getInputStream());

                doc.getDocumentElement().normalize();

                NodeList chaptersList = doc.getChildNodes().item(0).getChildNodes();
                for (int i = 0; i < chaptersList.getLength(); i++)
                {
                    Node chapterItem = chaptersList.item(i);

                    parseChapter(chapterItem);
                }

                int prevCount = 0;
                for (int i = 0; i < chapters.size(); i++)
                {
                    ChapterData chapter = chapters.get(i);
                    chapter.startPair = prevCount;
                    prevCount += chapter.pagePairs;
                }
                totalPairs = prevCount;
            }
            catch (IOException | ParserConfigurationException | SAXException e)
            {
                ChapterData ch = new ChapterData(0);
                chapters.add(ch);

                PageData pg = new PageData(0);
                ch.pages.add(pg);

                pg.elements.add(new Paragraph("Error loading book:"));
                pg.elements.add(new Paragraph(TextFormatting.RED + e.toString()));
            }
        }

        private void parseChapter(Node chapterItem)
        {
            if (!chapterItem.getNodeName().equals("chapter"))
            {
                return;
            }

            ChapterData chapter = new ChapterData(chapters.size());
            chapters.add(chapter);

            if (chapterItem.hasAttributes())
            {
                NamedNodeMap chapterAttributes = chapterItem.getAttributes();
                Node n = chapterAttributes.getNamedItem("id");
                if (n != null)
                {
                    chapter.id = n.getTextContent();
                    chaptersByName.put(chapter.id, chapter.num);
                }
            }

            NodeList pagesList = chapterItem.getChildNodes();
            for (int j = 0; j < pagesList.getLength(); j++)
            {
                Node pageItem = pagesList.item(j);

                parsePage(chapter, pageItem);
            }

            chapter.pagePairs = (chapter.pages.size() + 1) / 2;
        }

        private void parsePage(ChapterData chapter, Node pageItem)
        {
            if (!pageItem.getNodeName().equals("page"))
            {
                return;
            }

            PageData page = new PageData(chapter.pages.size());
            chapter.pages.add(page);

            if (pageItem.hasAttributes())
            {
                NamedNodeMap pageAttributes = pageItem.getAttributes();
                Node n = pageAttributes.getNamedItem("id");
                if (n != null)
                {
                    page.id = n.getTextContent();
                    pagesByName.put(page.id, new PageRef(chapter.num, page.num));
                }
            }

            NodeList elementsList = pageItem.getChildNodes();
            for (int k = 0; k < elementsList.getLength(); k++)
            {
                Node elementItem = elementsList.item(k);

                if (elementItem.getNodeName().equals("p"))
                {
                    Paragraph p = new Paragraph(elementItem.getTextContent());
                    page.elements.add(p);

                    if (elementItem.hasAttributes())
                    {
                        NamedNodeMap pageAttributes = elementItem.getAttributes();
                        parseParagraphAttributes(p, pageAttributes);
                    }
                }
                else if (elementItem.getNodeName().equals("title"))
                {
                    Title title = new Title(elementItem.getTextContent());
                    page.elements.add(title);

                    if (elementItem.hasAttributes())
                    {
                        NamedNodeMap pageAttributes = elementItem.getAttributes();
                        parseParagraphAttributes(title, pageAttributes);
                    }
                }
                else if (elementItem.getNodeName().equals("link"))
                {
                    Link link = new Link(elementItem.getTextContent());
                    page.elements.add(link);

                    if (elementItem.hasAttributes())
                    {
                        NamedNodeMap pageAttributes = elementItem.getAttributes();

                        parseLinkAttributes(link, pageAttributes);
                    }
                }
                else if (elementItem.getNodeName().equals("space"))
                {
                    Space s = new Space();
                    page.elements.add(s);

                    if (elementItem.hasAttributes())
                    {
                        NamedNodeMap pageAttributes = elementItem.getAttributes();

                        parseSpaceAttributes(s, pageAttributes);
                    }
                }
                else if (elementItem.getNodeName().equals("stack"))
                {
                    Stack s = new Stack();
                    page.elements.add(s);

                    if (elementItem.hasAttributes())
                    {
                        NamedNodeMap pageAttributes = elementItem.getAttributes();

                        parseStackAttributes(s, pageAttributes);
                    }
                }
            }
        }

        private void parseStackAttributes(Stack s, NamedNodeMap pageAttributes)
        {
            int meta = 0;
            int stackSize = 1;
            NBTTagCompound tag = null;

            Node attr = pageAttributes.getNamedItem("meta");
            if (attr != null)
            {
                meta = Ints.tryParse(attr.getTextContent());
            }

            attr = pageAttributes.getNamedItem("count");
            if (attr != null)
            {
                stackSize = Ints.tryParse(attr.getTextContent());
            }

            attr = pageAttributes.getNamedItem("tag");
            if (attr != null)
            {
                try
                {
                    tag = JsonToNBT.getTagFromJson(attr.getTextContent());
                }
                catch (NBTException e)
                {
                    ElementsOfPower.logger.warn("Invalid tag format: " + e.getMessage());
                }
            }

            attr = pageAttributes.getNamedItem("item");
            if (attr != null)
            {
                String itemName = attr.getTextContent();

                Item item = Item.itemRegistry.getObject(new ResourceLocation(itemName));

                s.stack = new ItemStack(item, stackSize, meta);
                s.stack.setTagCompound(tag);
            }

            attr = pageAttributes.getNamedItem("x");
            if (attr != null)
            {
                s.x = Ints.tryParse(attr.getTextContent());
            }

            attr = pageAttributes.getNamedItem("y");
            if (attr != null)
            {
                s.y = Ints.tryParse(attr.getTextContent());
            }
        }

        private void parseSpaceAttributes(Space s, NamedNodeMap pageAttributes)
        {
            Node attr = pageAttributes.getNamedItem("height");
            if (attr != null)
            {
                String t = attr.getTextContent();
                if (t.endsWith("%"))
                {
                    s.asPercent = true;
                    t = t.substring(0, t.length() - 1);
                }

                s.space = Ints.tryParse(t);
            }
        }

        private void parseLinkAttributes(Link link, NamedNodeMap pageAttributes)
        {
            parseParagraphAttributes(link, pageAttributes);

            Node attr = pageAttributes.getNamedItem("ref");
            if (attr != null)
            {
                String ref = attr.getTextContent();

                if (ref.indexOf(':') >= 0)
                {
                    String[] parts = ref.split(":");
                    link.target = new PageRef(parts[0], parts[1]);
                }
                else
                {
                    link.target = new PageRef(ref, null);
                }
            }
        }

        private void parseParagraphAttributes(Paragraph p, NamedNodeMap pageAttributes)
        {
            Node attr = pageAttributes.getNamedItem("align");
            if (attr != null)
            {
                String a = attr.getTextContent();
                if (a.equals("left"))
                    p.alignment = 0;
                else if (a.equals("center"))
                    p.alignment = 1;
                else if (a.equals("right"))
                    p.alignment = 2;
            }

            attr = pageAttributes.getNamedItem("indent");
            if (attr != null)
            {
                p.indent = Ints.tryParse(attr.getTextContent());
            }

            attr = pageAttributes.getNamedItem("space");
            if (attr != null)
            {
                p.space = Ints.tryParse(attr.getTextContent());
            }

            attr = pageAttributes.getNamedItem("color");
            if (attr != null)
            {
                String c = attr.getTextContent();

                if (c.startsWith("#"))
                    c = c.substring(1);

                try
                {
                    if (c.length() <= 6)
                    {
                        p.color = 0xFF000000 | Integer.parseInt(c, 16);
                    }
                    else
                    {
                        p.color = Integer.parseInt(c, 16);
                    }
                }
                catch (NumberFormatException e)
                {
                    // ignored
                }
            }
        }
    }

    private class PageRef
    {
        public int chapter;
        public int page;

        public boolean resolvedNames = false;
        public String chapterName;
        public String pageName;

        private PageRef(int chapter, int page)
        {
            this.chapter = chapter;
            this.page = page;
            resolvedNames = true;
        }

        private PageRef(String chapter, String page)
        {
            this.chapterName = chapter;
            this.pageName = page;
        }

        public void resolve()
        {
            if (!resolvedNames)
            {
                if (chapterName != null)
                {
                    Integer ch = Ints.tryParse(chapterName);
                    if (ch != null)
                    {
                        chapter = ch;
                    }
                    else
                    {
                        chapter = chaptersByName.get(chapterName);
                    }

                    if (pageName != null)
                    {
                        Integer pg = Ints.tryParse(pageName);
                        if (pg != null)
                        {
                            page = pg;
                        }
                    }
                }
                else if (pageName != null)
                {
                    PageRef temp = pagesByName.get(pageName);
                    temp.resolve();
                    chapter = temp.chapter;
                    page = temp.page;
                }
            }
        }
    }

    private class ChapterData
    {
        public final int num;
        public String id;

        public final List<PageData> pages = Lists.newArrayList();

        public int pagePairs;
        public int startPair;

        private ChapterData(int num)
        {
            this.num = num;
        }
    }

    private class PageData
    {
        public final int num;
        public String id;

        public final List<IPageElement> elements = Lists.newArrayList();

        private PageData(int num)
        {
            this.num = num;
        }
    }

    private interface IPageElement
    {
        int apply(int left, int top);
    }

    private class Paragraph implements IPageElement
    {
        public final String text;
        public int alignment = 0;
        public int color = 0xFF000000;
        public int indent = 0;
        public int space = 2;

        public Paragraph(String text)
        {
            this.text = text;
        }

        @Override
        public int apply(int left, int top)
        {
            return addStringWrapping(left + indent, top, text, color, alignment) + space;
        }
    }

    private class Link extends Paragraph
    {
        public PageRef target;
        public int colorHover = 0xFF77cc66;

        public boolean isHovering;
        public Rectangle bounds;

        public Link(String text)
        {
            super(TextFormatting.UNDERLINE + text);
            color = 0xFF7766cc;
        }

        public Rectangle getBounds()
        {
            return bounds;
        }

        public void click()
        {
            navigateTo(target);
        }

        @Override
        public int apply(int left, int top)
        {
            int height = fontRendererObj.splitStringWidth(text, pageWidth);
            int width = height > fontRendererObj.FONT_HEIGHT ? pageWidth : fontRendererObj.getStringWidth(text);
            bounds = new Rectangle(left, top, width, height);

            return addStringWrapping(left + indent, top, text, isHovering ? colorHover : color, alignment) + space;
        }
    }

    private class Title extends Paragraph
    {
        public Title(String text)
        {
            super(TextFormatting.ITALIC + "" + TextFormatting.UNDERLINE + text);
            alignment = 1;
            space = 4;
        }
    }

    private class Space implements IPageElement
    {
        public boolean asPercent;
        public int space;

        public Space()
        {
        }

        @Override
        public int apply(int left, int top)
        {
            return asPercent ? pageHeight * space / 100 : space;
        }
    }

    private class Stack implements IPageElement
    {
        public ItemStack stack;
        public int x = 0;
        public int y = 0;

        public Stack()
        {
        }

        @Override
        public int apply(int left, int top)
        {
            StackRenderingHelper.renderItemStack(mesher, renderEngine, left + x, top + y, stack, 0xFFFFFFFF, true);
            return 0;
        }
    }
}

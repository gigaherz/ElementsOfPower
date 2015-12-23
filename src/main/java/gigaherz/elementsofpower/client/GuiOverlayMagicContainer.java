package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiOverlayMagicContainer extends Gui
{
    public static GuiOverlayMagicContainer instance;

    Minecraft mc;
    ItemStack itemInUse = null;
    int slotInUse;
    StringBuilder sequence = new StringBuilder();

    final KeyBindingInterceptor[] interceptKeys = new KeyBindingInterceptor[8];

    public GuiOverlayMagicContainer()
    {
        instance = this;
        mc = Minecraft.getMinecraft();

        GameSettings s = Minecraft.getMinecraft().gameSettings;

        int l = s.keyBindings.length;
        int[] indices = new int[8];
        int f = 0;
        for(int i=0;i<8;i++)
        {
            KeyBinding b = s.keyBindsHotbar[i];
            for(int j =0;j<l;j++)
            {
                if(s.keyBindings[(f+j)%l] == b)
                {
                    f=f+j;
                    indices[i]=f;
                    break;
                }
            }
        }

        for (int i = 0; i < 8; i++)
        {
            interceptKeys[i] = new KeyBindingInterceptor(s.keyBindsHotbar[i]);
            s.keyBindsHotbar[i] = interceptKeys[i];
            s.keyBindings[indices[i]] = interceptKeys[i];
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE)
        {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        ItemStack heldItem = player.inventory.getCurrentItem();

        if (itemInUse != null && (heldItem != itemInUse || !player.isUsingItem()))
        {
            endHoldingRightButton(true);
        }

        // Contained essences

        MagicAmounts amounts = MagicDatabase.getContainedMagic(heldItem);
        if (amounts == null)
            return;

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        float rescale = 1;
        int rescaledWidth = (int)(event.resolution.getScaledWidth() / rescale);
        int rescaledHeight = (int)(event.resolution.getScaledHeight() / rescale);

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.scale(rescale,rescale,1);

        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

        int xPos = (rescaledWidth - 7 * 28 - 16) / 2;
        int yPos = 2;
        for (int i = 0; i < 8; i++)
        {
            int alpha = (amounts.amounts[i] < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

            ItemStack stack = ElementsOfPower.magicOrb.getStack((int) amounts.amounts[i], i);

            renderItem(mesher, renderEngine, xPos, yPos, stack, alpha);

            String formatted = ElementsOfPower.prettyNumberFormatter.format(amounts.amounts[i]);
            this.drawCenteredString(font, formatted, xPos + 8, yPos + 16, 0xFFC0C0C0);
            if (itemInUse != null)
                this.drawCenteredString(font, "K:" + (i + 1), xPos + 8, yPos + 28, 0xFFC0C0C0);

            xPos += 28;
        }

        renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        renderEngine.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        NBTTagCompound nbt = heldItem.getTagCompound();
        if (nbt != null)
        {
            String savedSequence = nbt.getString(ItemWand.SPELL_SEQUENCE_TAG);

            if(savedSequence != null && savedSequence.length() > 0)
            {
                // Saved spell sequence
                xPos = (rescaledWidth - 6 * (savedSequence.length() - 1) - 14) / 2;
                yPos = rescaledHeight / 2 - 16 - 16;
                for (char c : savedSequence.toCharArray())
                {
                    int i = SpellManager.elementIndices.get(c);

                    ItemStack stack = ElementsOfPower.magicOrb.getStack(1, i);

                    renderItem(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                    xPos += 6;
                }
            }
        }

        if (sequence != null)
        {
            // New spell sequence
            xPos = (rescaledWidth - 6 * (sequence.length() - 1) - 14) / 2;
            yPos = rescaledHeight / 2 + 16;
            for (char c : sequence.toString().toCharArray())
            {
                int i = SpellManager.elementIndices.get(c);

                ItemStack stack = ElementsOfPower.magicOrb.getStack(1, i);

                renderItem(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                xPos += 6;
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();

        // This doesn't belong here, but meh.
        if (itemInUse != null)
        {
            for (int i = 0; i < 8; i++)
            {
                if (interceptKeys[i].retrieveClick() && amounts.amounts[i] > 0)
                {
                    sequence.append(SpellManager.elementChars[i]);
                }
            }
        }
    }

    private void renderItem(ItemModelMesher mesher, TextureManager renderEngine, int xPos, int yPos, ItemStack stack, int color)
    {
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        renderEngine.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        GlStateManager.pushMatrix();

        IBakedModel model = mesher.getItemModel(stack);
        setupGuiTransform(xPos, yPos, model.isGui3d());

        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);

        renderItem(model, color);

        GlStateManager.popMatrix();

        renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        renderEngine.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }

    public void renderItem(IBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        for (BakedQuad bakedquad : model.getGeneralQuads())
        {
            LightUtil.renderQuadColor(worldrenderer, bakedquad, color);
        }

        tessellator.draw();
    }

    private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d)
    {
        GlStateManager.translate((float)xPosition, (float)yPosition, 100.0F + this.zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, -1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        if (isGui3d)
        {
            GlStateManager.scale(40.0F, 40.0F, 40.0F);
            GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.enableLighting();
        }
        else
        {
            GlStateManager.scale(64.0F, 64.0F, 64.0F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.disableLighting();
        }
    }

    public void beginHoldingRightButton(int slotNumber, ItemStack itemUsing)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        itemInUse = itemUsing;
        slotInUse = slotNumber;
        sequence = new StringBuilder();
        ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.BEGIN, player, slotInUse, null));

        for (int i = 0; i < 8; i++)
        {
            interceptKeys[i].setInterceptionActive(true);
        }
    }

    public void endHoldingRightButton(boolean cancelMagicSetting)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (cancelMagicSetting)
        {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.CANCEL, player, slotInUse, null));
        }
        else
        {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.COMMIT, player, slotInUse, sequence.toString()));
        }
        itemInUse = null;
        sequence = new StringBuilder();
        for (int i = 0; i < 8; i++)
        {
            interceptKeys[i].setInterceptionActive(false);
        }
    }
}

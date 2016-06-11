package gigaherz.elementsofpower.client.renderers;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.TickEventWandControl;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MagicContainerOverlay extends Gui
{
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
        {
            return;
        }

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        ItemStack heldItem = player.inventory.getCurrentItem();

        if (heldItem == null)
            return;

        // Contained essences
        MagicAmounts amounts = ContainerInformation.getContainedMagic(heldItem);
        if (amounts.isEmpty())
            return;

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        float rescale = 1;
        ScaledResolution res = event.getResolution();
        int rescaledWidth = (int) (res.getScaledWidth() / rescale);
        int rescaledHeight = (int) (res.getScaledHeight() / rescale);

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);

        GlStateManager.scale(rescale, rescale, 1);

        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;

        int xPos = (rescaledWidth - 7 * 28 - 16) / 2;
        int yPos = 2;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int alpha = (amounts.amounts[i] < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

            ItemStack stack = ElementsOfPower.magicOrb.getStack((int) amounts.amounts[i], Element.values[i]);

            StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, alpha);

            String formatted = ContainerInformation.isInfiniteContainer(heldItem) ? "\u221E" : ElementsOfPower.prettyNumberFormatter.format(amounts.amounts[i]);
            this.drawCenteredString(font, formatted, xPos + 8, yPos + 11, 0xFFC0C0C0);
            if (TickEventWandControl.instance.handInUse != null)
                this.drawCenteredString(font, "K:" + (i + 1), xPos + 8, yPos + 24, 0xFFC0C0C0);

            xPos += 28;
        }

        NBTTagCompound nbt = heldItem.getTagCompound();
        if (nbt != null)
        {
            String savedSequence = nbt.getString(ItemWand.SPELL_SEQUENCE_TAG);

            if (savedSequence.length() > 0)
            {
                // Saved spell sequence
                xPos = (rescaledWidth - 6 * (savedSequence.length() - 1) - 14) / 2;
                yPos = rescaledHeight / 2 - 16 - 16;
                for (char c : savedSequence.toCharArray())
                {
                    int i = SpellManager.elementIndices[c - 'A'];

                    ItemStack stack = ElementsOfPower.magicOrb.getStack(1, Element.values[i]);

                    StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                    xPos += 6;
                }
            }
        }

        if (TickEventWandControl.instance.sequence != null)
        {
            // New spell sequence
            xPos = (rescaledWidth - 6 * (TickEventWandControl.instance.sequence.length() - 1) - 14) / 2;
            yPos = rescaledHeight / 2 + 16;
            for (char c : TickEventWandControl.instance.sequence.toCharArray())
            {
                int i = SpellManager.elementIndices[c - 'A'];

                ItemStack stack = ElementsOfPower.magicOrb.getStack(1, Element.values[i]);

                StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                xPos += 6;
            }
        }

        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }
}

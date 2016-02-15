package gigaherz.elementsofpower.renders;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.TickEventWandControl;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
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
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE)
        {
            return;
        }

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        ItemStack heldItem = player.inventory.getCurrentItem();

        // Contained essences
        MagicAmounts amounts = ContainerInformation.getContainedMagic(heldItem);
        if (amounts == null)
            return;

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

        float rescale = 1;
        int rescaledWidth = (int) (event.resolution.getScaledWidth() / rescale);
        int rescaledHeight = (int) (event.resolution.getScaledHeight() / rescale);

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

            ItemStack stack = ElementsOfPower.magicOrb.getStack((int) amounts.amounts[i], i);

            StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, alpha, false);

            String formatted = ContainerInformation.isInfiniteContainer(heldItem) ? "\u221E" : ElementsOfPower.prettyNumberFormatter.format(amounts.amounts[i]);
            this.drawCenteredString(font, formatted, xPos + 8, yPos + 11, 0xFFC0C0C0);
            if (TickEventWandControl.instance.itemInUse != null)
                this.drawCenteredString(font, "K:" + (i + 1), xPos + 8, yPos + 24, 0xFFC0C0C0);

            xPos += 28;
        }

        NBTTagCompound nbt = heldItem.getTagCompound();
        if (nbt != null)
        {
            String savedSequence = nbt.getString(ItemWand.SPELL_SEQUENCE_TAG);

            if (savedSequence != null && savedSequence.length() > 0)
            {
                // Saved spell sequence
                xPos = (rescaledWidth - 6 * (savedSequence.length() - 1) - 14) / 2;
                yPos = rescaledHeight / 2 - 16 - 16;
                for (char c : savedSequence.toCharArray())
                {
                    int i = SpellManager.elementIndices.get(c);

                    ItemStack stack = ElementsOfPower.magicOrb.getStack(1, i);

                    StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF, false);

                    xPos += 6;
                }
            }
        }

        if (TickEventWandControl.instance.sequence != null)
        {
            // New spell sequence
            xPos = (rescaledWidth - 6 * (TickEventWandControl.instance.sequence.length() - 1) - 14) / 2;
            yPos = rescaledHeight / 2 + 16;
            for (char c : TickEventWandControl.instance.sequence.toString().toCharArray())
            {
                int i = SpellManager.elementIndices.get(c);

                ItemStack stack = ElementsOfPower.magicOrb.getStack(1, i);

                StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF, false);

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

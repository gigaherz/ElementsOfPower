package gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.client.WandUseManager;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MagicContainerOverlay extends AbstractGui
{
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
        {
            return;
        }

        ClientPlayerEntity player = Minecraft.getInstance().player;
        ItemStack heldItem = player.inventory.getCurrentItem();

        if (heldItem.getCount() <= 0)
            return;

        IMagicContainer magic = CapabilityMagicContainer.getContainer(heldItem);
        if (magic == null)
            return;

        // Contained essences
        MagicAmounts amounts = magic.getContainedMagic();
        if (!magic.isInfinite() && amounts.isEmpty())
            return;

        FontRenderer font = Minecraft.getInstance().fontRenderer;

        float rescale = 1;
        int rescaledWidth = (int) (res.getScaledWidth() / rescale);
        int rescaledHeight = (int) (res.getScaledHeight() / rescale);

        RenderSystem.pushMatrix();
        RenderSystem.depthMask(false);

        RenderSystem.scalef(rescale, rescale, 1);

        ItemModelMesher mesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
        TextureManager renderEngine = Minecraft.getInstance().textureManager;

        int xPos = (rescaledWidth - 7 * 28 - 16) / 2;
        int yPos = 2;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int alpha = (amounts.get(i) < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

            ItemStack stack = ElementsOfPowerMod.orb.getStack((int) amounts.get(i), Element.values[i]);

            StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, alpha);

            String formatted = magic.isInfinite() ? "\u221E" : ElementsOfPowerMod.prettyNumberFormatter.format(amounts.get(i));
            this.drawCenteredString(font, formatted, xPos + 8, yPos + 11, 0xFFC0C0C0);
            if (WandUseManager.instance.handInUse != null)
                this.drawCenteredString(font, "K:" + (i + 1), xPos + 8, yPos + 24, 0xFFC0C0C0);

            xPos += 28;
        }

        CompoundNBT nbt = heldItem.getTag();
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

                    ItemStack stack = ElementsOfPowerMod.orb.getStack(1, Element.values[i]);

                    StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                    xPos += 6;
                }
            }
        }

        if (WandUseManager.instance.sequence != null)
        {
            // New spell sequence
            xPos = (rescaledWidth - 6 * (WandUseManager.instance.sequence.length() - 1) - 14) / 2;
            yPos = rescaledHeight / 2 + 16;
            for (char c : WandUseManager.instance.sequence.toCharArray())
            {
                int i = SpellManager.elementIndices[c - 'A'];

                ItemStack stack = ElementsOfPowerMod.orb.getStack(1, Element.values[i]);

                StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                xPos += 6;
            }
        }

        RenderSystem.depthMask(true);

        RenderSystem.popMatrix();

        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }
}

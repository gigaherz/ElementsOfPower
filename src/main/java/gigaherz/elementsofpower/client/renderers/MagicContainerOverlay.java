package gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.client.MagicTooltips;
import gigaherz.elementsofpower.client.StackRenderingHelper;
import gigaherz.elementsofpower.client.WandUseManager;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.items.WandItem;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class MagicContainerOverlay extends AbstractGui
{
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        ItemStack heldItem = player.inventory.getCurrentItem();

        if (heldItem.getCount() <= 0)
            return;

        MagicContainerCapability.getContainer(heldItem).ifPresent(magic -> {

            // Contained essences
            MagicAmounts amounts = magic.getContainedMagic();
            if (!magic.isInfinite() && amounts.isEmpty())
                return;

            FontRenderer font = mc.fontRenderer;

            float rescale = 1;
            int rescaledWidth = (int) (mc.getMainWindow().getScaledWidth() / rescale);
            int rescaledHeight = (int) (mc.getMainWindow().getScaledHeight() / rescale);

            RenderSystem.pushMatrix();
            RenderSystem.depthMask(false);

            RenderSystem.scalef(rescale, rescale, 1);

            ItemModelMesher mesher = mc.getItemRenderer().getItemModelMesher();
            TextureManager renderEngine = mc.textureManager;

            int xPos = (rescaledWidth - 7 * 28 - 8) / 2;
            int yPos = 2;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                int alpha = (amounts.get(i) < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

                ItemStack stack = new ItemStack(Element.values[i].getOrb());

                StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, alpha);

                String formatted = magic.isInfinite() ? "\u221E" : MagicTooltips.PRETTY_NUMBER_FORMATTER.format(amounts.get(i));
                this.drawCenteredString(font, formatted, xPos+1, yPos + 11, 0xFFC0C0C0);
                if (WandUseManager.instance.handInUse != null)
                    this.drawCenteredString(font, "K:" + (i + 1), xPos+1, yPos + 24, 0xFFC0C0C0);

                xPos += 28;
            }

            CompoundNBT nbt = heldItem.getTag();
            if (nbt != null)
            {
                ListNBT seq = nbt.getList(WandItem.SPELL_SEQUENCE_TAG, Constants.NBT.TAG_LIST);
                List<Element> savedSequence = SpellManager.sequenceFromList(seq);

                if (savedSequence.size() > 0)
                {
                    // Saved spell sequence
                    xPos = (rescaledWidth - 6 * (savedSequence.size() - 1) - 14) / 2;
                    yPos = rescaledHeight / 2 - 16 - 16;
                    for (Element e : savedSequence)
                    {
                        ItemStack stack = new ItemStack(e.getOrb());

                        StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                        xPos += 6;
                    }
                }
            }

            List<Element> sequence = WandUseManager.instance.sequence;
            int spellLength = sequence.size();
            if (spellLength > 0)
            {
                // New spell sequence
                xPos = (rescaledWidth - 6 * (spellLength - 1) - 14) / 2;
                yPos = rescaledHeight / 2 + 16;
                for (Element e : sequence)
                {
                    ItemStack stack = new ItemStack(e.getOrb());

                    StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);

                    xPos += 6;
                }
            }

            RenderSystem.depthMask(true);

            RenderSystem.popMatrix();

            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
        });
    }
}

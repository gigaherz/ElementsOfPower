package gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.client.MagicTooltips;
import gigaherz.elementsofpower.client.StackRenderingHelper;
import gigaherz.elementsofpower.client.WandUseManager;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.essentializer.gui.EssentializerScreen;
import gigaherz.elementsofpower.items.WandItem;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;
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

        MatrixStack matrixStack = event.getMatrixStack();
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;
        ItemStack heldItem = player.inventory.getCurrentItem();

        if (heldItem.getCount() <= 0)
            return;

        MagicContainerCapability.getContainer(heldItem).ifPresent(magic -> {

            // Contained essences
            MagicAmounts contained = magic.getContainedMagic();

            MagicAmounts reservoir = MagicAmounts.EMPTY;

            MagicAmounts amounts = contained;
            if (heldItem.getItem() instanceof WandItem)
            {
                reservoir = WandItem.getTotalPlayerReservoir(player);
                amounts = amounts.add(reservoir);
            }

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

            int yTop = 13;

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                int xPos = (rescaledWidth - 7 * 28 - 8) / 2 + 28 * i + 1;
                int alpha = (amounts.get(i) < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

                ItemStack stack = new ItemStack(Element.values[i].getOrb());

                StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yTop - 11, stack, alpha);

                float e = contained.get(i);
                String formatted = Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e);
                drawCenteredString(matrixStack, font, formatted, xPos, yTop, 0xFFC0C0C0);
            }

            yTop += 12;

            if (!reservoir.isEmpty())
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    int xPos = (rescaledWidth - 7 * 28 - 8) / 2 + 28 * i + 1;

                    float e = reservoir.get(i);
                    String formatted = String.format("(%s)", Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e));
                    drawCenteredString(matrixStack, font, formatted, xPos, yTop, 0xFFC0C0C0);
                }

                yTop += 12;
            }

            CompoundNBT nbt = heldItem.getTag();
            if (nbt != null)
            {
                ListNBT seq = nbt.getList(WandItem.SPELL_SEQUENCE_TAG, Constants.NBT.TAG_STRING);
                List<Element> savedSequence = SpellManager.sequenceFromList(seq);

                if (savedSequence.size() > 0)
                {
                    // Saved spell sequence
                    for (int i = 0; i < savedSequence.size(); i++)
                    {
                        int xPos = (rescaledWidth - 6 * (savedSequence.size() - 1) - 14) / 2 + 6 * i;
                        int yPos = rescaledHeight / 2 - 16 - 16;
                        Element e = savedSequence.get(i);
                        ItemStack stack = new ItemStack(e.getOrb());

                        StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);
                    }

                    Spellcast temp = SpellManager.makeSpell(savedSequence);
                    if (temp != null)
                    {
                        MagicAmounts cost = temp.getSpellCost();
                        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                        {
                            if (MathHelper.epsilonEquals(cost.get(i), 0))
                                continue;

                            int xPos = (rescaledWidth - 7 * 28 - 8) / 2 + 28 * i + 1;

                            float e = -cost.get(i);
                            String formatted = Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e);
                            drawCenteredString(matrixStack, font, formatted, xPos, yTop, 0xFFC0C0C0);
                        }

                        yTop += 12;
                    }
                }
            }

            List<Element> sequence = WandUseManager.instance.sequence;
            int spellLength = sequence.size();
            if (spellLength > 0)
            {
                // New spell sequence
                for (int i = 0; i < sequence.size(); i++)
                {
                    int xPos = (rescaledWidth - 6 * (spellLength - 1) - 14) / 2 + 6 * i;
                    int yPos = rescaledHeight / 2 + 16;
                    Element e = sequence.get(i);
                    ItemStack stack = new ItemStack(e.getOrb());

                    StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos, yPos, stack, 0xFFFFFFFF);
                }

                Spellcast temp = SpellManager.makeSpell(sequence);
                if (temp != null)
                {
                    MagicAmounts cost = temp.getSpellCost();
                    for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                    {
                        if (MathHelper.epsilonEquals(cost.get(i), 0))
                            continue;

                        int xPos = (rescaledWidth - 7 * 28 - 8) / 2 + 28 * i + 1;

                        float e = -cost.get(i);
                        String formatted = Float.isInfinite(e) ? "\u221E" : MagicTooltips.PRETTY_NUMBER_FORMATTER.format(e);
                        drawCenteredString(matrixStack, font, formatted, xPos, yTop, 0xFFC0C0C0);
                    }

                    yTop += 12;
                }
            }

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                int xPos = (rescaledWidth - 7 * 28 - 8) / 2 + 28 * i + 1;

                if (WandUseManager.instance.handInUse != null)
                {
                    KeyBinding key = WandUseManager.instance.spellKeys[i];
                    drawCenteredString(matrixStack, font, new TranslationTextComponent("text.elementsofpower.magic.key", key.func_238171_j_()), xPos, yTop, 0xFFC0C0C0);
                }
            }

            RenderSystem.depthMask(true);

            RenderSystem.popMatrix();

            RenderSystem.disableAlphaTest();
            RenderSystem.disableBlend();
        });
    }
}

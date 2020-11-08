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
    public static final int ELEMENTS = 8;
    public static final int SPACING = 28;
    public static final int TOP_MARGIN = 4;
    public static final int SPELL_SPACING = 6;

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

            final int middle = rescaledWidth/2;
            final int xMargin = middle - (ELEMENTS - 1) * SPACING / 2;

            final int lineSpacing = font.FONT_HEIGHT + 3;

            int yTop = TOP_MARGIN;

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                int xPos = xMargin + SPACING * i;
                int alpha = (amounts.get(i) < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

                ItemStack stack = new ItemStack(Element.values[i].getOrb());

                StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos-16, yTop+8, stack, alpha);

                float e = contained.get(i);
                String formatted = Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e);
                drawCenteredString(matrixStack, font, formatted, xPos, yTop + 16 + 2, 0xFFC0C0C0);
            }

            yTop += 16 + 2 + lineSpacing;

            if (!reservoir.isEmpty())
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    int xPos = xMargin + SPACING * i;

                    float e = reservoir.get(i);
                    String formatted = String.format("(%s)", Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e));
                    drawCenteredString(matrixStack, font, formatted, xPos, yTop, 0xFFC0C0C0);
                }

                yTop += lineSpacing;
            }

            CompoundNBT nbt = heldItem.getTag();
            if (nbt != null)
            {
                ListNBT seq = nbt.getList(WandItem.SPELL_SEQUENCE_TAG, Constants.NBT.TAG_STRING);
                List<Element> savedSequence = SpellManager.sequenceFromList(seq);

                if (savedSequence.size() > 0)
                {
                    int intervals = savedSequence.size() - 1;
                    int xM = middle - intervals * SPELL_SPACING / 2;

                    int yPos = rescaledHeight / 2 - 32;

                    // Saved spell sequence
                    for (int i = savedSequence.size()-1; i >= 0; i--)
                    {
                        int xPos = xM + SPELL_SPACING * i;
                        Element e = savedSequence.get(i);
                        ItemStack stack = new ItemStack(e.getOrb());

                        StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos-16, yPos, stack, 0xFFFFFFFF);
                    }

                    Spellcast temp = SpellManager.makeSpell(savedSequence);
                    if (temp != null)
                    {
                        MagicAmounts cost = temp.getSpellCost();
                        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                        {
                            if (MathHelper.epsilonEquals(cost.get(i), 0))
                                continue;

                            int xPos = xMargin + SPACING * i;

                            float e = -cost.get(i);
                            String formatted = Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e);
                            drawCenteredString(matrixStack, font, formatted, xPos, yTop, 0xFFC0C0C0);
                        }

                        yTop += lineSpacing;
                    }
                }
            }

            List<Element> sequence = WandUseManager.instance.sequence;
            if (sequence.size() > 0)
            {
                int intervals = sequence.size() - 1;
                int xM = middle - intervals * SPELL_SPACING / 2;

                int yPos = rescaledHeight / 2 + 16;

                // New spell sequence
                for (int i = sequence.size()-1; i >= 0; i--)
                {
                    int xPos = xM + SPELL_SPACING * i;
                    Element e = sequence.get(i);
                    ItemStack stack = new ItemStack(e.getOrb());

                    StackRenderingHelper.renderItemStack(mesher, renderEngine, xPos-16, yPos, stack, 0xFFFFFFFF);
                }

                Spellcast temp = SpellManager.makeSpell(sequence);
                if (temp != null)
                {
                    MagicAmounts cost = temp.getSpellCost();
                    for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                    {
                        if (MathHelper.epsilonEquals(cost.get(i), 0))
                            continue;

                        int xPos = xMargin + SPACING * i;

                        float e = -cost.get(i);
                        String formatted = Float.isInfinite(e) ? "\u221E" : MagicTooltips.PRETTY_NUMBER_FORMATTER.format(e);
                        drawCenteredString(matrixStack, font, formatted, xPos, yTop, 0xFFC0C0C0);
                    }

                    yTop += lineSpacing;
                }
            }

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                int xPos = xMargin + SPACING * i;

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

package dev.gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.client.MagicTooltips;
import dev.gigaherz.elementsofpower.client.StackRenderingHelper;
import dev.gigaherz.elementsofpower.client.WandUseManager;
import dev.gigaherz.elementsofpower.essentializer.menu.EssentializerScreen;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.SpellManager;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class MagicContainerOverlay extends GuiComponent implements IIngameOverlay
{
    public static final int ELEMENTS = 8;
    public static final int SPACING = 28;
    public static final int TOP_MARGIN = 4;
    public static final int SPELL_SPACING = 6;

    public static void init()
    {
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, "elementsofpower_magic_overlay", new MagicContainerOverlay());
    }

    @Override
    public void render(ForgeIngameGui gui, PoseStack matrixStack, float partialTicks, int width, int height)
    {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        ItemStack heldItem = player.getInventory().getSelected();

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

            Font font = mc.font;

            float rescale = 1;
            int rescaledWidth = (int) (mc.getWindow().getGuiScaledWidth() / rescale);
            int rescaledHeight = (int) (mc.getWindow().getGuiScaledHeight() / rescale);

            matrixStack.pushPose();
            RenderSystem.depthMask(false);

            matrixStack.scale(rescale, rescale, 1);

            ItemModelShaper mesher = mc.getItemRenderer().getItemModelShaper();
            TextureManager renderEngine = mc.textureManager;

            final int middle = rescaledWidth / 2;
            final int xMargin = middle - (ELEMENTS - 1) * SPACING / 2;

            final int lineSpacing = font.lineHeight + 3;

            int yTop = TOP_MARGIN;

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                int xPos = xMargin + SPACING * i;
                int alpha = (amounts.get(i) < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

                ItemStack stack = new ItemStack(Element.values[i].getOrb());

                StackRenderingHelper.renderItemStack(mc.getItemRenderer(), matrixStack, stack, xPos - 8, yTop, 0, alpha);

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

            CompoundTag nbt = heldItem.getTag();
            if (nbt != null)
            {
                ListTag seq = nbt.getList(WandItem.SPELL_SEQUENCE_TAG, Constants.NBT.TAG_STRING);
                List<Element> savedSequence = SpellManager.sequenceFromList(seq);

                if (savedSequence.size() > 0)
                {
                    int intervals = savedSequence.size() - 1;
                    int xM = middle - intervals * SPELL_SPACING / 2;

                    int yPos = rescaledHeight / 2 - 32;

                    // Saved spell sequence
                    for (int i = savedSequence.size() - 1; i >= 0; i--)
                    {
                        int xPos = xM + SPELL_SPACING * i;
                        Element e = savedSequence.get(i);
                        ItemStack stack = new ItemStack(e.getOrb());

                        StackRenderingHelper.renderItemStack(mc.getItemRenderer(), matrixStack, stack, xPos - 8, yPos, 0, 0xFFFFFFFF);
                    }

                    Spellcast temp = SpellManager.makeSpell(savedSequence);
                    if (temp != null)
                    {
                        MagicAmounts cost = temp.getSpellCost();
                        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                        {
                            if (Mth.equal(cost.get(i), 0))
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
                for (int i = sequence.size() - 1; i >= 0; i--)
                {
                    int xPos = xM + SPELL_SPACING * i;
                    Element e = sequence.get(i);
                    ItemStack stack = new ItemStack(e.getOrb());

                    StackRenderingHelper.renderItemStack(mc.getItemRenderer(), matrixStack, stack, xPos - 8, yPos, 0, 0xFFFFFFFF);
                }

                Spellcast temp = SpellManager.makeSpell(sequence);
                if (temp != null)
                {
                    MagicAmounts cost = temp.getSpellCost();
                    for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                    {
                        if (Mth.equal(cost.get(i), 0))
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
                    KeyMapping key = WandUseManager.instance.spellKeys[i];
                    drawCenteredString(matrixStack, font, new TranslatableComponent("text.elementsofpower.magic.key", key.getTranslatedKeyMessage()), xPos, yTop, 0xFFC0C0C0);
                }
            }

            RenderSystem.depthMask(true);

            matrixStack.popPose();

            RenderSystem.disableBlend();
        });
    }
}
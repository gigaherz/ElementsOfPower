package dev.gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.capabilities.IMagicContainer;
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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value= Dist.CLIENT, modid= ElementsOfPowerMod.MODID, bus= EventBusSubscriber.Bus.MOD)
public class MagicContainerOverlay implements LayeredDraw.Layer
{
    public static final ResourceLocation SPELL_ACTIVE = ResourceLocation.fromNamespaceAndPath("elementsofpower", "textures/gui/spell_active.png");

    public static final int ELEMENTS = 8;
    public static final int SPACING = 28;
    public static final int TOP_MARGIN = 4;
    public static final int SPELL_SPACING = 6;

    public static MagicContainerOverlay instance;

    @SubscribeEvent
    public static void init(RegisterGuiLayersEvent event)
    {
        instance = new MagicContainerOverlay();
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, ElementsOfPowerMod.location("magic_overlay"), instance);
        //event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), ElementsOfPowerMod.location("magic_overlay"), new MagicContainerOverlay());
        NeoForge.EVENT_BUS.addListener(instance::tick);
    }

    public interface IGuiParticle
    {
        boolean tick();

        void render(GuiGraphics graphics, float partialTicks);

        void end();
    }

    public static class Rune implements IGuiParticle
    {
        private static final float MIN_RADIUS = 25;
        private static final float ROTATION_SPEED = 0.6f/20f;
        private static final int ICON_SIZE = 8;
        public static final float MAX_SPEED = 5.0f;
        public final TextureAtlasSprite sprite;
        private final float rotationSpeed;
        private final float distance;
        public float targetAngle;
        public int tickCount;
        public float actualPosX;
        public float actualPosY;
        public float deltaX;
        public float deltaY;
        public float targetPosX;
        public float targetPosY;
        public boolean end;
        public int endTicks;
        public int ticksBeforeEnd;

        public Rune(RandomSource random, TextureAtlasSprite sprite)
        {
            this.targetAngle = random.nextFloat() * Mth.TWO_PI;
            this.rotationSpeed = ROTATION_SPEED * (1 + 0.2f * (random.nextFloat() - 0.5f));
            this.distance = MIN_RADIUS * (1 + 0.2f * (random.nextFloat() - 0.5f));
            var startAngle = (1.0f/4) * Mth.TWO_PI;
            this.actualPosX = 1.5f * 20.0f * MAX_SPEED * Mth.cos(startAngle);
            this.actualPosY = 1.5f * 20.0f * MAX_SPEED * Mth.sin(startAngle);
            this.targetPosX = distance * Mth.cos(targetAngle);
            this.targetPosY = distance * Mth.sin(targetAngle);
            deltaX = targetPosX - actualPosX;
            deltaY = targetPosY - actualPosY;
            var angle = (float) Math.atan2(deltaY, deltaX);
            var magnitude = Math.min(MAX_SPEED, Mth.sqrt(deltaX * deltaX + deltaY * deltaY));
            deltaX = magnitude * Mth.cos(angle);
            deltaY = magnitude * Mth.sin(angle);
            this.sprite = sprite;
        }

        @Override
        public boolean tick()
        {
            tickCount ++;
            if (end) endTicks++;
            if (tickCount >= 0)
            {
                actualPosX += deltaX;
                actualPosY += deltaY;
                deltaX += 0.01f * (targetPosX - actualPosX);
                deltaY += 0.01f * (targetPosY - actualPosY);
                var angle = (float) Math.atan2(deltaY, deltaX);
                var magnitude = Math.min(MAX_SPEED, Mth.sqrt(deltaX * deltaX + deltaY * deltaY));
                deltaX = magnitude * Mth.cos(angle);
                deltaY = magnitude * Mth.sin(angle);

                targetAngle += rotationSpeed;
                targetPosX = distance * Mth.cos(targetAngle);
                targetPosY = distance * Mth.sin(targetAngle);
            }
            return end && (endTicks >= 15);
        }

        @Override
        public void render(GuiGraphics graphics, float partialTicks)
        {
            if (tickCount >= 0)
            {
                var size = end
                        ? ICON_SIZE * (1 + Math.max(0, 8 - ticksBeforeEnd / 2.0f)) * (1 - Math.min(1, (endTicks + partialTicks) / 15.0f))
                        : ICON_SIZE * (1 + Math.max(0, 8 - (tickCount + partialTicks) / 2.0f));
                var sizeI = (int) (size * 2);
                var posX = actualPosX + deltaX * partialTicks + graphics.guiWidth() / 2.0f - size;
                var posY = actualPosY + deltaY * partialTicks + graphics.guiHeight() / 2.0f - size;
                graphics.blit(Math.round(posX), Math.round(posY), 0, sizeI, sizeI, sprite, 1, 1, 1, 1);
            }
        }

        public void end()
        {
            end = true;
            ticksBeforeEnd = tickCount;
        }
    }

    public Rune addRune(Element element)
    {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        var sprite = atlas.apply(ResourceLocation.fromNamespaceAndPath("elementsofpower", "item/orb_"+ element.getName()));
        var rune = new Rune(Minecraft.getInstance().level.getRandom(), sprite);
        guiParticles.add(rune);
        return rune;
    }

    public final List<IGuiParticle> guiParticles = new ArrayList<>();
    private final Minecraft mc;

    private MagicContainerOverlay()
    {
        this.mc = Minecraft.getInstance();
    }

    public void tick(ClientTickEvent.Pre event)
    {
        List<IGuiParticle> remove = null;
        for(var rune : guiParticles)
        {
            if (rune.tick())
            {
                if (remove == null) remove = new ArrayList<>();
                remove.add(rune);
            }
        }
        if (remove != null)
            guiParticles.removeAll(remove);
    }

    public void endRunes()
    {
        for(var rune : guiParticles)
        {
            rune.end();
        }
    }

    public void setRunes(ItemStack itemUsing, WandItem wandItem)
    {
        endRunes();

        List<Element> savedSequence = wandItem.getSequence(itemUsing);
        if (savedSequence != null)
        {
            for (int i = 0; i < savedSequence.size(); i++)
            {
                var element = savedSequence.get(i);
                var rune = addRune(element);
                rune.tickCount = -10 * i;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker)
    {
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        ItemStack heldItem = player.getInventory().getSelected();

        if (heldItem.getCount() <= 0)
            return;

        var magic = MagicContainerCapability.getContainer(heldItem);
        if (magic == null)
            return;

        var poseStack = graphics.pose();
        poseStack.pushPose();
        RenderSystem.depthMask(false);

        if (WandUseManager.instance.handInUse != null)
        {
            int duration = 0;
            if (WandUseManager.instance.sequence.size() > 0)
            {
                duration = SpellManager.getChargeDuration(WandUseManager.instance.sequence);
            }
            else
            {
                duration = WandUseManager.instance.getChargeDuration(heldItem);
            }
            if (duration > 0 && WandUseManager.instance.useTicks > duration)
            {
                int scaledWidth = mc.getWindow().getGuiScaledWidth();
                int scaledHeight = mc.getWindow().getGuiScaledHeight();

                final int middleX= scaledWidth / 2;
                final int middleY = scaledHeight / 2;
                graphics.blit(SPELL_ACTIVE, middleX - 32, middleY - 32, 0, 0, 64, 64, 64, 64);
            }
        }

        for(var rune : guiParticles)
        {
            rune.render(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
        }

        renderCastingSequence(poseStack);

        renderSavedSpell(poseStack, heldItem);

        renderTopStack(graphics, poseStack, player, heldItem, magic);

        RenderSystem.depthMask(true);

        poseStack.popPose();

        RenderSystem.disableBlend();
    }

    private void renderCastingSequence(PoseStack poseStack)
    {
        List<Element> sequence = WandUseManager.instance.sequence;
        if (sequence.size() > 0)
        {
            int scaledWidth = mc.getWindow().getGuiScaledWidth();
            int scaledHeight = mc.getWindow().getGuiScaledHeight();

            final int middle = scaledWidth / 2;

            int intervals = sequence.size() - 1;
            int xM = middle - intervals * SPELL_SPACING / 2;

            int yPos = scaledHeight / 2 + 16;

            // New spell sequence
            for (int i = sequence.size() - 1; i >= 0; i--)
            {
                int xPos = xM + SPELL_SPACING * i;
                Element e = sequence.get(i);
                ItemStack stack = new ItemStack(e.getOrb());

                StackRenderingHelper.renderItemStack(mc.getItemRenderer(), poseStack, stack, xPos - 8, yPos, 0, 0xFFFFFFFF);
            }
        }
    }

    private void renderSavedSpell(PoseStack poseStack, ItemStack heldItem)
    {
        if (heldItem.getItem() instanceof  WandItem wandItem)
        {
            List<Element> savedSequence = wandItem.getSequence(heldItem);

            if (savedSequence != null && !savedSequence.isEmpty())
            {
                int scaledWidth = mc.getWindow().getGuiScaledWidth();
                int scaledHeight = mc.getWindow().getGuiScaledHeight();

                final int middle = scaledWidth / 2;

                int intervals = savedSequence.size() - 1;
                int xM = middle - intervals * SPELL_SPACING / 2;

                int yPos = scaledHeight - 64;

                // Saved spell sequence
                for (int i = savedSequence.size() - 1; i >= 0; i--)
                {
                    int xPos = xM + SPELL_SPACING * i;
                    Element e = savedSequence.get(i);
                    if (e.getOrb() != null)
                    {
                        ItemStack stack = new ItemStack(e.getOrb());

                        StackRenderingHelper.renderItemStack(mc.getItemRenderer(), poseStack, stack, xPos - 8, yPos, 0, 0xFFFFFFFF);
                    }
                }
            }
        }
    }

    private void renderTopStack(GuiGraphics graphics, PoseStack poseStack, LocalPlayer player, ItemStack heldItem, IMagicContainer magic)
    {
        if (heldItem.getItem() instanceof WandItem wandItem)
        {
            MagicAmounts contained = magic.getContainedMagic();
            MagicAmounts reservoir = WandItem.getTotalPlayerReservoir(player);
            MagicAmounts amounts = contained.add(reservoir);

            List<Element> savedSequence = wandItem.getSequence(heldItem);

            int scaledWidth = mc.getWindow().getGuiScaledWidth();

            final int middle = scaledWidth / 2;
            final int xMargin = middle - (ELEMENTS - 1) * SPACING / 2;

            final int lineSpacing = mc.font.lineHeight + 3;

            int yTop = TOP_MARGIN;

            if (magic.isInfinite() || !amounts.isEmpty())
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    int xPos = xMargin + SPACING * i;
                    int alpha = (amounts.get(i) < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

                    ItemStack stack = new ItemStack(Element.values[i].getOrb());

                    StackRenderingHelper.renderItemStack(mc.getItemRenderer(), poseStack, stack, xPos - 8, yTop, 0, alpha);

                    float e = contained.get(i);
                    String formatted = Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e);
                    graphics.drawCenteredString(mc.font, formatted, xPos, yTop + 16 + 2, 0xFFC0C0C0);
                }

                yTop += 16 + 2 + lineSpacing;

                if (!reservoir.isEmpty())
                {
                    for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                    {
                        int xPos = xMargin + SPACING * i;

                        float e = reservoir.get(i);
                        String formatted = String.format("(%s)", Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e));
                        graphics.drawCenteredString(mc.font, formatted, xPos, yTop, 0xFFC0C0C0);
                    }

                    yTop += lineSpacing;
                }

                if (savedSequence != null && savedSequence.size() > 0)
                {
                    Spellcast temp = SpellManager.makeSpell(savedSequence);
                    if (temp != null)
                    {
                        MagicAmounts cost = SpellManager.computeCost(temp);
                        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                        {
                            if (Mth.equal(cost.get(i), 0))
                                continue;

                            int xPos = xMargin + SPACING * i;

                            float e = -cost.get(i);
                            String formatted = Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e);
                            graphics.drawCenteredString(mc.font, formatted, xPos, yTop, 0xFFC0C0C0);
                        }

                        yTop += lineSpacing;
                    }
                }
            }

            if (WandUseManager.instance.handInUse != null)
            {
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    int xPos = xMargin + SPACING * i;

                    KeyMapping key = WandUseManager.instance.spellKeys[i];
                    graphics.drawCenteredString(mc.font, Component.translatable("text.elementsofpower.magic.key", key.getTranslatedKeyMessage()), xPos, yTop, 0xFFC0C0C0);
                }

                yTop += lineSpacing;
            }

            List<Element> sequence = WandUseManager.instance.sequence;
            if (sequence.size() > 0)
            {
                Spellcast temp = SpellManager.makeSpell(sequence);
                if (temp != null)
                {
                    MagicAmounts cost = SpellManager.computeCost(temp);
                    for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                    {
                        if (Mth.equal(cost.get(i), 0))
                            continue;

                        int xPos = xMargin + SPACING * i;

                        float e = -cost.get(i);
                        String formatted = Float.isInfinite(e) ? "\u221E" : MagicTooltips.PRETTY_NUMBER_FORMATTER.format(e);
                        graphics.drawCenteredString(mc.font, formatted, xPos, yTop, 0xFFC0C0C0);
                    }

                    yTop += lineSpacing;
                }
            }
        }
    }
}

package dev.gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value= Dist.CLIENT, modid= ElementsOfPowerMod.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class MagicContainerOverlay implements IGuiOverlay
{
    public static final int ELEMENTS = 8;
    public static final int SPACING = 28;
    public static final int TOP_MARGIN = 4;
    public static final int SPELL_SPACING = 6;

    public static MagicContainerOverlay instance;

    @SubscribeEvent
    public static void init(RegisterGuiOverlaysEvent event)
    {
        instance = new MagicContainerOverlay();
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), ElementsOfPowerMod.location("magic_overlay"), instance);
        //event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), ElementsOfPowerMod.location("magic_overlay"), new MagicContainerOverlay());
        NeoForge.EVENT_BUS.addListener(instance::tick);
    }

    public static class Rune
    {
        private static final float MIN_RADIUS = 25;
        private static final float ROTATION_SPEED = 0.6f/20f;
        private static final int ICON_SIZE = 8;
        public static final float MAX_SPEED = 5.0f;
        public final TextureAtlasSprite sprite;
        private final float rotationSpeed;
        private final float distance;
        public float targetAngle;
        public float tickCount;
        public float actualPosX;
        public float actualPosY;
        public float deltaX;
        public float deltaY;
        public float targetPosX;
        public float targetPosY;

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

        public void tick()
        {
            tickCount += 1;
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

        public void render(GuiGraphics graphics, float partialTicks)
        {
            var size = ICON_SIZE * (1 + Math.max(0, 8 - (tickCount+partialTicks)/2.0f));
            var sizeI = (int)(size * 2);
            var posX = actualPosX + deltaX * partialTicks + graphics.guiWidth() / 2.0f - size;
            var posY = actualPosY + deltaY * partialTicks + graphics.guiHeight() / 2.0f - size;
            graphics.blit(Math.round(posX), Math.round(posY), 0, sizeI, sizeI, sprite, 1, 1, 1, 1);
        }
    }

    public final List<Rune> runes = new ArrayList<>();

    public void addRune(Element element)
    {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        var sprite = atlas.apply(new ResourceLocation("elementsofpower", "item/orb_"+ element.getName()));
        var rune = new Rune(Minecraft.getInstance().level.getRandom(), sprite);
        runes.add(rune);
    }

    public void tick(TickEvent.ClientTickEvent event)
    {
        for(var rune : runes)
        {
            rune.tick();
        }
    }

    @Override
    public void render(ExtendedGui gui, GuiGraphics graphics, float partialTicks, int width, int height)
    {
        Minecraft mc = Minecraft.getInstance();

        for(var rune : runes)
        {
            rune.render(graphics, partialTicks);
        }

        LocalPlayer player = mc.player;
        if (player == null)
            return;

        ItemStack heldItem = player.getInventory().getSelected();

        if (heldItem.getCount() <= 0)
            return;

        var magic = MagicContainerCapability.getContainer(heldItem);
        if (magic == null)
            return;

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

        var poseStack = graphics.pose();
        poseStack.pushPose();
        RenderSystem.depthMask(false);

        poseStack.scale(rescale, rescale, 1);

        //ItemModelShaper mesher = mc.getItemRenderer().getItemModelShaper();
        //TextureManager renderEngine = mc.textureManager;

        final int middle = rescaledWidth / 2;
        final int xMargin = middle - (ELEMENTS - 1) * SPACING / 2;

        final int lineSpacing = font.lineHeight + 3;

        int yTop = TOP_MARGIN;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            int xPos = xMargin + SPACING * i;
            int alpha = (amounts.get(i) < 0.001) ? 0x3FFFFFFF : 0xFFFFFFFF;

            ItemStack stack = new ItemStack(Element.values[i].getOrb());

            StackRenderingHelper.renderItemStack(mc.getItemRenderer(), poseStack, stack, xPos - 8, yTop, 0, alpha);

            float e = contained.get(i);
            String formatted = Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e);
            graphics.drawCenteredString(font, formatted, xPos, yTop + 16 + 2, 0xFFC0C0C0);
        }

        yTop += 16 + 2 + lineSpacing;

        if (!reservoir.isEmpty())
        {
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                int xPos = xMargin + SPACING * i;

                float e = reservoir.get(i);
                String formatted = String.format("(%s)", Float.isInfinite(e) ? "\u221E" : EssentializerScreen.formatQuantityWithSuffix(e));
                graphics.drawCenteredString(font, formatted, xPos, yTop, 0xFFC0C0C0);
            }

            yTop += lineSpacing;
        }

        CompoundTag nbt = heldItem.getTag();
        if (nbt != null)
        {
            ListTag seq = nbt.getList(WandItem.SPELL_SEQUENCE_TAG, Tag.TAG_STRING);
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

                    StackRenderingHelper.renderItemStack(mc.getItemRenderer(), poseStack, stack, xPos - 8, yPos, 0, 0xFFFFFFFF);
                }

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
                        graphics.drawCenteredString(font, formatted, xPos, yTop, 0xFFC0C0C0);
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

                StackRenderingHelper.renderItemStack(mc.getItemRenderer(), poseStack, stack, xPos - 8, yPos, 0, 0xFFFFFFFF);
            }

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
                    graphics.drawCenteredString(font, formatted, xPos, yTop, 0xFFC0C0C0);
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
                graphics.drawCenteredString(font, Component.translatable("text.elementsofpower.magic.key", key.getTranslatedKeyMessage()), xPos, yTop, 0xFFC0C0C0);
            }
        }

        RenderSystem.depthMask(true);

        poseStack.popPose();

        RenderSystem.disableBlend();
    }
}

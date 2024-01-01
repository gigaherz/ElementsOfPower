package dev.gigaherz.elementsofpower.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.renderers.MagicContainerOverlay;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.UpdateSpellSequence;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.SpellManager;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WandUseManager
{
    private final static int[] defaultKeys = {
            GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4,
            GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8
    };

    public static WandUseManager instance = new WandUseManager();

    private final Minecraft mc;

    public final KeyMapping[] spellKeys = new KeyMapping[8];
    public final boolean[] lastKeyState = new boolean[8];

    public final List<Element> sequence = new ArrayList<>();
    public InteractionHand handInUse = null;
    public ItemStack activeStack = null;
    public int slotInUse;
    public int itemInUseCount;
    public int useTicks;
    private boolean failedSequence;

    public static void initialize()
    {
        NeoForge.EVENT_BUS.register(WandUseManager.instance);
    }

    static class OnUseContext implements IKeyConflictContext
    {
        @Override
        public boolean isActive()
        {
            return instance.handInUse != null;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return other != null && !(other instanceof VanillaHotbarResolverContext);
        }
    }

    static class VanillaHotbarResolverContext implements IKeyConflictContext
    {
        public final IKeyConflictContext context;

        VanillaHotbarResolverContext(@Nullable IKeyConflictContext context)
        {
            this.context = context;
        }

        @Override
        public boolean isActive()
        {
            return instance.handInUse == null && (context == null || context.isActive());
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return other != null && !(other instanceof OnUseContext)
                    && (context == null || context.conflicts(other));
        }
    }

    private WandUseManager()
    {
        this.mc = Minecraft.getInstance();
    }


    @Mod.EventBusSubscriber(value= Dist.CLIENT, modid=ElementsOfPowerMod.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
    private static class ModBusEvents
    {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event)
        {
            var mc = Minecraft.getInstance();

            Options s = mc.options;

        /*int l = s.keyBindings.length;
        int[] indices = new int[MagicAmounts.ELEMENTS];
        int f = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            KeyBinding b = s.keyBindsHotbar[i];
            for (int j = 0; j < l; j++)
            {
                if (s.keyBindings[(f + j) % l] == b)
                {
                    f = f + j;
                    indices[i] = f;
                    break;
                }
            }
        }*/

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                String translationKey = "key.elementsofpower.spellkey." + Element.values[i].getName();
                event.register(instance.spellKeys[i] =
                        new KeyMapping(translationKey, new OnUseContext(), InputConstants.Type.KEYSYM, defaultKeys[i], "key.elementsofpower.category"));

                s.keyHotbarSlots[i].setKeyConflictContext(new VanillaHotbarResolverContext(s.keyHotbarSlots[i].getKeyConflictContext()));
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (handInUse != null)
            {
                Player player = Objects.requireNonNull(Minecraft.getInstance().player);
                if (!player.isUsingItem()
                        || player.getUseItemRemainingTicks() > itemInUseCount)
                {
                    player.startUsingItem(handInUse);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingUseItem(LivingEntityUseItemEvent.Start e)
    {
        if (e.getEntity() instanceof LocalPlayer player)
        {
            if (activeStack == null)
            {

                int slotNumber = player.getInventory().selected;
                ItemStack itemUsing = player.getInventory().getSelected();
                if (!(itemUsing.getItem() instanceof WandItem))
                    return;

                InteractionHand hand = handInUse;

                beginHoldingRightButton(slotNumber, hand, itemUsing);
            }
            else if (e.getItem().getItem() == activeStack.getItem())
            {
                e.setDuration(itemInUseCount);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
            return;

        if (handInUse == null)
            return;

        LocalPlayer player = mc.player;
        if (player == null || player.getInventory() == null)
            return;

        ItemStack heldItem = player.getInventory().getSelected();
        int slotNumber = player.getInventory().selected;

        if (!mc.options.keyUse.isDown())
        {
            endHoldingRightButton(false);
            return;
        }

        if (heldItem.getItem().shouldCauseReequipAnimation(activeStack, heldItem, slotNumber != slotInUse))
        {
            endHoldingRightButton(true);
            return;
        }

        itemInUseCount--;
        useTicks++;
    }

    @SubscribeEvent
    public void checkKeys(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
            return;

        boolean anyChanged = false;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            boolean isPressedNow = false;
            if (spellKeys[i].isDown())
            {
                isPressedNow = true;
            }

            if (!isPressedNow && lastKeyState[i])
            {
                var element = Element.values[i];

                sequence.add(element);

                MagicContainerOverlay.instance.addRune(element);

                anyChanged = true;
            }

            lastKeyState[i] = isPressedNow;
        }

        if (anyChanged)
        {
            Spellcast temp = SpellManager.makeSpell(sequence);
            failedSequence = (temp == null);
            if (failedSequence)
            {
                sequence.clear();
            }
        }
    }

    private void beginHoldingRightButton(int slotNumber, InteractionHand hand, ItemStack itemUsing)
    {
        activeStack = itemUsing;
        handInUse = hand;
        itemInUseCount = activeStack.getUseDuration();
        slotInUse = slotNumber;
        useTicks = 0;
        sequence.clear();
        MagicContainerOverlay.instance.runes.clear();

        PacketDistributor.SERVER.noArg().send(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.BEGIN, slotInUse, null, useTicks));
    }

    private void endHoldingRightButton(boolean cancelMagicSetting)
    {
        if (cancelMagicSetting || (failedSequence && sequence.size() == 0))
        {
            PacketDistributor.SERVER.noArg().send(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.CANCEL, slotInUse, null, useTicks));
        }
        else
        {
            PacketDistributor.SERVER.noArg().send(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.COMMIT, slotInUse, sequence, useTicks));
        }

        handInUse = null;
        activeStack = null;
        itemInUseCount = 0;
        slotInUse = -1;
        useTicks = 0;
        sequence.clear();
        //MagicContainerOverlay.instance.runes.clear();

        Objects.requireNonNull(mc.player).stopUsingItem();
    }

    public boolean applyCustomArmTransforms(
            WandItem wandItem, PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack,
            float partialTicks, float equippedProgress, float swingProcess)
    {
        if (stack != activeStack)
            return false;

        boolean flag = player.getUsedItemHand() == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm = flag ? player.getMainArm() : player.getMainArm().getOpposite();

        boolean isRightHand = humanoidarm == HumanoidArm.RIGHT;
        float handFlip = isRightHand ? 1 : -1;
        poseStack.translate(handFlip * 0.56F, -0.52F + equippedProgress * -0.6F, -0.72F);
        poseStack.translate(handFlip * -0.2785682F, 0.18344387F, 0.15731531F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-13.935F));
        poseStack.mulPose(Axis.YP.rotationDegrees(handFlip * 35.3F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(handFlip * -9.785F));
        float animationProgressTicks = useTicks + partialTicks;
        float limit = sequence.size() > 0 ? SpellManager.getChargeDuration(sequence) : wandItem.getChargeDuration(stack);
        float animationProgress = animationProgressTicks / limit;
        animationProgress = (animationProgress * animationProgress + animationProgress * 2.0F) / 3.0F;

        if (animationProgress >= 1.0F)
        {
            float f15 = Mth.sin((animationProgressTicks - 0.1F) * 1.3F);
            float f18 = Mth.clamp(animationProgress - 1.0F, 0.1f, 1.0f);
            float f20 = f15 * f18;
            poseStack.translate(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);
        }

        if (animationProgress > 1.0F)
        {
            animationProgress = 1.0F;
        }

        poseStack.translate(animationProgress * 0.0F, animationProgress * 0.0F, animationProgress * 0.04F);
        poseStack.scale(1.0F, 1.0F, 1.0F + animationProgress * 0.2F);
        poseStack.mulPose(Axis.YN.rotationDegrees((float) handFlip * 45.0F));

        return true;
    }
}

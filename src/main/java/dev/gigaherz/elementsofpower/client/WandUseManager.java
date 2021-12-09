package dev.gigaherz.elementsofpower.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WandUseManager
{
    private final static int[] defaultKeys = {
            GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4,
            GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8
    };

    public static WandUseManager instance;

    public final KeyMapping[] spellKeys = new KeyMapping[8];
    public final boolean[] lastKeyState = new boolean[8];

    public final List<Element> sequence = new ArrayList<>();
    public InteractionHand handInUse = null;
    public ItemStack activeStack = null;
    public int slotInUse;
    public int itemInUseCount;
    private boolean failedSequence;

    private Minecraft mc;


    class OnUseContext implements IKeyConflictContext
    {
        @Override
        public boolean isActive()
        {
            return handInUse != null;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return other != null && !(other instanceof VanillaHotbarResolverContext);
        }
    }

    class VanillaHotbarResolverContext implements IKeyConflictContext
    {
        public final IKeyConflictContext context;

        VanillaHotbarResolverContext(@Nullable IKeyConflictContext context)
        {
            this.context = context;
        }

        @Override
        public boolean isActive()
        {
            return handInUse == null && (context == null || context.isActive());
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return other != null && !(other instanceof OnUseContext)
                    && (context == null || context.conflicts(other));
        }
    }

    public WandUseManager()
    {
        instance = this;
    }

    public void initialize()
    {
        mc = Minecraft.getInstance();

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
            ClientRegistry.registerKeyBinding(spellKeys[i] =
                    new KeyMapping(translationKey, new OnUseContext(), InputConstants.Type.KEYSYM, defaultKeys[i], "key.elementsofpower.category"));

            s.keyHotbarSlots[i].setKeyConflictContext(new VanillaHotbarResolverContext(s.keyHotbarSlots[i].getKeyConflictContext()));
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
        if (activeStack == null)
        {
            Player player = Objects.requireNonNull(mc.player);
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
    }

    @SubscribeEvent
    public void onKeyPress(TickEvent.ClientTickEvent event)
    {
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
                sequence.add(Element.values[i]);
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
        sequence.clear();

        ElementsOfPowerMod.CHANNEL.sendToServer(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.BEGIN, slotInUse, null));
    }

    private void endHoldingRightButton(boolean cancelMagicSetting)
    {
        if (cancelMagicSetting || (failedSequence && sequence.size() == 0))
        {
            ElementsOfPowerMod.CHANNEL.sendToServer(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.CANCEL, slotInUse, null));
        }
        else
        {
            ElementsOfPowerMod.CHANNEL.sendToServer(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.COMMIT, slotInUse, sequence));
        }

        handInUse = null;
        activeStack = null;
        itemInUseCount = 0;
        slotInUse = -1;
        sequence.clear();

        Objects.requireNonNull(mc.player).stopUsingItem();
    }
}

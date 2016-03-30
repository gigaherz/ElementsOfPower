package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickEventWandControl
{
    public static TickEventWandControl instance;

    public String sequence;
    public EnumHand handInUse = null;
    public ItemStack activeStack = null;
    public int slotInUse;
    public int itemInUseCount;

    private Minecraft mc;

    final KeyBindingInterceptor[] interceptKeys = new KeyBindingInterceptor[8];

    public TickEventWandControl()
    {
        instance = this;

        mc = Minecraft.getMinecraft();

        GameSettings s = mc.gameSettings;

        int l = s.keyBindings.length;
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
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            interceptKeys[i] = new KeyBindingInterceptor(s.keyBindsHotbar[i]);
            s.keyBindsHotbar[i] = interceptKeys[i];
            s.keyBindings[indices[i]] = interceptKeys[i];
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (handInUse != null)
            {
                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                if (!player.isHandActive()
                        || player.getItemInUseCount() > itemInUseCount)
                {
                    player.setActiveHand(handInUse);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingUseItem(LivingEntityUseItemEvent.Start e)
    {
        if (activeStack == null)
        {
            EntityPlayer player = mc.thePlayer;
            int slotNumber = player.inventory.currentItem;
            ItemStack itemUsing = player.inventory.getCurrentItem();
            if (itemUsing == null || !(itemUsing.getItem() instanceof ItemWand))
                return;

            EnumHand hand = handInUse;

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

        EntityPlayerSP player = mc.thePlayer;
        if (player == null || player.inventory == null)
            return;

        ItemStack heldItem = player.inventory.getCurrentItem();
        int slotNumber = player.inventory.currentItem;


        if (!mc.gameSettings.keyBindUseItem.isKeyDown())
        {
            endHoldingRightButton(false);
            return;
        }

        if (heldItem == null ||
                heldItem.getItem().shouldCauseReequipAnimation(activeStack, heldItem, slotNumber != slotInUse))
        {
            endHoldingRightButton(true);
            return;
        }

        itemInUseCount--;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (interceptKeys[i].retrieveClick())
            {
                sequence += SpellManager.elementChars[i];
            }
        }
    }

    private void beginHoldingRightButton(int slotNumber, EnumHand hand, ItemStack itemUsing)
    {
        activeStack = itemUsing;
        handInUse = hand;
        itemInUseCount = activeStack.getMaxItemUseDuration();
        slotInUse = slotNumber;
        sequence = "";

        ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.BEGIN, slotInUse, null));

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            interceptKeys[i].setInterceptionActive(true);
        }
    }

    private void endHoldingRightButton(boolean cancelMagicSetting)
    {
        if (cancelMagicSetting)
        {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.CANCEL, slotInUse, null));
        }
        else
        {
            ElementsOfPower.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.COMMIT, slotInUse, sequence));
        }

        handInUse = null;
        activeStack = null;
        itemInUseCount = 0;
        slotInUse = -1;
        sequence = null;

        mc.thePlayer.resetActiveHand();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            interceptKeys[i].setInterceptionActive(false);
        }
    }
}

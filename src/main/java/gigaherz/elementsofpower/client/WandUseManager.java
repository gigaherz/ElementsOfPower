package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.items.ItemWand;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

public class WandUseManager
{
    private final static int[] defaultKeys = {
            Keyboard.KEY_1,Keyboard.KEY_2,Keyboard.KEY_3,Keyboard.KEY_4,
            Keyboard.KEY_5,Keyboard.KEY_6,Keyboard.KEY_7,Keyboard.KEY_8
    };

    public static WandUseManager instance;

    public String sequence;
    public Hand handInUse = null;
    public ItemStack activeStack = null;
    public int slotInUse;
    public int itemInUseCount;

    private Minecraft mc;

    final KeyBinding[] spellKeys = new KeyBinding[8];

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
            ClientRegistry.registerKeyBinding(spellKeys[i] =
                    new KeyBinding("key.elementsofpower.spellkey."+ Element.values[i].getName(),
                            new OnUseContext(), defaultKeys[i], "key.elementsofpower.category"));

            s.keyBindsHotbar[i].setKeyConflictContext(new VanillaHotbarResolverContext(s.keyBindsHotbar[i].getKeyConflictContext()));
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (handInUse != null)
            {
                PlayerEntity player = Minecraft.getInstance().player;
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
            PlayerEntity player = mc.player;
            int slotNumber = player.inventory.currentItem;
            ItemStack itemUsing = player.inventory.getCurrentItem();
            if (!(itemUsing.getItem() instanceof ItemWand))
                return;

            Hand hand = handInUse;

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

        ClientPlayerEntity player = mc.player;
        if (player == null || player.inventory == null)
            return;

        ItemStack heldItem = player.inventory.getCurrentItem();
        int slotNumber = player.inventory.currentItem;


        if (!mc.gameSettings.keyBindUseItem.isKeyDown())
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
    public void onKeyPress(InputEvent.KeyInputEvent event)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            while (spellKeys[i].isPressed())
            {
                sequence += SpellManager.elementChars[i];
            }
        }
    }

    private void beginHoldingRightButton(int slotNumber, Hand hand, ItemStack itemUsing)
    {
        activeStack = itemUsing;
        handInUse = hand;
        itemInUseCount = activeStack.getMaxItemUseDuration();
        slotInUse = slotNumber;
        sequence = "";

        ElementsOfPowerMod.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.BEGIN, slotInUse, null));
    }

    private void endHoldingRightButton(boolean cancelMagicSetting)
    {
        if (cancelMagicSetting)
        {
            ElementsOfPowerMod.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.CANCEL, slotInUse, null));
        }
        else
        {
            ElementsOfPowerMod.channel.sendToServer(new SpellSequenceUpdate(SpellSequenceUpdate.ChangeMode.COMMIT, slotInUse, sequence));
        }

        handInUse = null;
        activeStack = null;
        itemInUseCount = 0;
        slotInUse = -1;
        sequence = null;

        mc.player.resetActiveHand();
    }
}

package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.items.WandItem;
import gigaherz.elementsofpower.network.UpdateSpellSequence;
import gigaherz.elementsofpower.spells.SpellManager;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class WandUseManager
{
    private final static int[] defaultKeys = {
            GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4,
            GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6, GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8
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
            String translationKey = "key.elementsofpower.spellkey." + Element.values[i].getName();
            ClientRegistry.registerKeyBinding(spellKeys[i] =
                    new KeyBinding(translationKey, new OnUseContext(), InputMappings.Type.SCANCODE, defaultKeys[i], "key.elementsofpower.category"));

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
            if (!(itemUsing.getItem() instanceof WandItem))
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
        itemInUseCount = activeStack.getUseDuration();
        slotInUse = slotNumber;
        sequence = "";

        ElementsOfPowerMod.channel.sendToServer(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.BEGIN, slotInUse, null));
    }

    private void endHoldingRightButton(boolean cancelMagicSetting)
    {
        if (cancelMagicSetting)
        {
            ElementsOfPowerMod.channel.sendToServer(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.CANCEL, slotInUse, null));
        }
        else
        {
            ElementsOfPowerMod.channel.sendToServer(new UpdateSpellSequence(UpdateSpellSequence.ChangeMode.COMMIT, slotInUse, sequence));
        }

        handInUse = null;
        activeStack = null;
        itemInUseCount = 0;
        slotInUse = -1;
        sequence = null;

        mc.player.resetActiveHand();
    }
}

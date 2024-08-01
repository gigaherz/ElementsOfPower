package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.ElementsOfPowerItems;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.items.MagicContainerItem;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid=ElementsOfPowerMod.MODID, bus= EventBusSubscriber.Bus.MOD)
public class MagicContainerCapability
{
    public static ItemCapability<IMagicContainer, Void> CAPABILITY = ItemCapability.createVoid(ElementsOfPowerMod.location("magic_container"), IMagicContainer.class);

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerItem(
                MagicContainerCapability.CAPABILITY,
                (stack, context) -> new MagicContainerItemImpl(stack),
                ElementsOfPowerItems.STAFF,
                ElementsOfPowerItems.WAND,
                ElementsOfPowerItems.RING,
                ElementsOfPowerItems.BRACELET,
                ElementsOfPowerItems.NECKLACE

        );
    }

    @Nullable
    public static IMagicContainer getContainer(ItemStack stack)
    {
        return stack.getCapability(CAPABILITY);
    }

    public static boolean hasContainer(ItemStack stack)
    {
        return stack.getCapability(CAPABILITY) != null;
    }

    public static boolean containsMagic(ItemStack stack)
    {
        var cap = stack.getCapability(CAPABILITY);
        return cap != null && (cap.isInfinite() || !cap.getContainedMagic().isEmpty());
    }

    public static boolean isNotFull(ItemStack stack)
    {
        var cap = stack.getCapability(CAPABILITY);
        return cap != null && !cap.isInfinite() && !cap.isFull() && !cap.getCapacity().isEmpty();
    }

    private static class MagicContainerItemImpl implements IMagicContainer
    {
        private final MagicContainerItem thisItem;
        private final ItemStack stack;

        public MagicContainerItemImpl(ItemStack stack)
        {
            this.stack = stack;
            thisItem = (MagicContainerItem) stack.getItem();
        }

        @Override
        public boolean isInfinite()
        {
            return thisItem.isInfinite(stack);
        }

        @Override
        public MagicAmounts getCapacity()
        {
            return thisItem.getCapacity(stack);
        }

        @Override
        public void setCapacity(MagicAmounts capacity)
        {
            // do nothing
        }

        @Override
        public MagicAmounts getContainedMagic()
        {
            return thisItem.getContainedMagic(stack);
        }

        @Override
        public void setContainedMagic(MagicAmounts containedMagic)
        {
            thisItem.setContainedMagic(stack, containedMagic);
        }

        @Override
        public MagicAmounts addMagic(MagicAmounts toAdd)
        {
            return thisItem.addMagic(stack, toAdd);
        }
    }
}

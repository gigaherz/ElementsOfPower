package dev.gigaherz.elementsofpower.items;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.world.item.ItemStack;

public class StaffItem extends WandItem
{
    public StaffItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        MagicAmounts magic = super.getCapacity(stack);
        magic = magic.add(magic);
        return magic;
    }

    @Override
    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        return am.multiply(2.0f);
    }

    @Override
    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        return am.multiply(0.5f);
    }

    // TODO: Enable when the animation system supports rotating OBJ parts
    /*@Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        return new ICapabilityProvider()
        {
            private final TimeValues.VariableValue cycleLength = new TimeValues.VariableValue(4);

            private final IAnimationStateMachine asm = ElementsOfPower.proxy.load(
                    ElementsOfPower.location("animation/staff.json"),
                    ImmutableMap.of("cycle_length", cycleLength));

            public boolean hasCapability(Capability<?> capability, EnumFacing facing)
            {
                return capability == CapabilityAnimation.ANIMATION_CAPABILITY;
            }

            @Nullable
            public <T> T getCapability(Capability<T> capability, EnumFacing facing)
            {
                if(capability == CapabilityAnimation.ANIMATION_CAPABILITY)
                    return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);

                return null;
            }
        };
    }*/
}

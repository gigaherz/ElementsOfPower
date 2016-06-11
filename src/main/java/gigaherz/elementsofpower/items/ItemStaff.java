package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.item.ItemStack;

public class ItemStaff extends ItemWand
{
    public ItemStaff(String name)
    {
        super(name);
        setUnlocalizedName(ElementsOfPower.MODID + ".staff");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Override
    public MagicAmounts getCapacity(ItemStack stack)
    {
        MagicAmounts magic = super.getCapacity(stack);
        if (magic == null)
            return null;

        magic.add(magic.copy());
        return magic;
    }

    @Override
    protected MagicAmounts adjustInsertedMagic(MagicAmounts am)
    {
        return am.copy().multiply(2.0f);
    }

    @Override
    protected MagicAmounts adjustRemovedMagic(MagicAmounts am)
    {
        return am.copy().multiply(0.5f);
    }

    // TODO: Enable when the animatio nsystem supports rotating OBJ parts
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

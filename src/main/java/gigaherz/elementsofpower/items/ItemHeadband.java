package gigaherz.elementsofpower.items;

import baubles.api.BaubleType;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemHeadband extends ItemBauble
{
    ICapabilityProvider provider;

    public ItemHeadband(String name)
    {
        super(name);
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        return super.initCapabilities(stack, nbt);
    }

    @Override
    protected Object getBaubleInstance()
    {
        return new BaubleData()
        {
            @Override
            public BaubleType getBaubleType(ItemStack itemstack)
            {
                return BaubleType.HEAD;
            }
        };
    }
}

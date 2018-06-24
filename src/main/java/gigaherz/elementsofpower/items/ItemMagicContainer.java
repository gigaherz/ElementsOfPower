package gigaherz.elementsofpower.items;

import gigaherz.common.state.ItemStateful;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.capabilities.AbstractMagicContainer;
import gigaherz.elementsofpower.capabilities.CapabilityMagicContainer;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.gemstones.Element;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.ItemGemstone;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemMagicContainer extends ItemStateful
{
    public ItemMagicContainer(String name)
    {
        super(name);
        setMaxStackSize(1);
        setHasSubtypes(true);
    }

    public ItemStack getStack(Gemstone gemstone)
    {
        return getStack(1, gemstone);
    }

    public ItemStack getStack(int count, Gemstone gemstone)
    {
        return new ItemStack(this, count, gemstone.ordinal());
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack _stack, @Nullable NBTTagCompound nbt)
    {
        return new ICapabilityProvider() {
            final ItemStack stack = _stack;
            final IMagicContainer container = new IMagicContainer() {

                @Override
                public boolean isInfinite()
                {
                    return ItemMagicContainer.this.isInfinite(stack);
                }

                @Override
                public MagicAmounts getCapacity()
                {
                    return ItemMagicContainer.this.getCapacity(stack);
                }

                @Override
                public void setCapacity(MagicAmounts capacity)
                {
                    // do nothing
                }

                @Override
                public MagicAmounts getContainedMagic()
                {
                    NBTTagCompound compound = stack.getTagCompound();
                    if (compound == null || !compound.hasKey("ContainedMagic"))
                        return MagicAmounts.EMPTY;
                    return new MagicAmounts(compound.getCompoundTag("ContainedMagic"));
                }

                @Override
                public void setContainedMagic(MagicAmounts containedMagic)
                {
                    NBTTagCompound compound = stack.getTagCompound();
                    if (compound == null)
                        stack.setTagCompound(compound = new NBTTagCompound());
                    compound.setTag("ContainedMagic", containedMagic.serializeNBT());
                }
            };

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
            {
                if (capability == CapabilityMagicContainer.INSTANCE && canContainMagic(stack))
                    return true;
                return false;
            }

            @SuppressWarnings("unchecked")
            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
            {
                if (capability == CapabilityMagicContainer.INSTANCE && canContainMagic(stack))
                    return (T)container;
                return null;
            }
        };
    }

    public abstract boolean canContainMagic(ItemStack stack);
    public abstract boolean isInfinite(ItemStack stack);
    public abstract MagicAmounts getCapacity(ItemStack stack);

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        IMagicContainer magic = ContainerInformation.getMagic(stack);
        if (magic == null)
            return false;

        if (magic.isInfinite())
            return true;

        return !magic.getContainedMagic().isEmpty();
    }

    public ItemStack addContainedMagic(ItemStack stack, ItemStack orb)
    {
        if (stack.getCount() <= 0)
            return ItemStack.EMPTY;
        if (orb.getCount() <= 0)
            return stack;

        IMagicContainer magic = ContainerInformation.getMagic(stack);
        if (magic == null)
            return stack;

        if (magic.isFull() || magic.isInfinite())
            return stack;

        magic.insertMagic(MagicAmounts.ofElement(Element.values[orb.getMetadata()], 8), false);

        return stack;
    }
}

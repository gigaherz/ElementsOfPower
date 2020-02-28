package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class GemstoneHolderCapability
{
    @CapabilityInject(IGemstoneHolder.class)
    public static Capability<IGemstoneHolder> INSTANCE;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IGemstoneHolder.class, new Storage(), Impl::new);
    }

    public static LazyOptional<IGemstoneHolder> get(ItemStack stack)
    {
        return stack.getCapability(INSTANCE);
    }

    private static class Storage implements Capability.IStorage<IGemstoneHolder>
    {
        @Override
        public INBT writeNBT(Capability<IGemstoneHolder> capability, IGemstoneHolder instance, Direction side)
        {
            throw new RuntimeException("This capability is not serializable");
        }

        @Override
        public void readNBT(Capability<IGemstoneHolder> capability, IGemstoneHolder instance, Direction side, INBT nbt)
        {
            throw new RuntimeException("This capability is not serializable");
        }
    }

    public static class Impl implements IGemstoneHolder
    {
        @Nullable
        @Override
        public Gemstone getGemstone(ItemStack stack)
        {
            CompoundNBT tag = stack.getChildTag("GemstoneHolder");
            if (tag != null)
            {
                return Gemstone.byName(tag.getString("Gemstone"));
            }
            return null;
        }
    }
}

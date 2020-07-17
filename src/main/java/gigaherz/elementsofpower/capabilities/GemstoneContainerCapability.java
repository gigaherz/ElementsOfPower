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

public class GemstoneContainerCapability
{
    @CapabilityInject(IGemstoneContainer.class)
    public static Capability<IGemstoneContainer> INSTANCE;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IGemstoneContainer.class, new Storage(), Impl::new);
    }

    public static LazyOptional<IGemstoneContainer> get(ItemStack stack)
    {
        return stack.getCapability(INSTANCE);
    }

    private static class Storage implements Capability.IStorage<IGemstoneContainer>
    {
        @Override
        public INBT writeNBT(Capability<IGemstoneContainer> capability, IGemstoneContainer instance, Direction side)
        {
            throw new RuntimeException("This capability is not serializable");
        }

        @Override
        public void readNBT(Capability<IGemstoneContainer> capability, IGemstoneContainer instance, Direction side, INBT nbt)
        {
            throw new RuntimeException("This capability is not serializable");
        }
    }

    public static class Impl implements IGemstoneContainer
    {
        @Override
        public ItemStack setGemstone(ItemStack stack, @Nullable Gemstone g)
        {
            if (g != null)
            {
                CompoundNBT tag = stack.getOrCreateChildTag("GemstoneContainer");
                tag.putString("Gemstone", g.getString());
            }
            else
            {
                CompoundNBT tag = stack.getChildTag("GemstoneContainer");
                if (tag != null)
                {
                    tag.remove("Gemstone");
                }
            }
            return stack;
        }

        @Nullable
        @Override
        public Gemstone getGemstone(ItemStack stack)
        {
            CompoundNBT tag = stack.getChildTag("GemstoneContainer");
            if (tag != null)
            {
                return Gemstone.byName(tag.getString("Gemstone"));
            }
            return null;
        }
    }
}

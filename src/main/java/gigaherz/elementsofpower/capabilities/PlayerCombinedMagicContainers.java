package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerCombinedMagicContainers implements IMagicContainer
{
    @CapabilityInject(PlayerCombinedMagicContainers.class)
    public static Capability<PlayerCombinedMagicContainers> CAPABILITY = null;

    private PlayerEntity player;

    public void setPlayer(PlayerEntity player)
    {
        this.player = player;
    }

    @Override
    public MagicAmounts getCapacity()
    {
        return player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(items -> {
            MagicAmounts am = MagicAmounts.EMPTY;
            for (int i = 0; i < items.getSlots(); i++)
            {
                am = am.add(items.getStackInSlot(i).getCapability(MagicContainerCapability.INSTANCE).map(IMagicContainer::getCapacity).orElse(MagicAmounts.EMPTY));
            }
            return am;
        }).orElse(MagicAmounts.EMPTY);
    }

    @Override
    public void setCapacity(MagicAmounts capacity)
    {
        throw new IllegalStateException("The Combined container can not be directly modified.");
    }

    @Override
    public MagicAmounts getContainedMagic()
    {
        return player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(items -> {
            MagicAmounts am = MagicAmounts.EMPTY;
            for (int i = 0; i < items.getSlots(); i++)
            {
                am = am.add(items.getStackInSlot(i).getCapability(MagicContainerCapability.INSTANCE).map(IMagicContainer::getContainedMagic).orElse(MagicAmounts.EMPTY));
            }
            return am;
        }).orElse(MagicAmounts.EMPTY);
    }

    @Override
    public void setContainedMagic(MagicAmounts containedMagic)
    {
        throw new IllegalStateException("The Combined container can not be directly modified.");
    }

    public MagicAmounts addMagic(MagicAmounts magicToAdd)
    {
        final MagicAmounts[] refAmounts = new MagicAmounts[]{magicToAdd};
        return player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).map(items -> {
            for (int i = 0; i < items.getSlots(); i++)
            {
                if (items.getStackInSlot(i).getCapability(MagicContainerCapability.INSTANCE).map(magic -> {
                    if (!magic.isInfinite())
                    {
                        MagicAmounts capacity = magic.getCapacity();
                        MagicAmounts contained = magic.getContainedMagic();

                        MagicAmounts empty = capacity.subtract(contained);

                        if (!empty.isEmpty())
                        {
                            MagicAmounts toAdd = refAmounts[0];
                            MagicAmounts willAdd = MagicAmounts.min(toAdd, empty);
                            MagicAmounts remaining = toAdd.subtract(willAdd);
                            magic.setContainedMagic(contained.add(willAdd));
                            refAmounts[0] = remaining;
                        }
                    }
                    return refAmounts[0].isEmpty();
                }).orElse(false))
                    break;
            }
            return refAmounts[0];
        }).orElse(MagicAmounts.EMPTY);
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(PlayerCombinedMagicContainers.class, new Capability.IStorage<PlayerCombinedMagicContainers>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<PlayerCombinedMagicContainers> capability, PlayerCombinedMagicContainers instance, Direction side)
            {
                throw new IllegalStateException("This capability is not serializable.");
            }

            @Override
            public void readNBT(Capability<PlayerCombinedMagicContainers> capability, PlayerCombinedMagicContainers instance, Direction side, INBT nbt)
            {
                throw new IllegalStateException("This capability is not serializable.");
            }
        }, PlayerCombinedMagicContainers::new);

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, PlayerCombinedMagicContainers::attachCapability);
    }

    private static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof PlayerEntity)
        {
            event.addCapability(ElementsOfPowerMod.location("player_combined_magic"), new ICapabilityProvider()
            {
                final LazyOptional<PlayerCombinedMagicContainers> supplier = LazyOptional.of(() -> {
                    PlayerCombinedMagicContainers inst = new PlayerCombinedMagicContainers();
                    inst.setPlayer((PlayerEntity) event.getObject());
                    return inst;
                });

                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
                {
                    if (cap == CAPABILITY)
                    {
                        return supplier.cast();
                    }
                    return LazyOptional.empty();
                }
            });
        }
    }
}

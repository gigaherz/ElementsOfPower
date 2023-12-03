package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.capabilities.*;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerCombinedMagicContainers implements IMagicContainer
{
    public static Capability<PlayerCombinedMagicContainers> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private Player player;

    public void setPlayer(Player player)
    {
        this.player = player;
    }

    @Override
    public MagicAmounts getCapacity()
    {
        return player.getCapability(Capabilities.ITEM_HANDLER).map(items -> {
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
        return player.getCapability(Capabilities.ITEM_HANDLER).map(items -> {
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
        return player.getCapability(Capabilities.ITEM_HANDLER).map(items -> {
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

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(PlayerCombinedMagicContainers.class);

        NeoForge.EVENT_BUS.addGenericListener(Entity.class, PlayerCombinedMagicContainers::attachCapability);
    }

    private static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            event.addCapability(ElementsOfPowerMod.location("player_combined_magic"), new ICapabilityProvider()
            {
                final LazyOptional<PlayerCombinedMagicContainers> supplier = LazyOptional.of(() -> {
                    PlayerCombinedMagicContainers inst = new PlayerCombinedMagicContainers();
                    inst.setPlayer((Player) event.getObject());
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

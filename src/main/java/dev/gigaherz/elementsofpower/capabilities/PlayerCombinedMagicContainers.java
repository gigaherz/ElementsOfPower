package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod.EventBusSubscriber(modid=ElementsOfPowerMod.MODID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerCombinedMagicContainers implements IMagicContainer
{
    public static EntityCapability<IMagicContainer, Void> CAPABILITY = EntityCapability.createVoid(ElementsOfPowerMod.location("player_combined_magic"), IMagicContainer.class);

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerEntity(
                PlayerCombinedMagicContainers.CAPABILITY,
                EntityType.PLAYER,
                (entity, context) -> new PlayerCombinedMagicContainers(entity)
        );
    }

    private final Player player;

    public PlayerCombinedMagicContainers(Player player)
    {
        this.player = player;
    }

    @Override
    public MagicAmounts getCapacity()
    {
        var items = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (items != null) {
            MagicAmounts am = MagicAmounts.EMPTY;
            for (int i = 0; i < items.getSlots(); i++)
            {
                var magic = items.getStackInSlot(i).getCapability(MagicContainerCapability.CAPABILITY);
                if (magic != null)
                    am = am.add(magic.getCapacity());
            }
            return am;
        }
        return MagicAmounts.EMPTY;
    }

    @Override
    public void setCapacity(MagicAmounts capacity)
    {
        throw new IllegalStateException("The Combined container can not be directly modified.");
    }

    @Override
    public MagicAmounts getContainedMagic()
    {
        var items = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (items != null) {
            MagicAmounts am = MagicAmounts.EMPTY;
            for (int i = 0; i < items.getSlots(); i++)
            {
                var magic = items.getStackInSlot(i).getCapability(MagicContainerCapability.CAPABILITY);
                if (magic != null)
                    am = am.add(magic.getContainedMagic());
            }
            return am;
        }
        return MagicAmounts.EMPTY;
    }

    @Override
    public void setContainedMagic(MagicAmounts containedMagic)
    {
        throw new IllegalStateException("The Combined container can not be directly modified.");
    }

    @Override
    public MagicAmounts addMagic(MagicAmounts magicToAdd)
    {
        var items = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (items != null)
        {
            for (int i = 0; i < items.getSlots(); i++)
            {
                var magic = items.getStackInSlot(i).getCapability(MagicContainerCapability.CAPABILITY);
                if (magic != null)
                {
                    magicToAdd = magic.addMagic(magicToAdd);
                    if (magicToAdd.isEmpty())
                        break;
                }
            }
            return magicToAdd;
        }
        return MagicAmounts.EMPTY;
    }
}

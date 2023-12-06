package dev.gigaherz.elementsofpower.integration;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.NonNullLazy;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.stream.Stream;

public class Curios
{
    private static final NonNullLazy<Boolean> isCuriosLoaded = NonNullLazy.of(() -> ModList.get().isLoaded("curios"));

    public static Stream<IItemHandler> getCurios(Player player)
    {
        return isCuriosLoaded.get() ? ActualCurios.getCurios(player) : Stream.empty();
    }

    private static class ActualCurios
    {
        public static Stream<IItemHandler> getCurios(Player player)
        {
            var curios = player.getCapability(CuriosCapability.INVENTORY);
            if (curios != null)
            {
                return curios.getCurios().values().stream().<IItemHandler>map(ICurioStacksHandler::getStacks);
            }
            return Stream.empty();
        }
    }
}

package gigaherz.elementsofpower.integration;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.stream.Stream;

public class Curios
{
    private static final NonNullLazy<Boolean> isCuriosLoaded = NonNullLazy.of(() -> ModList.get().isLoaded("curios"));

    public static Stream<IItemHandler> getCurios(PlayerEntity player)
    {
        return isCuriosLoaded.get() ? ActualCurios.getCurios(player) : Stream.empty();
    }

    private static class ActualCurios
    {
        public static Stream<IItemHandler> getCurios(PlayerEntity player)
        {
            return player.getCapability(CuriosCapability.INVENTORY).map((curios) ->
                    curios.getCurios().values().stream().<IItemHandler>map(ICurioStacksHandler::getStacks)
            ).orElse(Stream.empty());
        }
    }
}

package gigaherz.elementsofpower.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public interface IModProxy
{
    default void init() {}

    default void beginTracking(PlayerEntity playerIn, Hand hand) {}
}

package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.common.IModProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class ClientProxy implements IModProxy
{
    @Override
    public void beginTracking(PlayerEntity playerIn, Hand hand)
    {
        WandUseManager.instance.handInUse = hand;
        playerIn.setActiveHand(hand);
    }
}

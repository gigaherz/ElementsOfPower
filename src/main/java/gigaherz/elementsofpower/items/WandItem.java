package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.client.WandUseManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class WandItem extends GemContainerItem
{
    public static final String SPELL_SEQUENCE_TAG = "SpellSequence";

    public WandItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);

        if (hand == Hand.MAIN_HAND)
        {
            if (worldIn.isRemote)
            {
                beginTracking(playerIn, hand);
            }

            playerIn.setActiveHand(hand);
            return ActionResult.func_226248_a_(itemStackIn);
        }

        return ActionResult.func_226251_d_(itemStackIn);
    }

    public static void beginTracking(PlayerEntity playerIn, Hand hand)
    {
        WandUseManager.instance.handInUse = hand;
    }
}
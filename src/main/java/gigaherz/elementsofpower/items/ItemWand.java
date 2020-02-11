package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemWand extends ItemGemContainer
{
    public static final String SPELL_SEQUENCE_TAG = "SpellSequence";

    public ItemWand(Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);

        if (hand == Hand.MAIN_HAND)
        {
            ElementsOfPowerMod.proxy.beginTracking(playerIn, hand);
            return ActionResult.func_226248_a_(itemStackIn);
        }

        return ActionResult.func_226251_d_(itemStackIn);
    }
}
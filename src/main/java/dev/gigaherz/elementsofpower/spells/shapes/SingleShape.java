package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SingleShape extends SpellShape
{
    @Override
    public InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        return cast.init(player.level, player);
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        HitResult mop = cast.getHitPosition();

        if (mop != null)
        {
            if (mop.getType() == HitResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, ((EntityHitResult) mop).getEntity(), mop.getLocation());
            }
            else if (mop.getType() == HitResult.Type.BLOCK)
            {
                BlockPos pos = ((BlockHitResult) mop).getBlockPos();
                BlockState state = cast.world.getBlockState(pos);
                cast.getEffect().processBlockWithinRadius(cast, pos, state, 0, mop);
            }
        }
    }
}

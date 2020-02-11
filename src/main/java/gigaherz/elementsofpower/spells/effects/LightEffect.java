package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.blocks.BlockLight;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class LightEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0xFFFFFF;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 5;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {
        for (int i = 0; i < 8; ++i)
        {
            cast.spawnRandomParticle(EnumParticleTypes.CRIT_MAGIC,
                    mop.getHitVec().x, mop.getHitVec().y, mop.getHitVec().z);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null)
        {
            blockPos = blockPos.offset(mop.sideHit);
            currentState = cast.world.getBlockState(blockPos);
        }

        int density = MathHelper.clamp((int)(16-16*r),1,16);

        Block block = currentState.getBlock();

        if (block == Blocks.AIR)
        {
            cast.world.setBlockState(blockPos, ElementsOfPowerMod.light.getDefaultState().withProperty(BlockLight.DENSITY, density));
        }
        else if (block == ElementsOfPowerMod.light)
        {
            ((BlockLight)block).resetCooldown(cast.world, blockPos, currentState, density);
        }
    }
}

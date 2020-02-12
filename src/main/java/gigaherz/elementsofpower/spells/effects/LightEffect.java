package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.blocks.BlockLight;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.*;

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
            Vec3d hitVec = mop.getHitVec();
            cast.spawnRandomParticle(ParticleTypes.CRIT, hitVec.x, hitVec.y, hitVec.z);
        }
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, BlockState currentState, float r, @Nullable RayTraceResult mop)
    {
        if (mop != null && mop.getType() == RayTraceResult.Type.BLOCK)
        {
            blockPos = blockPos.offset(((BlockRayTraceResult)mop).getFace());
            currentState = cast.world.getBlockState(blockPos);
        }

        int density = MathHelper.clamp((int)(16-16*r),1,16);

        Block block = currentState.getBlock();

        if (block == Blocks.AIR)
        {
            cast.world.setBlockState(blockPos, ElementsOfPowerMod.light.getDefaultState().with(BlockLight.DENSITY, density));
        }
        else if (block == ElementsOfPowerMod.light)
        {
            ElementsOfPowerMod.light.resetCooldown(cast.world, blockPos, currentState, density);
        }
    }
}

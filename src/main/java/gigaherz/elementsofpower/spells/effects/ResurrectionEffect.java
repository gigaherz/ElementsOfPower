package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ResurrectionEffect
        extends SpellEffect
{

    @Override
    public int getColor(Spellcast cast)
    {
        return 0;
    }

    @Override
    public int getDuration(Spellcast cast)
    {
        return 0;
    }

    @Override
    public int getInterval(Spellcast cast)
    {
        return 0;
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity entity, Vec3d hitVec)
    {
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3d hitVec)
    {
        return false;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3d hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, RayTraceResult mop)
    {

    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float distance, @Nullable RayTraceResult mop)
    {
        // Resurrecting players could be done by
        // sending dimension packet or maybe respawn keeping items

        World world = cast.world;

        IBlockState state = world.getBlockState(blockPos);

        if (state.getBlock() == Blocks.DRAGON_EGG)
        {
            EntityDragon dragon = new EntityDragon(world);

            BlockPos spawnAt = world.getTopSolidOrLiquidBlock(blockPos).up(5);

            dragon.setLocationAndAngles(spawnAt.getX(), spawnAt.getY(), spawnAt.getZ(), world.rand.nextFloat() * 360.0F, 0.0F);

            if (world.spawnEntityInWorld(dragon))
            {
                world.setBlockToAir(blockPos);
            }
        }
    }
}

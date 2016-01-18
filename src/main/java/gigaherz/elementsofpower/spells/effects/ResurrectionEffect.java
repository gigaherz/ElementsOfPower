package gigaherz.elementsofpower.spells.effects;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

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
    public void processDirectHit(Spellcast cast, Entity e)
    {
    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        return false;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec)
    {

    }

    @Override
    public void spawnBallParticles(Spellcast cast, MovingObjectPosition mop)
    {

    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, float distance, MovingObjectPosition mop)
    {
        // Resurrecting players could be done by
        // sending dimension packet or maybe respawn keeping items

        World world = cast.world;

        IBlockState state = world.getBlockState(blockPos);

        if (state.getBlock() == Blocks.dragon_egg)
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

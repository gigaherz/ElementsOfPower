package dev.gigaherz.elementsofpower.spells.effects;

import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class ResurrectionEffect
        extends SpellEffect
{

    @Override
    public int getColor(SpellcastState cast)
    {
        return 0;
    }

    @Override
    public int getDuration(SpellcastState cast)
    {
        return 0;
    }

    @Override
    public int getInterval(SpellcastState cast)
    {
        return 0;
    }

    @Override
    public void processDirectHit(SpellcastState cast, Entity entity, Vec3 hitVec, Entity directEntity)
    {
    }

    @Override
    public boolean processEntitiesAroundBefore(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {
        return false;
    }

    @Override
    public void processEntitiesAroundAfter(SpellcastState cast, Vec3 hitVec, Entity directEntity)
    {

    }

    @Override
    public void spawnBallParticles(SpellcastState cast, HitResult mop)
    {

    }

    @Override
    public void processBlockWithinRadius(SpellcastState cast, BlockPos blockPos, BlockState currentState, float distance, @Nullable HitResult mop)
    {
        // Resurrecting players could be done by
        // sending dimension packet or maybe respawn keeping items

        Level world = cast.level();

        BlockState state = world.getBlockState(blockPos);

        if (state.getBlock() == Blocks.DRAGON_EGG)
        {
            EnderDragon dragon = EntityType.ENDER_DRAGON.create(world);

            // FIXME: figure out
            //BlockPos spawnAt = world.getTopSolidOrLiquidBlock(blockPos).up(5);
            BlockPos spawnAt = (blockPos).above(5);

            dragon.moveTo(spawnAt.getX(), spawnAt.getY(), spawnAt.getZ(), world.random.nextFloat() * 360.0F, 0.0F);

            if (world.addFreshEntity(dragon))
            {
                world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
        }
    }
}

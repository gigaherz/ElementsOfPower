package dev.gigaherz.elementsofpower.spells.shapes;

import com.google.common.collect.Lists;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LaserShape extends SpellShape
{
    @Override
    public float getScale(InitializedSpellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        return cast.init(player.level, player);
    }

    @Override
    public boolean isInstant()
    {
        return false;
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        HitResult mop = cast.getHitPosition();

        if (mop != null)
        {
            Vec3 diff = mop.getLocation().subtract(cast.start);

            List<BlockPos> intersections = getAllBlocksInRay(cast.start, diff.normalize(), diff.length());

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

            for (BlockPos pos : intersections)
            {
                cast.getEffect().processBlockWithinRadius(cast, pos, cast.world.getBlockState(pos), 0, null);
            }
        }
    }

    private List<BlockPos> getAllBlocksInRay(Vec3 start, Vec3 look, double range)
    {
        List<BlockPos> intersections = Lists.newArrayList();
        intersections.add(new BlockPos(start.x, start.y, start.z));
        look = look.normalize();
        Vec3 pos = start;
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);
        BlockPos block = new BlockPos(start);
        while (true)
        {
            // begin
            double maxTime = (range - pos.subtract(start).length()) / look.length();

            // X crossing
            double xTime;
            double xBoundary = pos.x;
            int xBlock = block.getX();
            if (look.x > 0)
            {
                xBoundary = xBlock + 1;
                xBlock = xBlock + 1;
                xTime = ((xBoundary - pos.x) / look.x);
            }
            else if (look.x < 0)
            {
                xBoundary = block.getX();
                xBlock = xBlock - 1;
                xTime = ((xBoundary - pos.x) / look.x); // the subtraction will result negative, but since look.x is also negative, the result will be positive
            }
            else
            {
                xTime = Double.POSITIVE_INFINITY;
            }

            double yTime;
            double yBoundary = pos.y;
            int yBlock = block.getY();
            if (look.y > 0)
            {
                yBoundary = yBlock + 1;
                yBlock = yBlock + 1;
                yTime = ((yBoundary - pos.y) / look.y);
            }
            else if (look.y < 0)
            {
                yBoundary = yBlock;
                yBlock = yBlock - 1;
                yTime = ((yBoundary - pos.y) / look.y);
            }
            else
            {
                yTime = Double.POSITIVE_INFINITY;
            }

            double zTime;
            double zBoundary = pos.z;
            int zBlock = block.getZ();
            if (look.z > 0)
            {
                zBoundary = zBlock + 1;
                zBlock = zBlock + 1;
                zTime = ((zBoundary - pos.z) / look.z);
            }
            else if (look.z < 0)
            {
                zBoundary = zBlock;
                zBlock = zBlock - 1;
                zTime = ((zBoundary - pos.z) / look.z);
            }
            else
            {
                zTime = Double.POSITIVE_INFINITY;
            }

            BlockPos currentBlock = block;
            Vec3 oldPos = pos;

            boolean xWins = false, yWins = false, zWins = false;
            if (xTime <= yTime && xTime <= zTime && xTime <= maxTime)
            {
                xWins = true;
                block = new BlockPos(xBlock, block.getY(), block.getZ());
            }
            if (yTime <= xTime && yTime <= zTime && yTime <= maxTime)
            {
                yWins = true;
                block = new BlockPos(block.getX(), yBlock, block.getZ());
            }
            else if (zTime <= xTime && zTime <= yTime && zTime <= maxTime)
            {
                zWins = true;
                block = new BlockPos(block.getX(), block.getY(), zBlock);
            }

            if (xWins)
            {
                pos = new Vec3(xBoundary, pos.y + look.y * xTime, pos.z + look.z * xTime);
            }
            else if (yWins)
            {
                pos = new Vec3(pos.x + look.x * yTime, yBoundary, pos.z + look.z * yTime);
            }
            else if (zWins)
            {
                pos = new Vec3(pos.x + look.x * zTime, pos.y + look.y * zTime, zBoundary);
            }
            else
            {
                pos = end;
            }

            if (testBoundingBox(currentBlock, oldPos, pos))
            {
                intersections.add(block);
            }

            if (!xWins && !yWins && !zWins)
            {
                break;
            }
        }
        return intersections;
    }

    private boolean testBoundingBox(BlockPos currentBlock, Vec3 oldPos, Vec3 pos)
    {
        return true;
    }
}
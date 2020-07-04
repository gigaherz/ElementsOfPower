package gigaherz.elementsofpower.spells.shapes;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class LaserShape extends SpellShape
{
    @Override
    public float getScale(InitializedSpellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public InitializedSpellcast castSpell(ItemStack stack, PlayerEntity player, Spellcast cast)
    {
        return cast.init(player.world, player);
    }

    @Override
    public boolean isInstant()
    {
        return false;
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        RayTraceResult mop = cast.getHitPosition();

        if (mop != null)
        {
            Vector3d diff = mop.getHitVec().subtract(cast.start);

            List<BlockPos> intersections = getAllBlocksInRay(cast.start, diff.normalize(), diff.length());

            if (mop.getType() == RayTraceResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, ((EntityRayTraceResult) mop).getEntity(), mop.getHitVec());
            }
            else if (mop.getType() == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = ((BlockRayTraceResult) mop).getPos();
                BlockState state = cast.world.getBlockState(pos);
                cast.getEffect().processBlockWithinRadius(cast, pos, state, 0, mop);
            }

            for (BlockPos pos : intersections)
            {
                cast.getEffect().processBlockWithinRadius(cast, pos, cast.world.getBlockState(pos), 0, null);
            }
        }
    }

    private List<BlockPos> getAllBlocksInRay(Vector3d start, Vector3d look, double range)
    {
        List<BlockPos> intersections = Lists.newArrayList();
        intersections.add(new BlockPos(start.x, start.y, start.z));
        look = look.normalize();
        Vector3d pos = start;
        Vector3d end = start.add(look.x * range, look.y * range, look.z * range);
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
            Vector3d oldPos = pos;

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
                pos = new Vector3d(xBoundary, pos.y + look.y * xTime, pos.z + look.z * xTime);
            }
            else if (yWins)
            {
                pos = new Vector3d(pos.x + look.x * yTime, yBoundary, pos.z + look.z * yTime);
            }
            else if (zWins)
            {
                pos = new Vector3d(pos.x + look.x * zTime, pos.y + look.y * zTime, zBoundary);
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

    private boolean testBoundingBox(BlockPos currentBlock, Vector3d oldPos, Vector3d pos)
    {
        return true;
    }
}
package gigaherz.elementsofpower.spells.shapes;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class LaserShape extends SpellShape
{
    @Override
    public float getScale(Spellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
    {
        return cast;
    }

    @Override
    public boolean isInstant()
    {
        return false;
    }

    @Override
    public void spellTick(Spellcast cast)
    {
        RayTraceResult mop = cast.getHitPosition();

        if (mop != null)
        {
            Vec3d diff = mop.hitVec.subtract(cast.start);

            List<BlockPos> intersections = getAllBlocksInRay(cast.start, diff.normalize(), diff.length());

            if (mop.typeOfHit == RayTraceResult.Type.ENTITY)
            {
                cast.getEffect().processDirectHit(cast, mop.entityHit, mop.hitVec);
            }
            else if (mop.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = mop.getBlockPos();
                IBlockState state = cast.world.getBlockState(pos);
                cast.getEffect().processBlockWithinRadius(cast, pos, state, 0, mop);
            }

            for(BlockPos pos : intersections)
            {
                cast.getEffect().processBlockWithinRadius(cast, pos, cast.world.getBlockState(pos), 0, null);
            }
        }
    }

    private List<BlockPos> getAllBlocksInRay(Vec3d start, Vec3d look, double range)
    {
        List<BlockPos> intersections = Lists.newArrayList();
        intersections.add(new BlockPos(start.x, start.y, start.z));
        look = look.normalize();
        Vec3d pos = start;
        Vec3d end = start.add(look.x*range,look.y*range,look.z*range);
        BlockPos block = new BlockPos(start);
        while(true)
        {
            // begin
            double maxTime = (range-pos.subtract(start).length())/look.length();

            // X crossing
            double xTime;
            double xBoundary = pos.x;
            int xBlock = block.getX();
            if (look.x > 0)
            {
                xBoundary = xBlock+1;
                xBlock = xBlock+1;
                xTime = ((xBoundary - pos.x) / look.x);
            }
            else if (look.x < 0)
            {
                xBoundary = block.getX();
                xBlock = xBlock-1;
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
                yBoundary = yBlock+1;
                yBlock = yBlock+1;
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
                zBoundary = zBlock+1;
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
            Vec3d oldPos = pos;

            boolean xWins = false, yWins = false, zWins = false;
            if(xTime <= yTime && xTime <= zTime && xTime <= maxTime)
            {
                xWins = true;
                block = new BlockPos(xBlock,block.getY(),block.getZ());
            }
            if(yTime <= xTime && yTime <= zTime && yTime <= maxTime)
            {
                yWins = true;
                block = new BlockPos(block.getX(),yBlock,block.getZ());
            }
            else if(zTime <= xTime && zTime <= yTime && zTime <= maxTime)
            {
                zWins = true;
                block = new BlockPos(block.getX(),block.getY(),zBlock);
            }

            if(xWins)
            {
                pos = new Vec3d(xBoundary, pos.y + look.y * xTime, pos.z + look.z * xTime);
            }
            else if(yWins)
            {
                pos = new Vec3d(pos.x + look.x * yTime, yBoundary, pos.z + look.z * yTime);
            }
            else if(zWins)
            {
                pos = new Vec3d(pos.x + look.x * zTime, pos.y + look.y * zTime, zBoundary);
            }
            else {
                pos = end;
            }

            if (testBoundingBox(currentBlock, oldPos, pos))
            {
                intersections.add(block);
            }

            if(!xWins && !yWins && !zWins)
            {
                break;
            }
        }
        return intersections;
    }

    private boolean testBoundingBox(BlockPos currentBlock, Vec3d oldPos, Vec3d pos)
    {
        return true;
    }
}
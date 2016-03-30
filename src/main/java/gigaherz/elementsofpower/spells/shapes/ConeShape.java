package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

import java.util.List;

public class ConeShape extends SpellShape
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

    private Vec3 scale(Vec3 vec, double scale)
    {
        return new Vec3(vec.xCoord * scale, vec.yCoord * scale, vec.zCoord * scale);
    }

    private AxisAlignedBB getConeBounds(Spellcast cast)
    {
        EntityPlayer player = cast.player;

        float length = 4; // cone length
        float radius = 2;
        float hyp = (float) Math.sqrt(radius * radius + length * length);

        Vec3 p0 = new Vec3(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);

        float y = player.rotationYawHead;
        float p = player.rotationPitch;
        float f = (float) Math.toDegrees(Math.atan2(radius, length));

        Vec3 q0 = p0.add(scale(getVectorForRotation(p - f, y), hyp));
        Vec3 q1 = p0.add(scale(getVectorForRotation(p + f, y), hyp));
        Vec3 q2 = p0.add(scale(getVectorForRotation(p, y - f), hyp));
        Vec3 q3 = p0.add(scale(getVectorForRotation(p, y + f), hyp));

        float mx = (float) max5(p0.xCoord, q0.xCoord, q1.xCoord, q2.xCoord, q3.xCoord);
        float nx = (float) min5(p0.xCoord, q0.xCoord, q1.xCoord, q2.xCoord, q3.xCoord);
        float my = (float) max5(p0.yCoord, q0.yCoord, q1.yCoord, q2.yCoord, q3.yCoord);
        float ny = (float) min5(p0.yCoord, q0.yCoord, q1.yCoord, q2.yCoord, q3.yCoord);
        float mz = (float) max5(p0.zCoord, q0.zCoord, q1.zCoord, q2.zCoord, q3.zCoord);
        float nz = (float) min5(p0.zCoord, q0.zCoord, q1.zCoord, q2.zCoord, q3.zCoord);

        return new AxisAlignedBB(mx, my, mz, nx, ny, nz);
    }

    static double max5(double a, double b, double c, double d, double e)
    {
        return Math.max(Math.max(Math.max(Math.max(a, b), c), d), e);
    }

    static double min5(double a, double b, double c, double d, double e)
    {
        return Math.min(Math.min(Math.min(Math.min(a, b), c), d), e);
    }

    static Vec3 getVectorForRotation(float pitch, float yaw)
    {
        float yc = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float ys = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float pc = -MathHelper.cos(-pitch * 0.017453292F);
        float ps = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(ys * pc, ps, yc * pc);
    }

    private boolean isPointInCone(Spellcast cast, Vec3 point)
    {
        EntityPlayer player = cast.player;

        float length = 4; // cone length
        float radius = 2;

        float y = player.rotationYawHead;
        float p = player.rotationPitch;
        double f = Math.atan2(radius, length);

        Vec3 p0 = new Vec3(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);
        Vec3 ab = getVectorForRotation(p, y);
        Vec3 abs = scale(ab, length);
        Vec3 ap = point.subtract(p0);

        double dot1 = ap.dotProduct(abs);
        double dot2 = abs.dotProduct(abs);

        double pd = (dot1 / dot2) * ab.lengthVector();

        if (pd > length)
            return false;

        double ax = Math.acos(ap.normalize().dotProduct(ab));

        return (ax <= f);
    }

    @Override
    public void spellTick(Spellcast cast)
    {
        Vec3 hitVec = new Vec3(
                cast.player.posX,
                cast.player.posY + (double) cast.player.getEyeHeight(),
                cast.player.posZ);

        final AxisAlignedBB aabb = getConeBounds(cast);

        List<Entity> entities = cast.world.getEntitiesWithinAABBExcludingEntity(cast.player, aabb);

        for (Entity e : entities)
        {
            Vec3 pt;
            AxisAlignedBB aabb_ = e.getEntityBoundingBox();
            if (aabb_ != null)
            {
                pt = new Vec3(
                        (aabb_.minX + aabb_.maxX) * 0.5,
                        (aabb_.minY + aabb_.maxY) * 0.5,
                        (aabb_.minZ + aabb_.maxZ) * 0.5);
            }
            else
            {
                pt = new Vec3(e.posX, e.posY + e.getEyeHeight() * 0.5, e.posZ);
            }

            if (isPointInCone(cast, pt))
                cast.getEffect().processDirectHit(cast, e, hitVec);
        }

        int nx = (int) Math.floor(aabb.minX);
        int mx = (int) Math.ceil(aabb.maxX);
        int ny = (int) Math.floor(aabb.minY);
        int my = (int) Math.ceil(aabb.maxY);
        int nz = (int) Math.floor(aabb.minZ);
        int mz = (int) Math.ceil(aabb.maxZ);

        for (int y = ny; y <= my; y++)
        {
            for (int z = nz; z <= mz; z++)
            {
                for (int x = nx; x <= mx; x++)
                {
                    Vec3 pt = new Vec3(x + 0.5, y + 0.5, z + 0.5);
                    if (isPointInCone(cast, pt))
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        IBlockState state = cast.world.getBlockState(pos);

                        Vec3 off = pt.subtract(hitVec);
                        MovingObjectPosition mop = new MovingObjectPosition(pt, EnumFacing.getFacingFromVector((float) off.xCoord, (float) off.yCoord, (float) off.zCoord), pos);
                        cast.getEffect().processBlockWithinRadius(cast, pos, state, (float) pt.distanceTo(hitVec), mop);
                    }
                }
            }
        }
    }
}
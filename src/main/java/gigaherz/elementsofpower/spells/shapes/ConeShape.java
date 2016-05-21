package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;

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

    private AxisAlignedBB getConeBounds(Spellcast cast)
    {
        EntityPlayer player = cast.player;

        float length = 4; // cone length
        float radius = 2;
        float hyp = (float) Math.sqrt(radius * radius + length * length);

        Vec3d p0 = new Vec3d(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);

        float y = player.rotationYawHead;
        float p = player.rotationPitch;
        float f = (float) Math.toDegrees(Math.atan2(radius, length));

        Vec3d q0 = p0.add(getVectorForRotation(p - f, y).scale(hyp));
        Vec3d q1 = p0.add(getVectorForRotation(p + f, y).scale(hyp));
        Vec3d q2 = p0.add(getVectorForRotation(p, y - f).scale(hyp));
        Vec3d q3 = p0.add(getVectorForRotation(p, y + f).scale(hyp));

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

    static Vec3d getVectorForRotation(float pitch, float yaw)
    {
        float yc = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float ys = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float pc = -MathHelper.cos(-pitch * 0.017453292F);
        float ps = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(ys * pc, ps, yc * pc);
    }

    private boolean isPointInCone(Spellcast cast, Vec3d point)
    {
        EntityPlayer player = cast.player;

        float length = 4; // cone length
        float radius = 2;

        float y = player.rotationYawHead;
        float p = player.rotationPitch;
        double f = Math.atan2(radius, length);

        Vec3d p0 = new Vec3d(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);
        Vec3d ab = getVectorForRotation(p, y);
        Vec3d abs = ab.scale(length);
        Vec3d ap = point.subtract(p0);

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
        Vec3d hitVec = new Vec3d(
                cast.player.posX,
                cast.player.posY + (double) cast.player.getEyeHeight(),
                cast.player.posZ);

        final AxisAlignedBB aabb = getConeBounds(cast);

        List<Entity> entities = cast.world.getEntitiesInAABBexcluding(cast.player, aabb, (e) -> {
            Vec3d pt;
            AxisAlignedBB aabb_ = e.getEntityBoundingBox();
            pt = new Vec3d(
                    (aabb_.minX + aabb_.maxX) * 0.5,
                    (aabb_.minY + aabb_.maxY) * 0.5,
                    (aabb_.minZ + aabb_.maxZ) * 0.5);
            return isPointInCone(cast, pt);
        });

        for (Entity e : entities)
        {

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
                    Vec3d pt = new Vec3d(x + 0.5, y + 0.5, z + 0.5);
                    if (isPointInCone(cast, pt))
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        IBlockState state = cast.world.getBlockState(pos);

                        Vec3d off = pt.subtract(hitVec);
                        RayTraceResult mop = new RayTraceResult(pt, EnumFacing.getFacingFromVector((float) off.xCoord, (float) off.yCoord, (float) off.zCoord), pos);
                        cast.getEffect().processBlockWithinRadius(cast, pos, state, (float) pt.distanceTo(hitVec), mop);
                    }
                }
            }
        }
    }
}
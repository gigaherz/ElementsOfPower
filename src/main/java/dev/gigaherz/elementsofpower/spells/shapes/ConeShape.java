package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ConeShape extends SpellShape
{
    @Override
    public float getScale(InitializedSpellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        return cast.init(player.level(), player);
    }

    @Override
    public boolean isInstant()
    {
        return false;
    }

    private AABB getConeBounds(InitializedSpellcast cast)
    {
        Player player = cast.player;

        float length = 4 + cast.getDamageForce() * 0.5f; // cone length
        float radius = 2;
        float hyp = (float) Math.sqrt(radius * radius + length * length);

        Vec3 p0 = player.getEyePosition(1.0f);

        float y = player.yHeadRot;
        float p = player.getXRot();
        float f = (float) Math.toDegrees(Math.atan2(radius, length));

        Vec3 v0 = (getVectorFromRPY(hyp, y, p, 0 - f));
        Vec3 v1 = (getVectorFromRPY(hyp, y, p, 0 + f));
        Vec3 v2 = (getVectorFromRPY(hyp, y, p - f, 0));
        Vec3 v3 = (getVectorFromRPY(hyp, y, p + f, 0));

        Vec3 q0 = p0.add(v0);
        Vec3 q1 = p0.add(v1);
        Vec3 q2 = p0.add(v2);
        Vec3 q3 = p0.add(v3);

        float mx = (float) max5(p0.x, q0.x, q1.x, q2.x, q3.x);
        float nx = (float) min5(p0.x, q0.x, q1.x, q2.x, q3.x);
        float my = (float) max5(p0.y, q0.y, q1.y, q2.y, q3.y);
        float ny = (float) min5(p0.y, q0.y, q1.y, q2.y, q3.y);
        float mz = (float) max5(p0.z, q0.z, q1.z, q2.z, q3.z);
        float nz = (float) min5(p0.z, q0.z, q1.z, q2.z, q3.z);

        return new AABB(mx, my, mz, nx, ny, nz);
    }

    private Vec3 getVectorFromRPY(double length, double y, double p, double r)
    {
        y = Math.toRadians(y + 90);
        p = Math.toRadians(180 - p);
        r = Math.toRadians(r + 180);
        double y2 = y + Math.toRadians(90);

        double ll = length * Math.cos(r);
        double ss = length * Math.sin(r);

        double xz = ll * Math.cos(p);
        double yy = ll * Math.sin(p);

        double xl = xz * Math.cos(y);
        double zl = xz * Math.sin(y);
        double xs = ss * Math.cos(y2);
        double zs = ss * Math.sin(y2);

        double xx = xl + xs;
        double zz = zl + zs;

        return new Vec3(xx, yy, zz);
    }

    private static double max5(double a, double b, double c, double d, double e)
    {
        return Math.max(Math.max(Math.max(Math.max(a, b), c), d), e);
    }

    private static double min5(double a, double b, double c, double d, double e)
    {
        return Math.min(Math.min(Math.min(Math.min(a, b), c), d), e);
    }

    private boolean isPointInCone(InitializedSpellcast cast, Vec3 point)
    {
        Player player = cast.player;

        double length = 4 + cast.getDamageForce() * 0.5f; // cone length
        double radius = 2;
        double tang = radius / length;

        double y = player.yHeadRot;
        double p = player.getXRot();
        double f = Math.abs(Math.atan(tang));

        Vec3 p0 = player.getEyePosition(1.0f);
        Vec3 ab = getVectorFromRPY(length, y, p, 0);
        Vec3 ap = point.subtract(p0);

        double dot1 = ap.dot(ab);
        double dot2 = ab.dot(ab);

        double pd = (dot1 / dot2) * ab.length();

        if (pd > length)
            return false;

        double ax = Math.acos(ap.normalize().dot(ab.normalize()));

        return (ax <= f);
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        Vec3 playerPos = cast.player.getEyePosition(1.0f);

        final AABB aabb = getConeBounds(cast);

        List<Entity> entities = cast.world.getEntities(cast.player, aabb, (e) ->
        {
            if (e == null)
                return false;
            AABB aabb_ = e.getBoundingBox();
            for (int i = 0; i <= 2; i++)
            {
                double x = Mth.clampedLerp(aabb_.minX, aabb_.maxX, i / 2.0);
                for (int j = 0; j <= 2; j++)
                {
                    double y = Mth.clampedLerp(aabb_.minY, aabb_.maxY, j / 2.0);
                    for (int k = 0; k <= 2; k++)
                    {
                        double z = Mth.clampedLerp(aabb_.minZ, aabb_.maxZ, k / 2.0);

                        if (isPointInCone(cast, new Vec3(x, y, z)))
                            return true;
                    }
                }
            }
            return false;
        });

        for (Entity e : entities)
        {
            cast.getEffect().processDirectHit(cast, e, playerPos);
        }

        int x0 = (int) Math.floor(aabb.minX);
        int x1 = (int) Math.ceil(aabb.maxX);
        int y0 = (int) Math.floor(aabb.minY);
        int y1 = (int) Math.ceil(aabb.maxY);
        int z0 = (int) Math.floor(aabb.minZ);
        int z1 = (int) Math.ceil(aabb.maxZ);

        //Set<FacePos> seenFaces = Sets.newHashSet();
        for (int y = y0; y <= y1; y++)
        {
            for (int z = z0; z <= z1; z++)
            {
                for (int x = x0; x <= x1; x++)
                {
                    Vec3 pt = new Vec3(x + 0.5, y + 0.5, z + 0.5);
                    if (isPointInCone(cast, pt))
                    {
                        BlockHitResult mop = cast.world.clip(new ClipContext(playerPos, pt, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, cast.player));
                        if (mop.getType() != HitResult.Type.MISS)
                        {
                            BlockPos pos = mop.getBlockPos();
                            BlockState state = cast.world.getBlockState(pos);

                            cast.getEffect().processBlockWithinRadius(cast, pos, state, (float) pt.distanceTo(playerPos), mop);
                        }
                        else
                        {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = cast.world.getBlockState(pos);
                            cast.getEffect().processBlockWithinRadius(cast, pos, state, (float) pt.distanceTo(playerPos), null);
                        }
                    }
                }
            }
        }
    }
}
package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class InitializedSpellcast extends Spellcast
{
    public World world;
    public PlayerEntity player;
    public int remainingCastTime;
    public int remainingInterval;
    public int totalCastTime;

    protected InitializedSpellcast(List<Element> sequence, Vector3d start, Vector3d end, SpellShape shape, SpellEffect effect, Entity projectile, int power, Random rand, int empowering, int radiating, MagicAmounts spellCost, World world, PlayerEntity player)
    {
        super(sequence, start, end, shape, effect, projectile, power, rand, empowering, radiating, spellCost);
        this.world = world;
        this.player = player;
        if (shape.isInstant())
        {
            remainingCastTime = shape.getInstantAnimationLength();
            remainingInterval = 0;
        }
        else
        {
            remainingCastTime = effect.getDuration(this);
            remainingInterval = effect.getInterval(this);
        }
        totalCastTime = remainingCastTime;
    }

    public int getDamageForce()
    {
        return Math.max(0, power - effect.getForceModifier(this));
    }

    public int getColor()
    {
        return effect.getColor(this);
    }

    public float getScale()
    {
        return shape.getScale(this);
    }

    public void onImpact(RayTraceResult mop, Random rand)
    {
        this.rand = rand;
        if (!world.isRemote)
        {
            shape.onImpact(this, mop);
        }
    }

    public void readFromNBT(CompoundNBT tagData)
    {
        remainingCastTime = tagData.getInt("remainingCastTime");
        remainingInterval = tagData.getInt("remainingInterval");
        totalCastTime = tagData.getInt("totalCastTime");
    }

    public void writeToNBT(CompoundNBT tagData)
    {
        tagData.putInt("remainingCastTime", remainingCastTime);
        tagData.putInt("remainingInterval", remainingInterval);
        tagData.putInt("totalCastTime", totalCastTime);
        tagData.put("sequence", getSequenceNBT());
    }

    public void update()
    {
        if (shape.isInstant() && remainingCastTime == totalCastTime)
        {
            if (!world.isRemote)
            {
                shape.spellTick(this);
            }
        }

        remainingCastTime--;

        if (!shape.isInstant())
        {
            remainingInterval--;

            if (remainingInterval <= 0)
            {
                remainingInterval = effect.getInterval(this);

                if (!world.isRemote)
                {
                    shape.spellTick(this);
                }
            }
        }

        if (remainingCastTime <= 0)
        {
            SpellcastEntityData.get(player).ifPresent(SpellcastEntityData::end);
        }
    }

    public PlayerEntity getCastingPlayer()
    {
        return player;
    }

    public float getRandomForParticle()
    {
        return (rand.nextFloat() - 0.5f) * power / 8.0f;
    }

    public void spawnRandomParticle(IParticleData type, double x, double y, double z)
    {
        world.addParticle(type, x, y, z, getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
    }

    // Butchered from the player getMouseOver()
    @Nullable
    public RayTraceResult getEntityIntercept(Vector3d start, Vector3d look, Vector3d end,
                                             @Nullable RayTraceResult mop)
    {
        double distance = end.distanceTo(start);

        if (mop != null && mop.getType() != RayTraceResult.Type.MISS)
        {
            distance = mop.getHitVec().distanceTo(start);
        }

        Vector3d direction = new Vector3d(
                look.x * distance,
                look.y * distance,
                look.z * distance);

        end = start.add(direction);

        Vector3d hitPosition = null;
        List<Entity> list = world.getEntitiesInAABBexcluding(player,
                player.getBoundingBox()
                        .expand(direction.x, direction.y, direction.z)
                        .grow(1.0, 1.0, 1.0),
                entity -> EntityPredicates.NOT_SPECTATING.test(entity) && entity.canBeCollidedWith());

        double distanceToEntity = distance;
        Entity pointedEntity = null;
        for (Entity entity : list)
        {
            double border = entity.getCollisionBorderSize();
            AxisAlignedBB bounds = entity.getBoundingBox().expand(border, border, border);
            Optional<Vector3d> intercept = bounds.rayTrace(start, end);

            if (bounds.contains(start))
            {
                if (distanceToEntity >= 0.0D)
                {
                    pointedEntity = entity;
                    hitPosition = intercept.orElse(start);
                    distanceToEntity = 0.0D;
                }
            }
            else if (intercept.isPresent())
            {
                double interceptDistance = start.distanceTo(intercept.get());

                if (interceptDistance < distanceToEntity || distanceToEntity == 0.0D)
                {
                    if (entity == player.getRidingEntity() && !player.canRiderInteract())
                    {
                        if (distanceToEntity == 0.0D)
                        {
                            pointedEntity = entity;
                            hitPosition = intercept.get();
                        }
                    }
                    else
                    {
                        pointedEntity = entity;
                        hitPosition = intercept.get();
                        distanceToEntity = interceptDistance;
                    }
                }
            }
        }

        if (pointedEntity != null)
        {
            if (distanceToEntity < distance)
            {
                if (start.distanceTo(hitPosition) < distance)
                {
                    return new EntityRayTraceResult(pointedEntity, hitPosition);
                }
            }
        }

        return mop;
    }

    // Called by the client on render, and by the server as needed
    @Nullable
    public RayTraceResult getHitPosition()
    {
        return getHitPosition(1);
    }

    @Nullable
    public RayTraceResult getHitPosition(float partialTicks)
    {
        float maxDistance = 10;

        calculateStartPosition(partialTicks);

        Vector3d look = player.getLook(partialTicks);
        end = start.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);

        // FIXME
        BlockRayTraceResult blockTrace = world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));

        RayTraceResult trace = getEntityIntercept(start, look, end, blockTrace);

        if (trace != null && trace.getType() != RayTraceResult.Type.MISS)
        {
            end = trace.getHitVec();
        }

        return trace;
    }

    public Vector3d calculateStartPosition(float partialTicks)
    {
        if (partialTicks < 1)
        {
            double sx = player.prevPosX + (player.getPosX() - player.prevPosX) * partialTicks;
            double sy = player.prevPosY + (player.getPosY() - player.prevPosY) * partialTicks + player.getEyeHeight();
            double sz = player.prevPosZ + (player.getPosZ() - player.prevPosZ) * partialTicks;
            start = new Vector3d(sx, sy, sz);
        }
        else
        {
            start = player.getEyePosition(1.0f);
        }
        return start;
    }
}
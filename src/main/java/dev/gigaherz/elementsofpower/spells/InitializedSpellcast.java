package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class InitializedSpellcast extends Spellcast
{
    public Level world;
    public Player player;
    public int remainingCastTime;
    public int remainingInterval;
    public int totalCastTime;

    protected InitializedSpellcast(List<Element> sequence, SpellShape shape, SpellEffect effect, Entity projectile, int power, RandomSource rand, int empowering, int radiating, MagicAmounts spellCost, Level world, Player player)
    {
        super(sequence, shape, effect, projectile, power, rand, empowering, radiating, spellCost);
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
        return Math.max(0, getPower() - getEffect().getForceModifier(this));
    }

    public int getColor()
    {
        return getEffect().getColor(this);
    }

    public float getScale()
    {
        return getShape().getScale(this);
    }

    public void onImpact(HitResult mop, RandomSource rand)
    {
        this.setRandom(rand);
        if (!world.isClientSide)
        {
            getShape().onImpact(this, mop);
        }
    }

    public void readFromNBT(CompoundTag tagData)
    {
        remainingCastTime = tagData.getInt("remainingCastTime");
        remainingInterval = tagData.getInt("remainingInterval");
        totalCastTime = tagData.getInt("totalCastTime");
    }

    public void writeToNBT(CompoundTag tagData)
    {
        tagData.putInt("remainingCastTime", remainingCastTime);
        tagData.putInt("remainingInterval", remainingInterval);
        tagData.putInt("totalCastTime", totalCastTime);
        tagData.put("sequence", getSequenceNBT());
    }

    public void update()
    {
        if (getShape().isInstant() && remainingCastTime == totalCastTime)
        {
            if (!world.isClientSide)
            {
                getShape().spellTick(this);
            }
        }

        remainingCastTime--;

        if (!getShape().isInstant())
        {
            remainingInterval--;

            if (remainingInterval <= 0)
            {
                remainingInterval = getEffect().getInterval(this);

                if (!world.isClientSide)
                {
                    getShape().spellTick(this);
                }
            }
        }

        if (remainingCastTime <= 0)
        {
            SpellcastEntityData.get(player).ifPresent(SpellcastEntityData::end);
        }
    }

    public Player getCastingPlayer()
    {
        return player;
    }

    public float getRandomForParticle()
    {
        return (getRandom().nextFloat() - 0.5f) * getPower() / 8.0f;
    }

    public void spawnRandomParticle(ParticleOptions type, double x, double y, double z)
    {
        world.addParticle(type, x, y, z, getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
    }

    // Butchered from the player getMouseOver()
    @Nullable
    public HitResult getEntityIntercept(Vec3 start, Vec3 look, Vec3 end,
                                        @Nullable HitResult mop)
    {
        double distance = end.distanceTo(start);

        if (mop != null && mop.getType() != HitResult.Type.MISS)
        {
            distance = mop.getLocation().distanceTo(start);
        }

        Vec3 direction = new Vec3(
                look.x * distance,
                look.y * distance,
                look.z * distance);

        end = start.add(direction);

        Vec3 hitPosition = null;
        List<Entity> list = world.getEntities(player,
                player.getBoundingBox()
                        .expandTowards(direction.x, direction.y, direction.z)
                        .inflate(1.0, 1.0, 1.0),
                entity -> EntitySelector.NO_SPECTATORS.test(entity) && entity.isPickable());

        double distanceToEntity = distance;
        Entity pointedEntity = null;
        for (Entity entity : list)
        {
            double border = entity.getPickRadius();
            AABB bounds = entity.getBoundingBox().expandTowards(border, border, border);
            Optional<Vec3> intercept = bounds.clip(start, end);

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
                    if (entity == player.getVehicle() && !player.canRiderInteract())
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
                    return new EntityHitResult(pointedEntity, hitPosition);
                }
            }
        }

        return mop;
    }

    // Rendering data;
    private Vec3 start;
    private Vec3 end;

    // Called by the client on render, and by the server as needed
    @Nullable
    public HitResult getHitPosition()
    {
        return getHitPosition(1);
    }

    @Nullable
    public HitResult getHitPosition(float partialTicks)
    {
        float maxDistance = 10;

        calculateStartPosition(partialTicks);

        Vec3 look = player.getViewVector(partialTicks);
        end = start.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);

        // FIXME
        BlockHitResult blockTrace = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        HitResult trace = getEntityIntercept(start, look, end, blockTrace);

        if (trace != null && trace.getType() != HitResult.Type.MISS)
        {
            end = trace.getLocation();
        }

        return trace;
    }

    public Vec3 calculateStartPosition(float partialTicks)
    {
        if (partialTicks < 1)
        {
            double sx = player.xo + (player.getX() - player.xo) * partialTicks;
            double sy = player.yo + (player.getY() - player.yo) * partialTicks + player.getEyeHeight();
            double sz = player.zo + (player.getZ() - player.zo) * partialTicks;
            start = new Vec3(sx, sy, sz);
        }
        else
        {
            start = player.getEyePosition(1.0f);
        }
        return start;
    }

    public Vec3 getStart()
    {
        return start;
    }

    public Vec3 getEnd()
    {
        return end;
    }
}
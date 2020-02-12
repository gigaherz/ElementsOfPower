package gigaherz.elementsofpower.spells;

import com.google.common.base.Predicates;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Spellcast
{
    public final String sequence;
    public int remainingCastTime;
    public int remainingInterval;
    public int totalCastTime;

    // Rendering data;
    public Vec3d start;
    public Vec3d end;

    protected SpellShape shape;
    protected SpellEffect effect;

    public World world;
    public PlayerEntity player;

    public Entity projectile;

    public int power;

    public Random rand;
    private int empowering;
    private int radiating;
    private MagicAmounts spellCost;

    public Spellcast(SpellShape shape, SpellEffect effect, int power, String sequence)
    {
        this.shape = shape;
        this.effect = effect;
        this.power = power;
        this.sequence = sequence;
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

    public String getSequence()
    {
        return sequence;
    }

    public void setProjectile(Entity entity)
    {
        projectile = entity;
    }

    public void init(World world, PlayerEntity player)
    {
        this.world = world;
        this.player = player;
    }

    public SpellShape getShape()
    {
        return shape;
    }

    public SpellEffect getEffect()
    {
        return effect;
    }

    public int getDamageForce()
    {
        return Math.max(0, power - effect.getForceModifier(this));
    }

    public void onImpact(RayTraceResult mop, Random rand)
    {
        this.rand = rand;
        if (!world.isRemote)
        {
            shape.onImpact(this, mop);
        }
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
    }

    public PlayerEntity getCastingPlayer()
    {
        return player;
    }

    public int getColor()
    {
        return effect.getColor(this);
    }

    public float getScale()
    {
        return shape.getScale(this);
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
    public RayTraceResult getEntityIntercept(Vec3d start, Vec3d look, Vec3d end,
                                             @Nullable RayTraceResult mop)
    {
        double distance = end.distanceTo(start);

        if (mop != null && mop.getType() != RayTraceResult.Type.MISS)
        {
            distance = mop.getHitVec().distanceTo(start);
        }

        Vec3d direction = new Vec3d(
                look.x * distance,
                look.y * distance,
                look.z * distance);

        end = start.add(direction);

        Vec3d hitPosition = null;
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
            Optional<Vec3d> intercept = bounds.rayTrace(start, end);

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

        Vec3d look = player.getLook(partialTicks);
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

    public Vec3d calculateStartPosition(float partialTicks)
    {
        if (partialTicks < 1)
        {
            double sx = player.prevPosX + (player.getPosX() - player.prevPosX) * partialTicks;
            double sy = player.prevPosY + (player.getPosY() - player.prevPosY) * partialTicks + player.getEyeHeight();
            double sz = player.prevPosZ + (player.getPosZ() - player.prevPosZ) * partialTicks;
            start = new Vec3d(sx, sy, sz);
        }
        else
        {
            start = player.getEyePosition(1.0f);
        }
        return start;
    }

    public void setEmpowering(int empowering)
    {
        this.empowering = empowering;
    }

    public int getEmpowering()
    {
        return empowering;
    }

    public void setRadiating(int radiating)
    {
        this.radiating = radiating;
    }

    public int getRadiating()
    {
        return radiating;
    }

    public MagicAmounts getSpellCost()
    {
        return spellCost;
    }

    public void setSpellCost(MagicAmounts spellCost)
    {
        this.spellCost = spellCost;
    }
}
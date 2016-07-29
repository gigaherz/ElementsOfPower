package gigaherz.elementsofpower.spells;

import com.google.common.base.Predicates;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
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
    public EntityPlayer player;

    public Entity projectile;

    public int power;

    public Random rand;
    private int empowering;
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

    public void init(World world, EntityPlayer player)
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
            SpellcastEntityData data = SpellcastEntityData.get(player);
            data.end();
        }
    }

    public void readFromNBT(NBTTagCompound tagData)
    {
        remainingCastTime = tagData.getInteger("remainingCastTime");
        remainingInterval = tagData.getInteger("remainingInterval");
        totalCastTime = tagData.getInteger("totalCastTime");
    }

    public void writeToNBT(NBTTagCompound tagData)
    {
        tagData.setInteger("remainingCastTime", remainingCastTime);
        tagData.setInteger("remainingInterval", remainingInterval);
        tagData.setInteger("totalCastTime", totalCastTime);
    }

    public EntityPlayer getCastingPlayer()
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

    public void spawnRandomParticle(EnumParticleTypes type, double x, double y, double z)
    {
        world.spawnParticle(type, x, y, z, getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
    }

    // Butchered from the player getMouseOver()
    public RayTraceResult getEntityIntercept(Vec3d start, Vec3d look, Vec3d end,
                                             RayTraceResult mop)
    {
        double distance = end.distanceTo(start);

        if (mop != null)
        {
            distance = mop.hitVec.distanceTo(start);
        }

        Vec3d direction = new Vec3d(
                look.xCoord * distance,
                look.yCoord * distance,
                look.zCoord * distance);

        end = start.add(direction);

        Vec3d hitPosition = null;
        List<Entity> list = world.getEntitiesInAABBexcluding(player,
                player.getEntityBoundingBox()
                        .addCoord(direction.xCoord, direction.yCoord, direction.zCoord)
                        .expand(1.0, 1.0, 1.0), Predicates.and(EntitySelectors.NOT_SPECTATING,
                        Entity::canBeCollidedWith));

        double distanceToEntity = distance;
        Entity pointedEntity = null;
        for (Entity entity : list)
        {
            double border = entity.getCollisionBorderSize();
            AxisAlignedBB bounds = entity.getEntityBoundingBox().expand(border, border, border);
            RayTraceResult intercept = bounds.calculateIntercept(start, end);

            if (bounds.isVecInside(start))
            {
                if (distanceToEntity >= 0.0D)
                {
                    pointedEntity = entity;
                    hitPosition = intercept == null ? start : intercept.hitVec;
                    distanceToEntity = 0.0D;
                }
            }
            else if (intercept != null)
            {
                double interceptDistance = start.distanceTo(intercept.hitVec);

                if (interceptDistance < distanceToEntity || distanceToEntity == 0.0D)
                {
                    if (entity == player.getRidingEntity() && !player.canRiderInteract())
                    {
                        if (distanceToEntity == 0.0D)
                        {
                            pointedEntity = entity;
                            hitPosition = intercept.hitVec;
                        }
                    }
                    else
                    {
                        pointedEntity = entity;
                        hitPosition = intercept.hitVec;
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
                    return new RayTraceResult(pointedEntity, hitPosition);
                }
            }
        }

        return mop;
    }

    // Called by the client on render, and by the server as needed
    public RayTraceResult getHitPosition()
    {
        return getHitPosition(1);
    }

    public RayTraceResult getHitPosition(float partialTicks)
    {
        float maxDistance = 10;

        if (partialTicks < 1)
        {
            double sx = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            double sy = player.prevPosY + (player.posY - player.prevPosY) * partialTicks + player.getEyeHeight();
            double sz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
            start = new Vec3d(sx, sy, sz);
        }
        else
        {
            start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        }

        Vec3d look = player.getLook(partialTicks);
        end = start.addVector(look.xCoord * maxDistance, look.yCoord * maxDistance, look.zCoord * maxDistance);

        RayTraceResult mop = player.worldObj.rayTraceBlocks(start, end, false, true, false);

        mop = getEntityIntercept(start, look, end, mop);

        if (mop != null && mop.hitVec != null)
        {
            end = mop.hitVec;
        }

        return mop;
    }

    public void setEmpowering(int empowering)
    {
        this.empowering = empowering;
    }

    public int getEmpowering()
    {
        return empowering;
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
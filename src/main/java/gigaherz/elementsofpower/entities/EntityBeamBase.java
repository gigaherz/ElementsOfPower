package gigaherz.elementsofpower.entities;

import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Map;

public abstract class EntityBeamBase extends Entity
{
    private static final int DATA_INDEX_CASTER = 5;

    protected String casterName;
    protected EntityLivingBase caster;
    protected int power;
    protected int effectInterval;

    protected int timeToLive;

    protected int effectTime;
    protected float maxDistance;

    protected Vec3 direction;
    protected Vec3 endPoint; // for rendering purposes only

    protected MovingObjectPosition hitInfo;

    protected EntityBeamBase(World worldIn)
    {
        super(worldIn);
    }

    protected EntityBeamBase(World worldIn, EntityLivingBase caster, float maxDistance, int power, int effectInterval, int timeToLive)
    {
        super(worldIn);
        this.caster = caster;
        this.maxDistance = maxDistance;
        this.power = power;
        this.effectInterval = effectInterval;
        this.timeToLive = timeToLive;
        this.effectTime = effectInterval;
    }

    @Override
    protected void entityInit()
    {
        DataWatcher dw = getDataWatcher();

        if(caster != null)
            dw.addObject(DATA_INDEX_CASTER, caster.getName());
    }

    protected void updateBeamPosition()
    {
        if(getCaster() == null)
            return;

        direction = caster.getLookVec();
        hitInfo = caster.rayTrace(maxDistance, 0);

        this.posX = caster.posX;
        this.posY = caster.posY + caster.getEyeHeight();
        this.posZ = caster.posX;

        if(hitInfo != null)
            endPoint = hitInfo.hitVec;
        else
            endPoint = new Vec3(
                    this.posX + direction.xCoord * maxDistance,
                    this.posY + direction.yCoord * maxDistance,
                    this.posZ + direction.zCoord * maxDistance);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        updateBeamPosition();

        if(!worldObj.isRemote)
        {
            timeToLive--;
            effectTime--;

            if (effectTime <= 0)
            {
                applyEffect();
                effectTime = effectInterval;
            }

            if (timeToLive <= 0)
            {
                this.setDead();
            }
        }
    }

    protected abstract void applyEffect();

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompound)
    {
        setCasterName(tagCompound.getString("ownerName"));
        power = tagCompound.getInteger("Power");
        effectInterval = tagCompound.getInteger("EffectInterval");
        timeToLive = tagCompound.getInteger("TimeToLive");
        effectTime = tagCompound.getInteger("EffectTime");
        maxDistance = tagCompound.getFloat("MaxDistance");
        direction = new Vec3(tagCompound.getDouble("XDirection"),
                tagCompound.getDouble("YDirection"),
                tagCompound.getDouble("ZDirection"));
        endPoint = new Vec3(tagCompound.getDouble("XHit"),
                tagCompound.getDouble("YHit"),
                tagCompound.getDouble("ZHit"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setString("OwnerName", getCasterName());
        tagCompound.setInteger("Power", power);
        tagCompound.setInteger("EffectInterval", effectInterval);
        tagCompound.setInteger("TimeToLive", timeToLive);
        tagCompound.setInteger("EffectTime", effectTime);
        tagCompound.setFloat("MaxDistance", maxDistance);
        tagCompound.setDouble("XDirection", direction.xCoord);
        tagCompound.setDouble("YDirection", direction.yCoord);
        tagCompound.setDouble("ZDirection", direction.zCoord);
        tagCompound.setDouble("XHit", endPoint.xCoord);
        tagCompound.setDouble("YHit", endPoint.yCoord);
        tagCompound.setDouble("ZHit", endPoint.zCoord);
    }

    public Vec3 getEndPoint()
    {
        return endPoint;
    }

    public Vec3 getDirection()
    {
        return direction;
    }

    protected String getCasterName()
    {
        if(casterName != null)
            return casterName;

        if(caster != null)
            casterName = caster.getName();

        if(casterName == null)
            casterName = "";

        return casterName;
    }

    protected void setCasterName(String name)
    {
        casterName = name;
        if (casterName != null && casterName.length() == 0)
        {
            casterName = null;
        }

        getDataWatcher().updateObject(DATA_INDEX_CASTER, casterName);
    }

    public EntityLivingBase getCaster()
    {
        if(this.caster == null && this.casterName == null)
        {
            casterName = getDataWatcher().getWatchableObjectString(DATA_INDEX_CASTER);
        }

        if (this.caster == null && this.casterName != null && this.casterName.length() > 0)
        {
            this.caster = this.worldObj.getPlayerEntityByName(this.casterName);
        }

        return caster;
    }
}

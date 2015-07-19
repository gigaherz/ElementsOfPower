package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.ElementsOfPower;
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

        setSize(0.1f, 0.1f);
        setCaster(caster);
    }

    protected EntityBeamBase(World worldIn, EntityLivingBase caster, float maxDistance, int power, int effectInterval, int timeToLive)
    {
        super(worldIn);

        this.maxDistance = maxDistance;
        this.power = power;
        this.effectInterval = effectInterval;
        this.timeToLive = timeToLive;
        this.effectTime = effectInterval;

        setSize(0.1f, 0.1f);
        setCaster(caster);

        this.posX = caster.posX;
        this.posY = caster.posY + caster.getEyeHeight() * 0.9;
        this.posZ = caster.posZ;
    }

    @Override
    protected void entityInit()
    {
        getDataWatcher().addObject(DATA_INDEX_CASTER, "");
    }

    protected void updateBeamPosition()
    {
        if(getCaster() == null)
            return;

        direction = caster.getLookVec();
        hitInfo = caster.rayTrace(maxDistance, 0);

        this.posX = caster.posX;
        this.posY = caster.posY + caster.getEyeHeight() * 0.9;
        this.posZ = caster.posZ;

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

        if(worldObj.isRemote)
            ElementsOfPower.logger.warn("Beam update! client");
        else
            ElementsOfPower.logger.warn("Beam update! server");

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

    protected String getCasterName()
    {
        String name = getDataWatcher().getWatchableObjectString(DATA_INDEX_CASTER);
        if(name.length() == 0)
            return null;
        else
            return name;
    }

    protected void setCasterName(String name)
    {
        DataWatcher dw = getDataWatcher();
        dw.updateObject(DATA_INDEX_CASTER, name != null ? name : "");
        dw.setObjectWatched(DATA_INDEX_CASTER);

        caster = null;
    }

    public EntityLivingBase getCaster()
    {
        if (caster == null)
        {
            String name = getCasterName();
            if(name != null)
                caster = worldObj.getPlayerEntityByName(name);
        }

        return caster;
    }

    public void setCaster(EntityLivingBase living)
    {
        if(living == null)
            setCasterName(null);
        else
            setCasterName(living.getName());

        caster = living;
    }
}

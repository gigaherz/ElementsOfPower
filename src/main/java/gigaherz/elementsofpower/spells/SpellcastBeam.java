package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class SpellcastBeam implements ISpellcast<SpellBeam>
{
    protected SpellBeam spell;
    protected World world;
    protected EntityPlayer player;

    protected int remainingCastTime;
    protected int remainingInterval;

    public SpellcastBeam(SpellBeam spell)
    {
        this.spell = spell;
    }

    @Override
    public float getRemainingCastTime()
    {
        return remainingCastTime;
    }

    @Override
    public void init(World world, EntityPlayer player)
    {
        this.world = world;
        this.player = player;
        remainingCastTime = spell.getDuration();
        remainingInterval = spell.effectInterval;
    }

    @Override
    public SpellBeam getEffect()
    {
        return spell;
    }

    protected abstract void applyEffect(MovingObjectPosition mop);

    @Override
    public void update()
    {
        remainingCastTime--;
        remainingInterval--;

        if (remainingInterval <= 0)
        {
            remainingInterval = spell.effectInterval;

            if (!world.isRemote)
            {
                float maxDistance = 10;
                Vec3 start = new Vec3(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);
                Vec3 dir = player.getLook(1);
                Vec3 end = start.addVector(dir.xCoord * maxDistance, dir.yCoord * maxDistance, dir.zCoord * maxDistance);
                MovingObjectPosition mop = player.worldObj.rayTraceBlocks(start, end, false, true, false);

                // TODO: Detect entities

                applyEffect(mop);
            }

            // TODO: visual effects?
        }

        if (remainingCastTime <= 0)
        {
            SpellcastEntityData data = SpellcastEntityData.get(player);
            if (data != null)
            {
                data.end();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagData)
    {
        remainingCastTime = tagData.getInteger("remainingCastTime");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagData)
    {
        tagData.setInteger("remainingCastTime", remainingCastTime);
    }
}
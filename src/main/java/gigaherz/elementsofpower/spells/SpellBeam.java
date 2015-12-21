package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellBeam
        extends SpellBase
{
    int power;
    int effectInterval;
    int timeToLive;

    public SpellBeam(int power, int ticks)
    {
        this.power = power;
        this.effectInterval = 2;
        this.timeToLive = effectInterval * ticks;
    }

    @Override
    public boolean isBeam() { return true; }

    @Override
    public int getDuration() {return timeToLive; }

    @Override
    public float getScale() { return 1 + 0.25f * power; }

    @Override
    public ISpellcast getNewCast()
    {
        return new SpellcastBeam();
    }

    @Override
    public ISpellcast castSpell(ItemStack stack, EntityPlayer player)
    {
        return getNewCast();
    }

    public class SpellcastBeam implements ISpellcast<SpellBeam>
    {
        int remainingCastTime;
        int remainingInterval;
        World world;
        EntityPlayer player;

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
            remainingCastTime = getDuration();
            remainingInterval = effectInterval;
        }

        @Override
        public SpellBeam getEffect() {return SpellBeam.this; }

        void applyEffect()
        {
            float maxDistance = 10;
            Vec3 start = new Vec3(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ);
            Vec3 dir = player.getLook(1);
            Vec3 end = start.addVector(dir.xCoord * maxDistance, dir.yCoord * maxDistance, dir.zCoord * maxDistance);
            MovingObjectPosition mop = player.worldObj.rayTraceBlocks(start, end, false, true, false);

            if (mop == null)
                return;

            if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos pos = mop.getBlockPos().offset(mop.sideHit);
                if (world.getBlockState(pos).getBlock() == Blocks.air)
                {
                    world.setBlockState(pos, Blocks.fire.getDefaultState());
                }
            }
            else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            {
                // TODO: this.applyEnchantments(this.getCaster(), hitInfo.entityHit);

                if (!mop.entityHit.isImmuneToFire())
                {
                    mop.entityHit.setFire(power);
                }
            }
        }

        @Override
        public void update()
        {
            remainingCastTime--;
            remainingInterval--;

            if(remainingInterval<=0)
            {
                remainingInterval = effectInterval;

                if(!world.isRemote)
                {
                    applyEffect();
                }

                // TODO: visual effects?
            }

            if(remainingCastTime<=0)
            {
                SpellcastEntityData data = SpellcastEntityData.get(player);
                if(data != null)
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
}

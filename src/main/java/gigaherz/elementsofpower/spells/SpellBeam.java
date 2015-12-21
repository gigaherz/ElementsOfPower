package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entitydata.SpellcastEntityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
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
        this.effectInterval = 30;
        this.timeToLive = effectInterval * ticks;
    }

    @Override
    public boolean isBeam() { return true; }

    @Override
    public float getDuration() {return timeToLive; }

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
        float remainingCastTime;
        EntityPlayer player;

        @Override
        public float getRemainingCastTime()
        {
            return remainingCastTime;
        }

        @Override
        public void init(EntityPlayer player)
        {
            this.player = player;
            remainingCastTime = getDuration();
        }

        @Override
        public SpellBeam getEffect() {return SpellBeam.this; }

        void applyEffect(World worldObj, MovingObjectPosition hitInfo)
        {
            if (hitInfo == null)
                return;

            if (hitInfo.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos pos = hitInfo.getBlockPos().offset(hitInfo.sideHit);
                if (worldObj.getBlockState(pos).getBlock() == Blocks.air)
                {
                    worldObj.setBlockState(pos, Blocks.fire.getDefaultState());
                }
            }
            else if (hitInfo.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            {
                // TODO: this.applyEnchantments(this.getCaster(), hitInfo.entityHit);

                if (!hitInfo.entityHit.isImmuneToFire())
                {
                    hitInfo.entityHit.setFire(power);
                }
            }
        }

        @Override
        public void update()
        {
            remainingCastTime--;
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
            remainingCastTime = tagData.getFloat("remainingCastTime");
        }

        @Override
        public void writeToNBT(NBTTagCompound tagData)
        {
            tagData.setFloat("remainingCastTime", remainingCastTime);
        }
    }
}

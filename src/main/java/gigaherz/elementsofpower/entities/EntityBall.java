package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityBall extends EntityThrowable
{
    Spellcast spellcast;

    public EntityBall(World worldIn)
    {
        super(worldIn);
    }

    public EntityBall(World worldIn, Spellcast spellcast, EntityLivingBase thrower)
    {
        super(worldIn, thrower);
        this.spellcast = spellcast;
        spellcast.setProjectile(this);
        getDataWatcher().addObjectByDataType(10, 4);
        getDataWatcher().updateObject(10, spellcast.getSequence());
        this.getDataWatcher().setObjectWatched(10);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();

        getDataWatcher().addObjectByDataType(10, 4);
    }

    @Override
    protected float getVelocity()
    {
        return 2.0F;
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.001F;
    }

    @Override
    protected void onImpact(MovingObjectPosition pos)
    {
        if (!this.worldObj.isRemote)
        {
            if (getSpellcast() != null)
                spellcast.onImpact(pos, rand);

            this.setDead();
        }
    }

    public float getScale()
    {
        return 0.6f * (1 + getSpellcast().getDamageForce());
    }

    public Spellcast getSpellcast()
    {
        if (spellcast == null)
        {
            String sequence = getDataWatcher().getWatchableObjectString(10);
            if (sequence != null)
            {
                spellcast = SpellManager.makeSpell(sequence);
                spellcast.init(worldObj, (EntityPlayer) getThrower());
            }
        }
        return spellcast;
    }
}

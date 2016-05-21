package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityBall extends EntityThrowable
{
    Spellcast spellcast;

    private static final DataParameter<String> SEQ = EntityDataManager.createKey(EntityBall.class, DataSerializers.STRING);

    public EntityBall(World worldIn)
    {
        super(worldIn);
    }

    public EntityBall(World worldIn, Spellcast spellcast, EntityLivingBase thrower)
    {
        super(worldIn, thrower);

        this.spellcast = spellcast;
        spellcast.setProjectile(this);

        getDataManager().set(SEQ, spellcast.getSequence());
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();

        getDataManager().register(SEQ, "");
    }

    @Override
    protected float getGravityVelocity()
    {
        return 0.001F;
    }

    @Override
    protected void onImpact(RayTraceResult pos)
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
        if (getSpellcast() != null)
            return 0.6f * (1 + spellcast.getDamageForce());
        return 0;
    }

    public int getColor()
    {
        if (getSpellcast() != null)
            return spellcast.getColor();
        return 0xFFFFFF;
    }

    @Nullable
    public Spellcast getSpellcast()
    {
        if (spellcast == null)
        {
            String sequence = getDataManager().get(SEQ);
            if (sequence.length() > 0)
            {
                spellcast = SpellManager.makeSpell(sequence);
                spellcast.init(worldObj, (EntityPlayer) getThrower());
            }
        }
        return spellcast;
    }
}

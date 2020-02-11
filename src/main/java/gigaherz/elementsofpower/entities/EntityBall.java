package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class EntityBall extends ThrowableEntity implements IEntityAdditionalSpawnData
{
    @ObjectHolder("elementsofpower:ball")
    public static EntityType<EntityBall> TYPE;
    
    Spellcast spellcast;

    private static final DataParameter<String> SEQ = EntityDataManager.createKey(EntityBall.class, DataSerializers.STRING);

    public EntityBall(World worldIn, Spellcast spellcast, LivingEntity thrower)
    {
        super(TYPE, thrower, worldIn);

        this.spellcast = spellcast;
        spellcast.setProjectile(this);
        
        getDataManager().set(SEQ, spellcast.getSequence());
    }

    public EntityBall(EntityType<EntityBall> type, World world)
    {
        super(type, world);
    }

    @Override
    protected void registerData()
    {

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
        if (!this.world.isRemote)
        {
            if (getSpellcast() != null)
                spellcast.onImpact(pos, rand);

            this.remove();
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
                spellcast.init(world, (PlayerEntity) getThrower());
            }
        }
        return spellcast;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer)
    {

    }

    @Override
    public void readSpawnData(PacketBuffer additionalData)
    {

    }
}

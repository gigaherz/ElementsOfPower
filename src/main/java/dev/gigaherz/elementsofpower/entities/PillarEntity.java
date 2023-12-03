package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.entity.IEntityAdditionalSpawnData;
import net.neoforged.neoforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class PillarEntity extends Entity implements IEntityAdditionalSpawnData
{
    public static final int RAISE_TICKS = 3;
    private LivingEntity caster;
    private int delay;
    private int duration;

    @Nullable
    private InitializedSpellcast spellcast;

    public PillarEntity(Level level, LivingEntity caster, InitializedSpellcast spellcast, double posX, double posY, double posZ, float yaw, int delayTicks)
    {
        super(ElementsOfPowerMod.PILLAR_ENTITY_TYPE.get(), level);

        this.spellcast = spellcast;
        this.caster = caster;
        this.delay = delayTicks;
        this.duration = Math.max(60, spellcast.totalCastTime);
        this.setYRot(yaw);
        this.setPos(posX, posY, posZ);
    }

    public PillarEntity(EntityType<PillarEntity> pEntityType, Level pLevel)
    {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!level().isClientSide && (tickCount - delay) > duration)
        {
            // TODO: removal particles
            remove(RemovalReason.DISCARDED);
        }

        if (tickCount <= RAISE_TICKS)
        {
            this.setBoundingBox(this.makeBoundingBox());
        }

        if (level().isClientSide && tickCount == delay)
        {
            var blockpos = BlockPos.containing(this.position()) ;
            BlockState blockState = level().getBlockState(blockpos.below());
            var options = new BlockParticleOption(ParticleTypes.BLOCK, blockState).setPos(blockpos.below());
            for (int i = 0; i < 50; i++)
            {
                float offX = (random.nextFloat() - 0.5f);
                float offZ = (random.nextFloat() - 0.5f);
                level().addParticle(options, getX() + offX, getY() + 0.25f, getZ() + offZ, 0.1f * offX, 5, 0.1f * offZ);
            }
        }
    }

    public int delay()
    {
        return delay;
    }

    @Override
    protected AABB makeBoundingBox() {
        var height = (32.0f+Math.min((tickCount-delay-RAISE_TICKS)*31/RAISE_TICKS, -1))/16.0f;
        return new AABB(position().subtract(7/16.0f,0,7/16.0f), position().add(7/16.0f,height,7/16.0f));
    }

    @Override
    public void push(Entity pEntity) {
        System.out.println("push by " + pEntity.getName().getString());
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isAlive();
    }



    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        duration = pCompound.getInt("duration");
        delay = pCompound.getInt("delay");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        pCompound.putInt("duration", duration);
        pCompound.putInt("delay", delay);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeInt(duration);
        buffer.writeInt(delay);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        duration = buffer.readInt();
        delay = buffer.readInt();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

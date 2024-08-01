package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.network.ParticlesInShape;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class PillarEntity extends Entity implements IEntityWithComplexSpawn
{
    public static final int RAISE_TICKS = 3;
    private Player caster;
    private int delay;
    private int duration;

    @Nullable
    private Spellcast spellcast;

    public PillarEntity(Level level, Player caster, Spellcast spellcast, double posX, double posY, double posZ, float yaw, int delayTicks)
    {
        super(ElementsOfPowerMod.PILLAR_ENTITY_TYPE.get(), level);

        this.spellcast = spellcast;
        this.caster = caster;
        this.delay = delayTicks;
        this.duration = Math.max(60, SpellcastState.get(caster).totalCastTime);
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
            float height = getHeight();

            PacketDistributor.sendToPlayersTrackingEntity(this, new ParticlesInShape(
                    ParticlesInShape.AreaShape.BOX_UNIFORM,
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                    150,
                    getX(), getY() + height * 0.5f, getZ(),
                    0.5f, height * 0.5f, 0.5f,
                    0,0,0,
                    0,0,0
            ));
            remove(RemovalReason.DISCARDED);
        }

        if (tickCount <= RAISE_TICKS)
        {
            this.setBoundingBox(this.makeBoundingBox());
        }

        if (level().isClientSide && tickCount > delay && (tickCount-delay) <= RAISE_TICKS)
        {
            var blockpos = BlockPos.containing(this.position()) ;
            BlockState blockState = level().getBlockState(blockpos.below());
            float height = getHeight();
            for (int i = 0; i < 50; i++)
            {
                float offX = (random.nextFloat() - 0.5f);
                float offZ = (random.nextFloat() - 0.5f);
                float fx = offX < -0.25f ? -0.5f : (offX > 0.25f ? 0.5f : 0.0f);
                float fz = offZ < -0.25f ? -0.5f : (offZ > 0.25f ? 0.5f : 0.0f);
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                        getX() + fx, getY() + 0.25f, getZ() + fz,
                        1.5f * offX, 5f, 1.5f * offZ);
            }
            for (int i = 0; i < 25; i++)
            {
                float offX = (random.nextFloat() - 0.5f);
                float offZ = (random.nextFloat() - 0.5f);
                float fx = offX < -0.25f ? -0.5f : (offX > 0.25f ? 0.5f : 0.0f);
                float fz = offZ < -0.25f ? -0.5f : (offZ > 0.25f ? 0.5f : 0.0f);
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                        getX() + fx, getY() + height, getZ() + fz,
                        0.01f * offX, 10f, 0.01f * offZ);
            }
        }
    }



    public int delay()
    {
        return delay;
    }

    @Override
    protected AABB makeBoundingBox() {
        float height = getHeight();
        return new AABB(position().subtract(7/16.0f,0,7/16.0f), position().add(7/16.0f,height,7/16.0f));
    }

    private float getHeight()
    {
        return (32.0f+Math.min((tickCount-delay-RAISE_TICKS)*31/RAISE_TICKS, -1))/16.0f;
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
    protected void defineSynchedData(SynchedEntityData.Builder p_326003_)
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
    public void writeSpawnData(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(duration);
        buffer.writeInt(delay);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer)
    {
        duration = buffer.readInt();
        delay = buffer.readInt();
    }
}

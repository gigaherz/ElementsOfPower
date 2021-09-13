package dev.gigaherz.elementsofpower.entities;

import com.google.common.collect.Lists;
import dev.gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;

public class EssenceEntity extends AmbientCreature
{
    @ObjectHolder("elementsofpower:essence")
    public static EntityType<EssenceEntity> TYPE;

    public static final float[][] EssenceColors = {
            {1.0f, 0.0f, 0.0f},
            {0.0f, 0.0f, 1.0f},
            {1.0f, 1.0f, 0.0f},
            {0.6f, 0.4f, 0.1f},
            {1.0f, 1.0f, 1.0f},
            {0.0f, 0.0f, 0.0f},
            {0.5f, 1.0f, 0.5f},
            {0.6f, 0.0f, 0.0f},
    };

    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] ELEMENTS = new EntityDataAccessor[]{
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(EssenceEntity.class, EntityDataSerializers.FLOAT)
    };

    private float scale;
    float[][] sequence;
    private BlockPos spawnPosition;

    double accelX;
    double accelY;
    double accelZ;

    int entityAge2;

    public EssenceEntity(EntityType<EssenceEntity> type, Level world)
    {
        this(type, world, MagicAmounts.EMPTY);
    }

    public EssenceEntity(Level worldIn, MagicAmounts am)
    {
        this(TYPE, worldIn, am);
    }

    protected EssenceEntity(EntityType<EssenceEntity> type, Level world, MagicAmounts am)
    {
        super(type, world);

        int numEssences = 0;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            getEntityData().define(ELEMENTS[i], am.get(i));
            numEssences += am.get(i);
        }

        scale = 0.025f * numEssences;

        //setEntityBoundingBox(new AxisAlignedBB(0, 0, 0, 0, 0, 0));
    }

    public static AttributeSupplier.Builder prepareAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 2.0D);
    }

    public float getScale()
    {
        return scale;
    }

    public static float lerp(float a, float b, float t)
    {
        return a + t * (b - a);
    }

    public static double lerp(double a, double b, double t)
    {
        return a + t * (b - a);
    }

    public void updateSequence()
    {
        int total = 0;

        MagicAmounts amounts = MagicAmounts.EMPTY;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            amounts = amounts.with(i, getEntityData().get(ELEMENTS[i]));
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            total += Math.ceil(amounts.get(i));
        }

        if (total == 0)
        {
            sequence = new float[][]{new float[]{0.0f, 0.0f, 0.0f}};
            return;
        }

        List<float[]> seq = Lists.newArrayList();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float am = amounts.get(i);
            while (am > 0)
            {
                seq.add(EssenceColors[i]);
                am -= 1;
                total -= 1;
            }
        }

        if (seq.size() == 0)
        {
            sequence = new float[][]{new float[]{0.0f, 0.0f, 0.0f}};
            return;
        }

        sequence = seq.toArray(new float[seq.size()][]);
    }

    public int getColor(float cycle)
    {
        if (sequence == null)
        {
            updateSequence();
        }

        if (sequence == null || sequence.length == 0)
            return 0;

        cycle *= 0.1f;

        int c = (int) Math.floor(cycle);
        int i = (c) % sequence.length;
        int j = (i + 1) % sequence.length;

        float t = (cycle - c);
        float fr = lerp(sequence[i][0], sequence[j][0], t);
        float fg = lerp(sequence[i][1], sequence[j][1], t);
        float fb = lerp(sequence[i][2], sequence[j][2], t);
        int r = (int) Math.floor(fr * 255);
        int g = (int) Math.floor(fg * 255);
        int b = (int) Math.floor(fb * 255);

        return (b << 16) | (g << 8) | r;
    }

    @Override
    public void tick()
    {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);

        int numEssences = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            numEssences += getEntityData().get(ELEMENTS[i]);
        }
        scale = 0.025f * numEssences;

        if (level.isClientSide)
            return;

        entityAge2++;
        if (random.nextDouble() < (entityAge2 * (1.0 / 2000) - 0.1))
        {
            MagicAmounts amounts = MagicAmounts.EMPTY;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                amounts = amounts.with(i, getEntityData().get(ELEMENTS[i]));
            }

            if (amounts.getTotalMagic() > 0)
            {
                int rnd = random.nextInt(8);
                int j = 0;
                int i = 0;
                while (j < rnd)
                {
                    i = (i + 1) % 8;
                    if (amounts.get(i) > 0)
                        j++;
                }
                amounts = amounts.with(i, Math.max(0, amounts.get(i) - 1));
                getEntityData().set(ELEMENTS[i], amounts.get(i));
            }

            if (amounts.getTotalMagic() <= 0)
                hurt(DamageSource.GENERIC, 1);
            else
            {
                sequence = null;
                entityAge2 = 0;
            }
        }
    }

    private Player target = null;

    @Override
    protected void customServerAiStep()
    {
        super.customServerAiStep();

        if (level.isClientSide)
            return;

        Vec3 followPos = position();

        double dp = Double.POSITIVE_INFINITY;

        if (tickCount % 80 == 0)
        {
            if (target == null)
            {
                target = level.getNearestPlayer(this, 8.0D);
            }
            else
            {
                if (distanceTo(target) > 12.0f)
                {
                    target = null;
                }
            }
        }

        final Entity entity = target;
        if (entity != null)
        {
            dp = distanceToSqr(entity);
            if (dp < 2.0 && entityAge2 > 100)
            {
                tryAbosrbInto(entity);
                if (!isAlive()) return;
            }
            followPos = getEyePosition(1.0f);
            spawnPosition = null;
        }
        else
        {
            if (spawnPosition == null)
            {
                BlockPos blockPos = blockPosition();

                do
                {
                    blockPos = blockPos.below();
                } while (blockPos.getY() > 0 && level.isEmptyBlock(blockPos));

                spawnPosition = blockPos.above();
            }
            else if (spawnPosition.getY() < 1 ||
                    !level.isEmptyBlock(spawnPosition))
            {
                spawnPosition = spawnPosition.above();
            }

            if (spawnPosition != null)
                followPos = Vec3.atCenterOf(spawnPosition);
        }

        Vec3 home = followPos.subtract(position());
        Vec3 forward = getLookAngle();
        Vec3 rnd = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian());

        double wantedDistance = Math.min(dp * 0.5f, 2.0f * scale);
        double currentDistance = home.length();

        double factor = Math.sqrt(currentDistance / wantedDistance);
        double r = Mth.clamp(1 + random.nextGaussian() - factor, 0, 2); // 0: point home. 1: stay forward. 2: move outward.

        double speedMax = (entity != null ? 0.1f : 0.01) * Math.min(1, 1 / scale);

        double sa = entity != null ? 0.1 : 0.01f;
        double sm = entity != null ? 0.8 : 0.25f;

        if (r <= 1)
        {
            accelX = lerp(accelX, lerp(home.x, forward.x, r), sa);
            accelY = lerp(accelY, lerp(home.y, forward.y, r), sa);
            accelZ = lerp(accelZ, lerp(home.z, forward.z, r), sa);
        }
        else
        {
            accelX = lerp(accelX, lerp(forward.x, rnd.x, r), sa);
            accelY = lerp(accelY, lerp(forward.y, rnd.y, r), sa);
            accelZ = lerp(accelZ, lerp(forward.z, rnd.z, r), sa);
        }
        Vec3 motion = getDeltaMovement();
        motion = new Vec3(
                Mth.clamp(motion.x + lerp(0, accelX, sm), -speedMax, speedMax),
                Mth.clamp(motion.y + lerp(0, accelY, sm), -speedMax, speedMax),
                Mth.clamp(motion.z + lerp(0, accelZ, sm), -speedMax, speedMax)
        );
        setDeltaMovement(motion);

        setYRot((float) Math.atan2(motion.z, motion.x));
        float xz = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        setXRot((float) Math.atan2(motion.y, xz));

        zza = 0.5f;
    }

    private void tryAbosrbInto(Entity entity)
    {
        if (!(entity instanceof Player))
            return;

        MagicAmounts self = MagicAmounts.EMPTY;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            self = self.with(i, getEntityData().get(ELEMENTS[i]));
        }

        Player p = (Player) entity;
        Container b = p.getInventory();
        for (int i = 0; i < b.getContainerSize(); i++)
        {
            ItemStack s = b.getItem(i);
            if (s.getCount() <= 0)
                continue;

            MagicAmounts[] _self = {self};
            if (MagicContainerCapability.getContainer(s).filter(magic -> !magic.isFull()).map(magic -> {
                MagicAmounts am = _self[0];
                if (!magic.isFull())
                {
                    MagicAmounts limits = magic.getCapacity();
                    MagicAmounts amounts = magic.getContainedMagic();

                    if (!limits.isEmpty())
                    {
                        MagicAmounts transfer = MagicAmounts.min(am, limits.subtract(amounts));
                        amounts = amounts.add(MagicAmounts.min(amounts.add(transfer), limits));
                        _self[0] = am = am.subtract(transfer);
                        for (int e = 0; e < MagicAmounts.ELEMENTS; e++)
                        {
                            if (transfer.get(e) != 0)
                            {
                                getEntityData().set(ELEMENTS[e], am.get(e));
                            }
                        }

                        if (!transfer.isEmpty())
                        {
                            magic.setContainedMagic(amounts);
                        }
                    }

                    sequence = null;
                    return true;
                }
                return false;
            }).orElse(false))
            {
                break;
            }
            self = _self[0];
        }

        remove(RemovalReason.KILLED);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            getEntityData().set(ELEMENTS[i], tag.getFloat("Essence" + i));
        }
        entityAge2 = tag.getInt("Age2");
        accelX = tag.getDouble("accelX");
        accelY = tag.getDouble("accelY");
        accelZ = tag.getDouble("accelZ");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            tag.putFloat("Essence" + i, getEntityData().get(ELEMENTS[i]));
        }
        tag.putInt("Age2", entityAge2);
        tag.putDouble("accelX", accelX);
        tag.putDouble("accelY", accelY);
        tag.putDouble("accelZ", accelZ);
    }

    @Override
    public void travel(Vec3 direction)
    {
        {
            this.moveRelative(1.0f, direction);

            this.move(MoverType.SELF, getDeltaMovement());

            setDeltaMovement(getDeltaMovement().scale(0.91f));
        }

        this.animationSpeedOld = this.animationSpeed;
        double d1 = this.getX() - this.xo;
        double d0 = this.getZ() - this.zo;
        float f2 = (float) Math.sqrt(d1 * d1 + d0 * d0) * 4.0F;

        if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }

        this.animationSpeed += (f2 - this.animationSpeed) * 0.4F;
        this.animationPosition += this.animationSpeed;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound()
    {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_184601_1_)
    {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return null;
    }

    @Override
    public void spawnAnim()
    {
        super.spawnAnim();
    }

    @Override
    public boolean displayFireAnimation()
    {
        return false;
    }

    @Override
    public void lavaHurt()
    {
    }

    @Override
    public void setRemainingFireTicks(int amount)
    {
    }

    @Override
    public void setSecondsOnFire(int seconds)
    {
    }

    @Override
    public boolean onClimbable()
    {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader p_205019_1_)
    {
        return true;
    }

    @Override
    public boolean canBeLeashed(Player player)
    {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player p_230254_1_, InteractionHand p_230254_2_)
    {
        return InteractionResult.PASS;
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.1F;
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    protected void doPush(Entity p_82167_1_)
    {
    }

    @Override
    protected void pushEntities()
    {
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos)
    {
    }

    @Override
    public boolean isIgnoringBlockTriggers()
    {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if (source.isBypassArmor() || source.getMsgId().equals(DamageSource.OUT_OF_WORLD.getMsgId()))
            return super.hurt(source, amount);
        return false;
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return true;
    }

    @Override
    public float getEyeHeight(Pose pose)
    {
        return 0;
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

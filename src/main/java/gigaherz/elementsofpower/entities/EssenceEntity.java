package gigaherz.elementsofpower.entities;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;

public class EssenceEntity extends AmbientEntity
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
    private static final DataParameter<Float>[] ELEMENTS = new DataParameter[]{
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EssenceEntity.class, DataSerializers.FLOAT)
    };

    private float scale;
    float[][] sequence;
    private BlockPos spawnPosition;

    double accelX;
    double accelY;
    double accelZ;

    int entityAge2;

    public EssenceEntity(EntityType<EssenceEntity> type, World world)
    {
        this(type, world, MagicAmounts.EMPTY);
    }

    public EssenceEntity(World worldIn, MagicAmounts am)
    {
        this(TYPE, worldIn, am);
    }

    protected EssenceEntity(EntityType<EssenceEntity> type, World world, MagicAmounts am)
    {
        super(type, world);

        int numEssences = 0;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            getDataManager().register(ELEMENTS[i], am.get(i));
            numEssences += am.get(i);
        }

        scale = 0.025f * numEssences;

        //setEntityBoundingBox(new AxisAlignedBB(0, 0, 0, 0, 0, 0));
    }

    public static AttributeModifierMap.MutableAttribute prepareAttributes()
    {
        return MonsterEntity.func_234295_eP_()
                .func_233815_a_(Attributes.field_233818_a_, 2.0D);
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
            amounts = amounts.with(i, getDataManager().get(ELEMENTS[i]));
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
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);

        int numEssences = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            numEssences += getDataManager().get(ELEMENTS[i]);
        }
        scale = 0.025f * numEssences;

        if (world.isRemote)
            return;

        entityAge2++;
        if (rand.nextDouble() < (entityAge2 * (1.0 / 2000) - 0.1))
        {
            MagicAmounts amounts = MagicAmounts.EMPTY;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                amounts = amounts.with(i, getDataManager().get(ELEMENTS[i]));
            }

            if (amounts.getTotalMagic() > 0)
            {
                int rnd = rand.nextInt(8);
                int j = 0;
                int i = 0;
                while (j < rnd)
                {
                    i = (i + 1) % 8;
                    if (amounts.get(i) > 0)
                        j++;
                }
                amounts = amounts.with(i, Math.max(0, amounts.get(i) - 1));
                getDataManager().set(ELEMENTS[i], amounts.get(i));
            }

            if (amounts.getTotalMagic() <= 0)
                attackEntityFrom(DamageSource.GENERIC, 1);
            else
            {
                sequence = null;
                entityAge2 = 0;
            }
        }
    }

    private PlayerEntity target = null;

    @Override
    protected void updateAITasks()
    {
        super.updateAITasks();

        if (world.isRemote)
            return;

        Vector3d followPos = getPositionVec();

        double dp = Double.POSITIVE_INFINITY;

        if (ticksExisted % 80 == 0)
        {
            if (target == null)
            {
                target = world.getClosestPlayer(this, 8.0D);
            }
            else
            {
                if (getDistance(target) > 12.0f)
                {
                    target = null;
                }
            }
        }

        final Entity entity = target;
        if (entity != null)
        {
            dp = getDistanceSq(entity);
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
                BlockPos blockPos = func_233580_cy_();

                do
                {
                    blockPos = blockPos.down();
                } while (blockPos.getY() > 0 && world.isAirBlock(blockPos));

                spawnPosition = blockPos.up();
            }
            else if (spawnPosition.getY() < 1 ||
                    !world.isAirBlock(spawnPosition))
            {
                spawnPosition = spawnPosition.up();
            }

            if (spawnPosition != null)
                followPos = Vector3d.func_237491_b_(spawnPosition);
        }

        Vector3d home = followPos.subtract(getPositionVec());
        Vector3d forward = getLookVec();
        Vector3d random = new Vector3d(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());

        double wantedDistance = Math.min(dp * 0.5f, 2.0f * scale);
        double currentDistance = home.length();

        double factor = Math.sqrt(currentDistance / wantedDistance);
        double r = MathHelper.clamp(1 + rand.nextGaussian() - factor, 0, 2); // 0: point home. 1: stay forward. 2: move outward.

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
            accelX = lerp(accelX, lerp(forward.x, random.x, r), sa);
            accelY = lerp(accelY, lerp(forward.y, random.y, r), sa);
            accelZ = lerp(accelZ, lerp(forward.z, random.z, r), sa);
        }
        Vector3d motion = getMotion();
        motion = new Vector3d(
                MathHelper.clamp(motion.x + lerp(0, accelX, sm), -speedMax, speedMax),
                MathHelper.clamp(motion.y + lerp(0, accelY, sm), -speedMax, speedMax),
                MathHelper.clamp(motion.z + lerp(0, accelZ, sm), -speedMax, speedMax)
        );
        setMotion(motion);

        rotationYaw = (float) Math.atan2(motion.z, motion.x);
        float xz = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        rotationPitch = (float) Math.atan2(motion.y, xz);

        moveForward = 0.5f;
    }

    private void tryAbosrbInto(Entity entity)
    {
        if (!(entity instanceof PlayerEntity))
            return;

        MagicAmounts self = MagicAmounts.EMPTY;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            self = self.with(i, getDataManager().get(ELEMENTS[i]));
        }

        PlayerEntity p = (PlayerEntity) entity;
        IInventory b = p.inventory;
        for (int i = 0; i < b.getSizeInventory(); i++)
        {
            ItemStack s = b.getStackInSlot(i);
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
                                getDataManager().set(ELEMENTS[e], am.get(e));
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

        remove();
    }

    @Override
    public void readAdditional(CompoundNBT tag)
    {
        super.readAdditional(tag);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            getDataManager().set(ELEMENTS[i], tag.getFloat("Essence" + i));
        }
        entityAge2 = tag.getInt("Age2");
        accelX = tag.getDouble("accelX");
        accelY = tag.getDouble("accelY");
        accelZ = tag.getDouble("accelZ");
    }

    @Override
    public void writeAdditional(CompoundNBT tag)
    {
        super.writeAdditional(tag);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            tag.putFloat("Essence" + i, getDataManager().get(ELEMENTS[i]));
        }
        tag.putInt("Age2", entityAge2);
        tag.putDouble("accelX", accelX);
        tag.putDouble("accelY", accelY);
        tag.putDouble("accelZ", accelZ);
    }

    @Override
    public void travel(Vector3d direction)
    {
        {
            this.moveRelative(1.0f, direction);

            this.move(MoverType.SELF, getMotion());

            setMotion(getMotion().scale(0.91f));
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d1 = this.getPosX() - this.prevPosX;
        double d0 = this.getPosZ() - this.prevPosZ;
        float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

        if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }

        this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
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
    public void spawnExplosionParticle()
    {
        super.spawnExplosionParticle();
    }

    @Override
    public boolean canRenderOnFire()
    {
        return false;
    }

    @Override
    protected void setOnFireFromLava()
    {
    }

    @Override
    public void func_241209_g_(int amount)
    {
    }

    @Override
    public void setFire(int seconds)
    {
    }

    @Override
    public boolean isOnLadder()
    {
        return false;
    }

    @Override
    public boolean isNotColliding(IWorldReader p_205019_1_)
    {
        return true;
    }

    @Override
    public boolean canBeLeashedTo(PlayerEntity player)
    {
        return false;
    }

    @Override
    protected ActionResultType func_230254_b_(PlayerEntity p_230254_1_, Hand p_230254_2_)
    {
        return ActionResultType.PASS;
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.1F;
    }

    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity p_82167_1_)
    {
    }

    @Override
    protected void collideWithNearbyEntities()
    {
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public boolean onLivingFall(float p_225503_1_, float p_225503_2_)
    {
        return false;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos)
    {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate()
    {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source.isUnblockable() || source.getDamageType().equals(DamageSource.OUT_OF_WORLD.getDamageType()))
            return super.attackEntityFrom(source, amount);
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
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

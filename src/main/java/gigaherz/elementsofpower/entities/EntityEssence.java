package gigaherz.elementsofpower.entities;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.database.ContainerInformation;
import gigaherz.elementsofpower.database.MagicAmounts;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class EntityEssence extends EntityAmbientCreature
{
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
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT),
            EntityDataManager.createKey(EntityEssence.class, DataSerializers.FLOAT)
    };

    private float scale;
    float[][] sequence;
    private BlockPos spawnPosition;

    double accelX;
    double accelY;
    double accelZ;

    int entityAge2;

    public EntityEssence(World worldIn)
    {
        super(worldIn);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            getDataManager().register(ELEMENTS[i], 0.0f);
        }

        setEntityBoundingBox(new AxisAlignedBB(0, 0, 0, 0, 0, 0));
    }

    public EntityEssence(World worldIn, MagicAmounts am)
    {
        super(worldIn);

        int numEssences = 0;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            getDataManager().register(ELEMENTS[i], am.get(i));
            numEssences += am.get(i);
        }

        scale = 0.025f * numEssences;

        setEntityBoundingBox(new AxisAlignedBB(0, 0, 0, 0, 0, 0));
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
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0D);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

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
                setDead();
            else
            {
                sequence = null;
                entityAge2 = 0;
            }
        }
    }

    @Override
    protected void updateAITasks()
    {
        super.updateAITasks();

        if (world.isRemote)
            return;

        Vec3d followPos = new Vec3d(posX, posY, posZ);

        double dp = Double.POSITIVE_INFINITY;

        Entity entity = world.getClosestPlayerToEntity(this, 8.0D);
        if (entity != null)
        {
            dp = getDistanceSq(entity);
            if (dp < 2.0 && entityAge2 > 100)
            {
                tryAbosrbInto(entity);
                if (isDead) return;
            }
            followPos = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            spawnPosition = null;
        }
        else
        {
            if (spawnPosition == null)
            {
                BlockPos blockPos = getPosition();

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
                new Vec3d(spawnPosition.getX(), spawnPosition.getY() + 1, spawnPosition.getZ());
        }

        double dx = followPos.x - posX;
        double dy = followPos.y - posY;
        double dz = followPos.z - posZ;

        Vec3d home = new Vec3d(dx, dy, dz);
        Vec3d forward = getLookVec();
        Vec3d random = new Vec3d(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());

        double wantedDistance = Math.min(dp * 0.5f, 2.0f * scale);
        double currentDistance = home.lengthVector();

        double factor = Math.sqrt(currentDistance / wantedDistance);
        double r = MathHelper.clamp(1 + rand.nextGaussian() - factor, 0, 2); // 0: point home. 1: stay forward. 2: move outward.

        double speedMax = (entity != null ? 0.1f : 0.01) / scale;

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
        motionX = MathHelper.clamp(motionX + lerp(0, accelX, sm), -speedMax, speedMax);
        motionY = MathHelper.clamp(motionY + lerp(0, accelY, sm), -speedMax, speedMax);
        motionZ = MathHelper.clamp(motionZ + lerp(0, accelZ, sm), -speedMax, speedMax);

        rotationYaw = (float) Math.atan2(motionZ, motionX);
        float xz = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
        rotationPitch = (float) Math.atan2(motionY, xz);

        moveForward = 0.5f;
    }

    private void tryAbosrbInto(Entity entity)
    {
        if (!(entity instanceof EntityPlayer))
            return;

        MagicAmounts self = MagicAmounts.EMPTY;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            self = self.with(i, getDataManager().get(ELEMENTS[i]));
        }

        EntityPlayer p = (EntityPlayer) entity;
        IInventory inv = null;
        int slot = 0;
        ItemStack stack = ItemStack.EMPTY;

        IInventory b = p.inventory;
        for (int i = 0; i < b.getSizeInventory(); i++)
        {
            ItemStack s = b.getStackInSlot(i);
            if (ContainerInformation.canItemContainMagic(s))
            {
                if (ContainerInformation.canTransferAnything(s, self))
                {
                    stack = s;
                    inv = b;
                    slot = i;
                    break;
                }
            }
        }

        if (stack.getCount() > 0)
        {
            IMagicContainer magic = ContainerInformation.getMagic(stack);
            if (magic == null)
                return;

            MagicAmounts limits = magic.getCapacity();
            MagicAmounts amounts = magic.getContainedMagic();

            if (!limits.isEmpty())
            {
                float totalTransfer = 0;
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    float transfer = Math.min(self.get(i), limits.get(i) - amounts.get(i));
                    if (transfer > 0)
                    {
                        totalTransfer += transfer;
                        amounts = amounts.with(i, Math.min(amounts.get(i) + transfer, limits.get(i)));
                        self = self.add(i, -transfer);
                        getDataManager().set(ELEMENTS[i], self.get(i));
                    }
                }

                if (totalTransfer > 0)
                {
                    magic.setContainedMagic(amounts);
                }
            }

            sequence = null;
        }

        setDead();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag)
    {
        super.readEntityFromNBT(tag);
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            getDataManager().set(ELEMENTS[i], tag.getFloat("Essence" + i));
        }
        entityAge2 = tag.getInteger("Age2");
        accelX = tag.getDouble("accelX");
        accelY = tag.getDouble("accelY");
        accelZ = tag.getDouble("accelZ");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag)
    {
        super.writeEntityToNBT(tag);
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            tag.setFloat("Essence" + i, getDataManager().get(ELEMENTS[i]));
        }
        tag.setInteger("Age2", entityAge2);
        tag.setDouble("accelX", accelX);
        tag.setDouble("accelY", accelY);
        tag.setDouble("accelZ", accelZ);
    }


    @Override
    public void travel(float strafe, float up, float forward)
    {
        {
            float f = 0.91F;

            float f1 = 0.16277136F / (f * f * f);
            this.moveRelative(strafe, up, forward, this.onGround ? 0.1F * f1 : 0.02F);
            f = 0.91F;

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double) f;
            this.motionY *= (double) f;
            this.motionZ *= (double) f;
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d1 = this.posX - this.prevPosX;
        double d0 = this.posZ - this.prevPosZ;
        float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

        if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }

        this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
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
    protected void dealFireDamage(int amount)
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
    public boolean isNotColliding()
    {
        return true;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player)
    {
        return false;
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand p_184645_2_)
    {
        return false;
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
    public void fall(float distance, float damageMultiplier)
    {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos)
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
        return false;
    }

    @Override
    public float getEyeHeight()
    {
        return 0;
    }
}

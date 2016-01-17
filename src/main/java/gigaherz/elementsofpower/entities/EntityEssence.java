package gigaherz.elementsofpower.entities;

import baubles.api.BaublesApi;
import com.google.common.collect.Lists;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.database.MagicDatabase;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class EntityEssence extends EntityAmbientCreature
{
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

        int numEssences = 5 + rand.nextInt(15);
        MagicAmounts am = new MagicAmounts();

        int j = 0;
        for (int i = 0; i < numEssences; i++)
        {
            j = (j + rand.nextInt(8)) % 8;
            am.amounts[j] += 1;
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            dataWatcher.addObject(16 + i, am.amounts[i]);
        }

        scale = 0.025f * numEssences;

        setEntityBoundingBox(null);
    }

    public float getScale()
    {
        return scale;
    }

    static float[][] essenceColors = {
            {1.0f, 0.0f, 0.0f},
            {0.0f, 0.0f, 1.0f},
            {1.0f, 1.0f, 0.0f},
            {0.6f, 0.4f, 0.1f},
            {1.0f, 1.0f, 1.0f},
            {0.0f, 0.0f, 0.0f},
            {0.5f, 1.0f, 0.5f},
            {0.6f, 0.0f, 0.0f},
    };

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

        MagicAmounts amounts = new MagicAmounts();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            amounts.amounts[i] = dataWatcher.getWatchableObjectFloat(16 + i);
        }

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            total += Math.ceil(amounts.amounts[i]);
        }

        if (total == 0)
        {
            sequence = new float[][]{new float[]{0.0f, 0.0f, 0.0f}};
            return;
        }

        List<float[]> seq = Lists.newArrayList();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            float am = amounts.amounts[i];
            while (am > 0)
            {
                seq.add(essenceColors[i]);
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
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(2.0D);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (worldObj.isRemote)
            return;

        entityAge2++;
        if (rand.nextDouble() < (entityAge2 * (1.0 / 2000) - 0.1))
        {
            MagicAmounts amounts = new MagicAmounts();
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                amounts.amounts[i] = dataWatcher.getWatchableObjectFloat(16 + i);
            }

            if (amounts.getTotalMagic() > 0)
            {
                int rnd = rand.nextInt(8);
                int j = 0;
                int i = 0;
                while (j < rnd)
                {
                    i = (i + 1) % 8;
                    if (amounts.amounts[i] > 0)
                        j++;
                }
                amounts.amounts[i] = Math.max(0, amounts.amounts[i] - 1);
                dataWatcher.updateObject(16 + i, amounts.amounts[i]);
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

        if (worldObj.isRemote)
            return;

        Vec3 followPos = new Vec3(posX, posY, posZ);

        double dp = Double.POSITIVE_INFINITY;

        Entity entity = worldObj.getClosestPlayerToEntity(this, 8.0D);
        if (entity != null)
        {
            dp = getDistanceSqToEntity(entity);
            if (dp < 2.0 && entityAge2 > 100)
            {
                tryAbosrbInto(entity);
                if (isDead) return;
            }
            followPos = new Vec3(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
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
                } while (blockPos.getY() > 0 && worldObj.isAirBlock(blockPos));

                spawnPosition = blockPos.up();
            }
            else if (spawnPosition.getY() < 1 ||
                    !worldObj.isAirBlock(spawnPosition))
            {
                spawnPosition = spawnPosition.up();
            }

            if (spawnPosition != null)
                new Vec3(spawnPosition.getX(), spawnPosition.getY() + 1, spawnPosition.getZ());
        }

        double dx = followPos.xCoord - posX;
        double dy = followPos.yCoord - posY;
        double dz = followPos.zCoord - posZ;

        Vec3 home = new Vec3(dx, dy, dz);
        Vec3 forward = getLookVec();
        Vec3 random = new Vec3(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());

        double wantedDistance = Math.min(dp * 0.5f, 2.0f * scale);
        double currentDistance = home.lengthVector();

        double factor = Math.sqrt(currentDistance / wantedDistance);
        double r = MathHelper.clamp_double(1 + rand.nextGaussian() - factor, 0, 2); // 0: point home. 1: stay forward. 2: move outward.

        double speedMax = (entity != null ? 0.1f : 0.025) / scale;

        double sa = entity != null ? 0.1 : 0.01f;
        double sm = entity != null ? 0.8 : 0.25f;

        if (r <= 1)
        {
            accelX = lerp(accelX, lerp(home.xCoord, forward.xCoord, r), sa);
            accelY = lerp(accelY, lerp(home.yCoord, forward.yCoord, r), sa);
            accelZ = lerp(accelZ, lerp(home.zCoord, forward.zCoord, r), sa);
        }
        else
        {
            accelX = lerp(accelX, lerp(forward.xCoord, random.xCoord, r), sa);
            accelY = lerp(accelY, lerp(forward.yCoord, random.yCoord, r), sa);
            accelZ = lerp(accelZ, lerp(forward.zCoord, random.zCoord, r), sa);
        }
        motionX = MathHelper.clamp_double(motionX + lerp(0, accelX, sm), -speedMax, speedMax);
        motionY = MathHelper.clamp_double(motionY + lerp(0, accelY, sm), -speedMax, speedMax);
        motionZ = MathHelper.clamp_double(motionZ + lerp(0, accelZ, sm), -speedMax, speedMax);

        rotationYaw = (float) Math.atan2(motionZ, motionX);
        float xz = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
        rotationPitch = (float) Math.atan2(motionY, xz);

        moveForward = 0.5f;
    }

    private void tryAbosrbInto(Entity entity)
    {
        if (!(entity instanceof EntityPlayer))
            return;

        MagicAmounts self = new MagicAmounts();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            self.amounts[i] = dataWatcher.getWatchableObjectFloat(16 + i);
        }

        EntityPlayer p = (EntityPlayer) entity;
        IInventory inv = null;
        int slot = 0;
        ItemStack stack = null;

        IInventory b = p.inventory;
        for (int i = 0; i < b.getSizeInventory(); i++)
        {
            ItemStack s = b.getStackInSlot(i);
            if (s == null)
                continue;
            if (MagicDatabase.canItemContainMagic(s))
            {
                if (MagicDatabase.canTransferAnything(s, self))
                {
                    stack = s;
                    inv = b;
                    slot = i;
                    break;
                }
            }
        }

        if (stack == null)
        {
            b = BaublesApi.getBaubles(p);
            if (b != null)
            {
                for (int i = 0; i < b.getSizeInventory(); i++)
                {
                    ItemStack s = b.getStackInSlot(i);
                    if (s == null)
                        continue;
                    if (MagicDatabase.canItemContainMagic(s))
                    {
                        if (MagicDatabase.canTransferAnything(s, self))
                        {
                            stack = s;
                            inv = b;
                            slot = i;
                            break;
                        }
                    }
                }
            }
        }

        if (stack != null)
        {
            MagicAmounts limits = MagicDatabase.getMagicLimits(stack);
            MagicAmounts amounts = MagicDatabase.getContainedMagic(stack);

            if (limits != null)
            {
                if (amounts == null)
                    amounts = new MagicAmounts();

                float totalTransfer = 0;
                for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
                {
                    float transfer = Math.min(self.amounts[i], limits.amounts[i] - amounts.amounts[i]);
                    if (transfer > 0)
                    {
                        totalTransfer += transfer;
                        amounts.amounts[i] = Math.min(amounts.amounts[i] + transfer, limits.amounts[i]);
                        self.amounts[i] -= transfer;
                        dataWatcher.updateObject(16 + i, self.amounts[i]);
                    }
                }

                if (totalTransfer > 0)
                {
                    stack = MagicDatabase.setContainedMagic(stack, amounts);
                    inv.setInventorySlotContents(slot, stack);
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
            dataWatcher.updateObject(16 + i, tag.getFloat("Essence" + i));
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
            tag.setFloat("Essence" + i, dataWatcher.getWatchableObjectFloat(16 + i));
        }
        tag.setInteger("Age2", entityAge2);
        tag.setDouble("accelX", accelX);
        tag.setDouble("accelY", accelY);
        tag.setDouble("accelZ", accelZ);
    }

    @Override
    public void moveEntityWithHeading(float strafe, float forward)
    {
        {
            float f = 0.91F;

            float f1 = 0.16277136F / (f * f * f);
            this.moveFlying(strafe, forward, this.onGround ? 0.1F * f1 : 0.02F);
            f = 0.91F;

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= (double) f;
            this.motionY *= (double) f;
            this.motionZ *= (double) f;
        }

        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d1 = this.posX - this.prevPosX;
        double d0 = this.posZ - this.prevPosZ;
        float f2 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;

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
    public boolean allowLeashing()
    {
        return false;
    }

    @Override
    protected boolean interact(EntityPlayer player)
    {
        return false;
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.1F;
    }

    @Override
    protected float getSoundPitch()
    {
        return super.getSoundPitch();
    }

    @Override
    protected String getLivingSound()
    {
        return null;
    }

    @Override
    protected String getHurtSound()
    {
        return null;
    }

    @Override
    protected String getDeathSound()
    {
        return null;
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
    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
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

package gigaherz.elementsofpower.client;
/*
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.CloudParticle;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class ParticleSmallCloud extends CloudParticle
{
    private static final Field internalParticleSizeField = ObfuscationReflectionHelper.findField(CloudParticle.class, "field_70569_a");

    private ParticleSmallCloud(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        particleScale *= 0.45f;
        try
        {
            internalParticleSizeField.set(this, particleScale);
        }
        catch (IllegalAccessException e)
        {
            throw new ReportedException(new CrashReport("Error trying to construct particle", e));
        }
    }

    @Override
    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.age++ >= this.maxAge)
        {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.age * 8 / this.maxAge);
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        if (this.onGround)
        {
            this.motionY *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }

    public static void spawn(World worldIn, double x, double y, double z, double vx, double vy, double vz)
    {
        Minecraft.getInstance().particles.addEffect(new ParticleSmallCloud(worldIn, x, y, z, vx, vy, vz));
    }
}*/
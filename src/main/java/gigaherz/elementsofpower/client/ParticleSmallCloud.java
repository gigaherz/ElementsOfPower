package gigaherz.elementsofpower.client;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.ParametersAreNonnullByDefault;

public class ParticleSmallCloud extends ParticleCloud
{
    public ParticleSmallCloud(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        particleScale *= 0.45f;
        ReflectionHelper.setPrivateValue(ParticleCloud.class, this, particleScale, "field_70569_a", "oSize");
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        if (this.isCollided)
        {
            this.motionY *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }

    public static void spawn(World worldIn, double x, double y, double z, double vx, double vy, double vz)
    {
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmallCloud(worldIn, x, y, z, vx, vy, vz));
    }
}
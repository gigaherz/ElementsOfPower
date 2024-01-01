package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Objects;

public class ParticlesInShape implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("particles_in_shape");

    public enum AreaShape
    {
        BOX,
        BOX_UNIFORM
    }

    public final AreaShape areaShape;
    public final ParticleOptions options;
    public final int count;
    public final double centerX;
    public final double centerY;
    public final double centerZ;
    public final float spreadX;
    public final float spreadY;
    public final float spreadZ;
    public final float minVelocityX;
    public final float minVelocityY;
    public final float minVelocityZ;
    public final float maxVelocityX;
    public final float maxVelocityY;
    public final float maxVelocityZ;

    public ParticlesInShape(AreaShape areaShape,
                            ParticleOptions options,
                            int count,
                            double centerX, double centerY, double centerZ,
                            float spreadX, float spreadY, float spreadZ,
                            float minVelocityX, float minVelocityY, float minVelocityZ,
                            float maxVelocityX, float maxVelocityY, float maxVelocityZ)
    {
        this.areaShape = areaShape;
        this.options = options;
        this.count = count;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        this.spreadZ = spreadZ;
        this.minVelocityX = minVelocityX;
        this.minVelocityY = minVelocityY;
        this.minVelocityZ = minVelocityZ;
        this.maxVelocityX = maxVelocityX;
        this.maxVelocityY = maxVelocityY;
        this.maxVelocityZ = maxVelocityZ;
    }

    public ParticlesInShape(FriendlyByteBuf buf)
    {
        this.areaShape = buf.readEnum(AreaShape.class);
        this.count = buf.readVarInt();
        this.centerX = buf.readDouble();
        this.centerY = buf.readDouble();
        this.centerZ = buf.readDouble();
        this.spreadX = buf.readFloat();
        this.spreadY = buf.readFloat();
        this.spreadZ = buf.readFloat();
        this.minVelocityX = buf.readFloat();
        this.minVelocityY = buf.readFloat();
        this.minVelocityZ = buf.readFloat();
        this.maxVelocityX = buf.readFloat();
        this.maxVelocityY = buf.readFloat();
        this.maxVelocityZ = buf.readFloat();
        var particletype = Objects.requireNonNull(buf.readById(BuiltInRegistries.PARTICLE_TYPE));
        this.options = readParticle(buf, particletype);
    }

    private <T extends ParticleOptions> T readParticle(FriendlyByteBuf buf, ParticleType<T> particleType) {
        return particleType.getDeserializer().fromNetwork(particleType, buf);
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeEnum(this.areaShape);
        buf.writeVarInt(this.count);
        buf.writeDouble(this.centerX);
        buf.writeDouble(this.centerY);
        buf.writeDouble(this.centerZ);
        buf.writeFloat(this.spreadX);
        buf.writeFloat(this.spreadY);
        buf.writeFloat(this.spreadZ);
        buf.writeFloat(this.minVelocityX);
        buf.writeFloat(this.minVelocityY);
        buf.writeFloat(this.minVelocityZ);
        buf.writeFloat(this.maxVelocityX);
        buf.writeFloat(this.maxVelocityY);
        buf.writeFloat(this.maxVelocityZ);
        buf.writeId(BuiltInRegistries.PARTICLE_TYPE, this.options.getType());
        this.options.writeToNetwork(buf);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientPacketHandlers.handleParticlesInShape(this);
    }
}

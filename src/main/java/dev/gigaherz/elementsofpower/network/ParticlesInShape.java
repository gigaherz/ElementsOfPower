package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ParticlesInShape(
        AreaShape areaShape,
        ParticleOptions options,
        int count,
        double centerX,
        double centerY,
        double centerZ,
        float spreadX,
        float spreadY,
        float spreadZ,
        float minVelocityX,
        float minVelocityY,
        float minVelocityZ,
        float maxVelocityX,
        float maxVelocityY,
        float maxVelocityZ
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("particles_in_shape");
    public static final Type<ParticlesInShape> TYPE = new Type<>(ID);

    public enum AreaShape
    {
        BOX,
        BOX_UNIFORM
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticlesInShape> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public ParticlesInShape decode(RegistryFriendlyByteBuf buf)
        {
            var areaShape = buf.readEnum(AreaShape.class);
            var count = buf.readVarInt();
            var centerX = buf.readDouble();
            var centerY = buf.readDouble();
            var centerZ = buf.readDouble();
            var spreadX = buf.readFloat();
            var spreadY = buf.readFloat();
            var spreadZ = buf.readFloat();
            var minVelocityX = buf.readFloat();
            var minVelocityY = buf.readFloat();
            var minVelocityZ = buf.readFloat();
            var maxVelocityX = buf.readFloat();
            var maxVelocityY = buf.readFloat();
            var maxVelocityZ = buf.readFloat();
            var options = ParticleTypes.STREAM_CODEC.decode(buf);
            return new ParticlesInShape(
                    areaShape, options, count,
                    centerX, centerY, centerZ,
                    spreadX, spreadY, spreadZ,
                    minVelocityX, minVelocityY, minVelocityZ,
                    maxVelocityX, maxVelocityY, maxVelocityZ
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ParticlesInShape instance)
        {
            buf.writeEnum(instance.areaShape());
            buf.writeVarInt(instance.count());
            buf.writeDouble(instance.centerX());
            buf.writeDouble(instance.centerY());
            buf.writeDouble(instance.centerZ());
            buf.writeFloat(instance.spreadX());
            buf.writeFloat(instance.spreadY());
            buf.writeFloat(instance.spreadZ());
            buf.writeFloat(instance.minVelocityX());
            buf.writeFloat(instance.minVelocityY());
            buf.writeFloat(instance.minVelocityZ());
            buf.writeFloat(instance.maxVelocityX());
            buf.writeFloat(instance.maxVelocityY());
            buf.writeFloat(instance.maxVelocityZ());
            ParticleTypes.STREAM_CODEC.encode(buf, instance.options());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientPacketHandlers.handleParticlesInShape(this);
    }
}

package dev.gigaherz.elementsofpower.essentializer;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import dev.gigaherz.elementsofpower.network.AddVelocityToPlayer;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import org.jetbrains.annotations.Nullable;
import java.util.Locale;
import java.util.Random;

public record ColoredSmokeData(
        float red,
        float green,
        float blue
) implements ParticleOptions
{
    public static MapCodec<ColoredSmokeData> CODEC = RecordCodecBuilder
            .mapCodec((instance) -> instance.group(
                    Codec.FLOAT.fieldOf("red").forGetter(ColoredSmokeData::red),
                    Codec.FLOAT.fieldOf("green").forGetter(ColoredSmokeData::green),
                    Codec.FLOAT.fieldOf("blue").forGetter(ColoredSmokeData::blue)
            ).apply(instance, ColoredSmokeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ColoredSmokeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ColoredSmokeData::red,
            ByteBufCodecs.FLOAT, ColoredSmokeData::green,
            ByteBufCodecs.FLOAT, ColoredSmokeData::blue,
            ColoredSmokeData::new
    );

    private static final RandomSource RANDOM = RandomSource.create();

    @Override
    public ParticleType<?> getType()
    {
        return ElementsOfPowerMod.COLORED_SMOKE_DATA.get();
    }

    public static ColoredSmokeData withColor(float red, float green, float blue)
    {
        return new ColoredSmokeData(red, green, blue);
    }

    public static ColoredSmokeData withRandomColor(MagicAmounts amounts)
    {
        float total = amounts.getTotalMagic();

        // 10% extra so that at least 10% of the smoke is white.
        float rnd = RANDOM.nextFloat() * 1.1f * total;

        Element e = null;
        for (int i = 0; i < 8; i++)
        {
            float v = amounts.get(i);
            if (rnd <= v)
            {
                e = Element.values[i];
                break;
            }

            rnd -= v;
        }

        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;

        if (e != null)
        {
            int color = e.getColor();
            red = ((color >> 16) & 0xFF) / 255.0f;
            green = ((color >> 8) & 0xFF) / 255.0f;
            blue = ((color >> 0) & 0xFF) / 255.0f;
        }

        return withColor(red, green, blue);
    }

    public static class Factory implements ParticleProvider<ColoredSmokeData>
    {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet p_i51045_1_)
        {
            this.spriteSet = p_i51045_1_;
        }

        @Nullable
        @Override
        public Particle createParticle(ColoredSmokeData colorData, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new SmokeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, 1.0F, this.spriteSet)
            {
                {
                    setColor(colorData.red, colorData.green, colorData.blue);
                }
            };
        }
    }

    public static class Type extends ParticleType<ColoredSmokeData>
    {
        public Type(boolean alwaysShow)
        {
            super(alwaysShow);
        }

        @Override
        public MapCodec<ColoredSmokeData> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ColoredSmokeData> streamCodec()
        {
            return STREAM_CODEC;
        }
    }
}

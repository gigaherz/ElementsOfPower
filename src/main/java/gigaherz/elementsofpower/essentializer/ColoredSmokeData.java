package gigaherz.elementsofpower.essentializer;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gigaherz.elementsofpower.cocoons.CocoonFeatureConfig;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.magic.MagicGradient;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Random;

public class ColoredSmokeData implements IParticleData
{
    @ObjectHolder("elementsofpower:colored_smoke")
    public static ParticleType<ColoredSmokeData> TYPE = null;

    public static Codec<ColoredSmokeData> CODEC = RecordCodecBuilder
            .create((instance) -> instance.group(
                    Codec.FLOAT.fieldOf("red").forGetter(i -> i.red),
                    Codec.FLOAT.fieldOf("green").forGetter(i -> i.green),
                    Codec.FLOAT.fieldOf("blue").forGetter(i -> i.blue)
            ).apply(instance, ColoredSmokeData::new));


    private static Random RANDOM = new Random();

    public final float red;
    public final float green;
    public final float blue;

    private ColoredSmokeData(float red, float green, float blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public ParticleType<?> getType()
    {
        return TYPE;
    }

    @Override
    public void write(PacketBuffer buffer)
    {
        buffer.writeFloat(red);
        buffer.writeFloat(green);
        buffer.writeFloat(blue);
    }

    @Override
    public String getParameters()
    {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", this.getType().getRegistryName(), this.red, this.green, this.blue);
    }

    @Deprecated
    public static final IDeserializer<ColoredSmokeData> DESERIALIZER = new IDeserializer<ColoredSmokeData>()
    {
        @Override
        public ColoredSmokeData deserialize(ParticleType<ColoredSmokeData> particleTypeIn, StringReader reader) throws CommandSyntaxException
        {
            reader.expect(' ');
            float r = (float)reader.readDouble();
            reader.expect(' ');
            float g = (float)reader.readDouble();
            reader.expect(' ');
            float b = (float)reader.readDouble();
            return new ColoredSmokeData(r,g,b);
        }

        @Override
        public ColoredSmokeData read(ParticleType<ColoredSmokeData> particleTypeIn, PacketBuffer buffer)
        {
            return new ColoredSmokeData(
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat()
            );
        }
    };

    public static ColoredSmokeData withColor(float red, float green, float blue)
    {
        return new ColoredSmokeData(red,green,blue);
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

    public static class Factory implements IParticleFactory<ColoredSmokeData>
    {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite p_i51045_1_)
        {
            this.spriteSet = p_i51045_1_;
        }

        @Nullable
        @Override
        public Particle makeParticle(ColoredSmokeData colorData, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
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
            super(alwaysShow, ColoredSmokeData.DESERIALIZER);
        }

        @Override
        public Codec<ColoredSmokeData> func_230522_e_()
        {
            return CODEC;
        }
    }
}

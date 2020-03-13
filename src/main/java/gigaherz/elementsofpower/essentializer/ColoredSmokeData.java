package gigaherz.elementsofpower.essentializer;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.spells.Element;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Random;

public class ColoredSmokeData implements IParticleData
{
    @ObjectHolder("elementsofpower:white_smoke")
    public static ParticleType<ColoredSmokeData> TYPE = null;

    private static Random RANDOM = new Random();

    public float red;
    public float green;
    public float blue;

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
        return "<red> <green> <blue>";
    }

    public static final IDeserializer<ColoredSmokeData> DESERIALIZER = new IDeserializer<ColoredSmokeData>()
    {
        @Override
        public ColoredSmokeData deserialize(ParticleType<ColoredSmokeData> particleTypeIn, StringReader reader) throws CommandSyntaxException
        {
            ColoredSmokeData data = new ColoredSmokeData();
            data.red = reader.readFloat();
            data.green = reader.readFloat();
            data.blue = reader.readFloat();
            return data;
        }

        @Override
        public ColoredSmokeData read(ParticleType<ColoredSmokeData> particleTypeIn, PacketBuffer buffer)
        {
            ColoredSmokeData data = new ColoredSmokeData();
            data.red = buffer.readFloat();
            data.green = buffer.readFloat();
            data.blue = buffer.readFloat();
            return data;
        }
    };

    public static ColoredSmokeData withColor(float red, float green, float blue)
    {
        ColoredSmokeData data = new ColoredSmokeData();
        data.red = red;
        data.green = green;
        data.blue = blue;
        return data;
    }

    public static ColoredSmokeData withRandomColor(MagicAmounts amounts)
    {
        float total = amounts.getTotalMagic();

        // 10% extra so that at least 10% of the smoke is white.
        float rnd = RANDOM.nextFloat() * 1.1f * total;

        Element e = null;
        for (int i=0;i<8;i++)
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
            red = ((color >> 16)&0xFF)/255.0f;
            green = ((color >> 8)&0xFF)/255.0f;
            blue = ((color >> 0)&0xFF)/255.0f;
        }

        return withColor(red, green, blue);
    }

    public static class Factory implements IParticleFactory<ColoredSmokeData>
    {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite p_i51045_1_) {
            this.spriteSet = p_i51045_1_;
        }

        public Particle makeParticle(ColoredSmokeData colorData, World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
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
    }
}

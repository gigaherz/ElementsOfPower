package dev.gigaherz.elementsofpower.magic;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.CheckReturnValue;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public record MagicAmounts(
        float fire,
        float water,
        float air,
        float earth,
        float light,
        float time,
        float life,
        float chaos
)
{
    public static final Codec<MagicAmounts> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("fire").forGetter(MagicAmounts::fire),
                    Codec.FLOAT.fieldOf("water").forGetter(MagicAmounts::water),
                    Codec.FLOAT.fieldOf("air").forGetter(MagicAmounts::air),
                    Codec.FLOAT.fieldOf("earth").forGetter(MagicAmounts::earth),
                    Codec.FLOAT.fieldOf("light").forGetter(MagicAmounts::light),
                    Codec.FLOAT.fieldOf("time").forGetter(MagicAmounts::time),
                    Codec.FLOAT.fieldOf("life").forGetter(MagicAmounts::life),
                    Codec.FLOAT.fieldOf("chaos").forGetter(MagicAmounts::chaos)
            ).apply(instance, MagicAmounts::new));

    public static final MagicAmounts EMPTY = new MagicAmounts();
    public static final MagicAmounts INFINITE = new MagicAmounts(
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY);

    public static final int ELEMENTS = 8;

    public final static String[] magicNames = {
            ElementsOfPowerMod.MODID + ".element.fire",
            ElementsOfPowerMod.MODID + ".element.water",
            ElementsOfPowerMod.MODID + ".element.air",
            ElementsOfPowerMod.MODID + ".element.earth",
            ElementsOfPowerMod.MODID + ".element.light",
            ElementsOfPowerMod.MODID + ".element.time",
            ElementsOfPowerMod.MODID + ".element.life",
            ElementsOfPowerMod.MODID + ".element.chaos",
    };

    public static MutableComponent getMagicName(int i)
    {
        return Component.translatable(magicNames[i]);
    }


    private MagicAmounts()
    {
        this(0,0,0,0,0,0,0,0);
    }

    private MagicAmounts(final MagicAmounts other)
    {
        this(other.fire, other.water, other.air, other.earth, other.light, other.time, other.life, other.chaos);
    }

    public MagicAmounts(CompoundTag tag)
    {
        this(
            tag.getFloat("fire"),
            tag.getFloat("water"),
            tag.getFloat("air"),
            tag.getFloat("earth"),
            tag.getFloat("light"),
            tag.getFloat("darkness"),
            tag.getFloat("life"),
            tag.getFloat("death")
        );
    }

    public MagicAmounts(FriendlyByteBuf buf)
    {
        this(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static MagicAmounts ofElement(Element element, float count)
    {
        return switch(element)
        {
            case FIRE -> EMPTY.fire(count);
            case WATER -> EMPTY.water(count);
            case AIR -> EMPTY.air(count);
            case EARTH -> EMPTY.earth(count);
            case LIGHT -> EMPTY.light(count);
            case TIME -> EMPTY.time(count);
            case LIFE -> EMPTY.life(count);
            case CHAOS -> EMPTY.chaos(count);
            default -> throw new RuntimeException("Cannot add " + element + " to MagicAmounts");
        };
    }

    public static MagicAmounts lerp(MagicGradient.GradientPoint pt0, MagicGradient.GradientPoint pt1, float t)
    {
        return pt0.value.add(pt1.value.subtract(pt0.value).multiply(t));
    }

    @Override
    public String toString()
    {
        if (isEmpty())
            return "{empty}";

        return "{fire: " + fire +
               ", water: " + water +
               ", air: " + air +
               ", earth: " + earth +
               ", light: " + light +
               ", time: " + time +
               ", life: " + life +
               ", death: " + chaos +
               "}";
    }

    public String toShortString()
    {
        if (isEmpty())
            return "{empty}";

        return "{" + fire +
                ", " + water +
                ", " + air +
                ", " + earth +
                ", " + light +
                ", " + time +
                ", " + life +
                ", " + chaos +
                "}";
    }

    @CheckReturnValue
    public MagicAmounts fire(float amount)
    {
        return new MagicAmounts(fire+amount,water,air,earth,light,time,life,chaos);
    }

    @CheckReturnValue
    public MagicAmounts water(float amount)
    {
        return new MagicAmounts(fire,water+amount,air,earth,light,time,life,chaos);
    }

    @CheckReturnValue
    public MagicAmounts air(float amount)
    {
        return new MagicAmounts(fire,water,air+amount,earth,light,time,life,chaos);
    }

    @CheckReturnValue
    public MagicAmounts earth(float amount)
    {
        return new MagicAmounts(fire,water,air,earth+amount,light,time,life,chaos);
    }

    @CheckReturnValue
    public MagicAmounts light(float amount)
    {
        return new MagicAmounts(fire,water,air,earth,light+amount,time,life,chaos);
    }

    @CheckReturnValue
    public MagicAmounts time(float amount)
    {
        return new MagicAmounts(fire,water,air,earth,light,time+amount,life,chaos);
    }

    @CheckReturnValue
    public MagicAmounts life(float amount)
    {
        return new MagicAmounts(fire,water,air,earth,light,time,life+amount,chaos);
    }

    @CheckReturnValue
    public MagicAmounts chaos(float amount)
    {
        return new MagicAmounts(fire,water,air,earth,light,time,life,chaos+amount);
    }

    @CheckReturnValue
    public MagicAmounts all(float amount)
    {
        return new MagicAmounts(
                fire+amount,
                water+amount,
                air+amount,
                earth+amount,
                light+amount,
                time+amount,
                life+amount,
                chaos+amount);
    }

    @CheckReturnValue
    public MagicAmounts add(MagicAmounts other)
    {
        return new MagicAmounts(
                fire+other.fire,
                water+other.water,
                air+other.air,
                earth+other.earth,
                light+other.light,
                time+other.time,
                life+other.life,
                chaos+other.chaos);
    }

    @CheckReturnValue
    public MagicAmounts subtract(MagicAmounts other)
    {
        return new MagicAmounts(
                fire-other.fire,
                water-other.water,
                air-other.air,
                earth-other.earth,
                light-other.light,
                time-other.time,
                life-other.life,
                chaos-other.chaos);
    }

    @CheckReturnValue
    public MagicAmounts multiply(float scale)
    {
        return new MagicAmounts(
                fire*scale,
                water*scale,
                air*scale,
                earth*scale,
                light*scale,
                time*scale,
                life*scale,
                chaos*scale);
    }

    @CheckReturnValue
    public boolean isInfinite()
    {
        if (Float.isInfinite(fire)) return true;
        if (Float.isInfinite(water)) return true;
        if (Float.isInfinite(air)) return true;
        if (Float.isInfinite(earth)) return true;
        if (Float.isInfinite(light)) return true;
        if (Float.isInfinite(time)) return true;
        if (Float.isInfinite(life)) return true;
        if (Float.isInfinite(chaos)) return true;
        return false;
    }

    public static MagicAmounts min(MagicAmounts a, MagicAmounts b)
    {
        return new MagicAmounts(
                Math.min(a.fire,b.fire),
                Math.min(a.water,b.water),
                Math.min(a.air,b.air),
                Math.min(a.earth,b.earth),
                Math.min(a.light,b.light),
                Math.min(a.time,b.time),
                Math.min(a.life,b.life),
                Math.min(a.chaos,b.chaos));
    }

    public static int compare(MagicAmounts a, MagicAmounts b)
    {
        int tmp;
        if ((tmp = Float.compare(a.fire,b.fire)) != 0) return tmp;
        if ((tmp = Float.compare(a.water,b.water)) != 0) return tmp;
        if ((tmp = Float.compare(a.air,b.air)) != 0) return tmp;
        if ((tmp = Float.compare(a.earth,b.earth)) != 0) return tmp;
        if ((tmp = Float.compare(a.light,b.light)) != 0) return tmp;
        if ((tmp = Float.compare(a.time,b.time)) != 0) return tmp;
        if ((tmp = Float.compare(a.life,b.life)) != 0) return tmp;
        if ((tmp = Float.compare(a.chaos,b.chaos)) != 0) return tmp;
        return 0;
    }

    @CheckReturnValue
    private MagicAmounts copy()
    {
        return new MagicAmounts(this);
    }

    public float get(Element element)
    {
        return switch(element)
        {
            case FIRE -> fire;
            case WATER -> water;
            case AIR -> air;
            case EARTH -> earth;
            case LIGHT -> light;
            case TIME -> time;
            case LIFE -> life;
            case CHAOS -> chaos;
            default -> 0;
        };
    }

    public float get(int element)
    {
        return switch(element)
        {
            case 0 -> fire;
            case 1 -> water;
            case 2 -> air;
            case 3 -> earth;
            case 4 -> light;
            case 5 -> time;
            case 6 -> life;
            case 7 -> chaos;
            default -> 0;
        };
    }

    public MagicAmounts add(Element element, float value)
    {
        return switch(element)
        {
            case FIRE -> fire(value);
            case WATER -> water(value);
            case AIR -> air(value);
            case EARTH -> earth(value);
            case LIGHT -> light(value);
            case TIME -> time(value);
            case LIFE -> life(value);
            case CHAOS -> chaos(value);
            default -> this;
        };
    }

    public MagicAmounts add(int element, float value)
    {
        return switch(element)
        {
            case 0 -> fire(value);
            case 1 -> water(value);
            case 2 -> air(value);
            case 3 -> earth(value);
            case 4 -> light(value);
            case 5 -> time(value);
            case 6 -> life(value);
            case 7 -> chaos(value);
            default -> this;
        };
    }

    public MagicAmounts with(Element element, float value)
    {
        return switch(element)
        {
            case FIRE -> new MagicAmounts(value,water,air,earth,light,time,life,chaos);
            case WATER -> new MagicAmounts(fire,value,air,earth,light,time,life,chaos);
            case AIR -> new MagicAmounts(fire,water,value,earth,light,time,life,chaos);
            case EARTH -> new MagicAmounts(fire,water,air,value,light,time,life,chaos);
            case LIGHT -> new MagicAmounts(fire,water,air,earth,value,time,life,chaos);
            case TIME -> new MagicAmounts(fire,water,air,earth,light,value,life,chaos);
            case LIFE -> new MagicAmounts(fire,water,air,earth,light,time,value,chaos);
            case CHAOS -> new MagicAmounts(fire,water,air,earth,light,time,life,value);
            default -> this;
        };
    }

    public MagicAmounts with(int element, float value)
    {
        return switch(element)
        {
            case 0 -> new MagicAmounts(value,water,air,earth,light,time,life,chaos);
            case 1 -> new MagicAmounts(fire,value,air,earth,light,time,life,chaos);
            case 2 -> new MagicAmounts(fire,water,value,earth,light,time,life,chaos);
            case 3 -> new MagicAmounts(fire,water,air,value,light,time,life,chaos);
            case 4 -> new MagicAmounts(fire,water,air,earth,value,time,life,chaos);
            case 5 -> new MagicAmounts(fire,water,air,earth,light,value,life,chaos);
            case 6 -> new MagicAmounts(fire,water,air,earth,light,time,value,chaos);
            case 7 -> new MagicAmounts(fire,water,air,earth,light,time,life,value);
            default -> this;
        };
    }

    public Stream<Float> stream()
    {
        return Stream.of(fire, water, air, earth, light, time, life, chaos);
    }

    public static boolean isNullOrEmpty(MagicAmounts value)
    {
        return value == null || value.isEmpty();
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("fire", fire);
        nbt.putFloat("water", water);
        nbt.putFloat("air", air);
        nbt.putFloat("earth", earth);
        nbt.putFloat("light", light);
        nbt.putFloat("time", time);
        nbt.putFloat("life", life);
        nbt.putFloat("chaos", chaos);
        return nbt;
    }

    public Element getDominantElement()
    {
        Element dominant = Element.BALANCE;
        float value = 0;

        if (fire > value) { dominant = Element.FIRE; value=fire; }
        if (water > value) { dominant = Element.WATER; value=water; }
        if (air > value) { dominant = Element.AIR; value=air; }
        if (earth > value) { dominant = Element.EARTH; value=earth; }
        if (light > value) { dominant = Element.LIGHT; value=light; }
        if (time > value) { dominant = Element.TIME; value=time; }
        if (life > value) { dominant = Element.LIFE; value=life; }
        if (chaos > value) { dominant = Element.CHAOS; value=chaos; }

        return dominant;
    }

    public void writeTo(FriendlyByteBuf buf)
    {
        buf.writeFloat(fire);
        buf.writeFloat(water);
        buf.writeFloat(air);
        buf.writeFloat(earth);
        buf.writeFloat(light);
        buf.writeFloat(time);
        buf.writeFloat(life);
        buf.writeFloat(chaos);
    }

    public boolean isEmpty()
    {
        if (fire!=0) return false;
        if (water!=0) return false;
        if (air!=0) return false;
        if (earth!=0) return false;
        if (light!=0) return false;
        if (time!=0) return false;
        if (life!=0) return false;
        if (chaos!=0) return false;
        return true;
    }

    public boolean isPositive()
    {
        if (fire>0) return true;
        if (water>0) return true;
        if (air>0) return true;
        if (earth>0) return true;
        if (light>0) return true;
        if (time>0) return true;
        if (life>0) return true;
        if (chaos>0) return true;
        return false;
    }

    public float getTotalMagic()
    {
        return fire
                + water
                + air
                + earth
                + light
                + time
                + life
                + chaos;
    }

    public boolean lessEqual(MagicAmounts other)
    {
        if (fire>=other.fire) return false;
        if (water>=other.water) return false;
        if (air>=other.air) return false;
        if (earth>=other.earth) return false;
        if (light>=other.light) return false;
        if (time>=other.time) return false;
        if (life>=other.life) return false;
        if (chaos>=other.chaos) return false;
        return true;
    }

    public boolean anyLessThan(MagicAmounts other)
    {
        if (fire<other.fire) return true;
        if (water<other.water) return true;
        if (air<other.air) return true;
        if (earth<other.earth) return true;
        if (light<other.light) return true;
        if (time<other.time) return true;
        if (life<other.life) return true;
        if (chaos<other.chaos) return true;
        return false;
    }

    public boolean allLessThan(MagicAmounts other)
    {
        if (fire>other.fire) return false;
        if (water>other.water) return false;
        if (air>other.air) return false;
        if (earth>other.earth) return false;
        if (light>other.light) return false;
        if (time>other.time) return false;
        if (life>other.life) return false;
        if (chaos>other.chaos) return false;
        return true;
    }

    public boolean greaterEqual(MagicAmounts other)
    {
        if (fire<=other.fire) return false;
        if (water<=other.water) return false;
        if (air<=other.air) return false;
        if (earth<=other.earth) return false;
        if (light<=other.light) return false;
        if (time<=other.time) return false;
        if (life<=other.life) return false;
        if (chaos<=other.chaos) return false;
        return true;
    }

    public boolean equals(MagicAmounts other)
    {
        if (fire!=other.fire) return false;
        if (water!=other.water) return false;
        if (air!=other.air) return false;
        if (earth!=other.earth) return false;
        if (light!=other.light) return false;
        if (time!=other.time) return false;
        if (life!=other.life) return false;
        if (chaos!=other.chaos) return false;
        return true;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof MagicAmounts && equals((MagicAmounts) obj);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fire, water, air, earth, light, time, life, chaos);
    }

    public static Accumulator builder()
    {
        return new Accumulator();
    }

    public static class Accumulator
    {
        private final float[] amounts = new float[ELEMENTS];

        public void add(MagicAmounts value)
        {
            amounts[0] += value.fire;
            amounts[1] += value.water;
            amounts[2] += value.air;
            amounts[3] += value.earth;
            amounts[4] += value.light;
            amounts[5] += value.time;
            amounts[6] += value.life;
            amounts[7] += value.chaos;
        }

        public void subtract(MagicAmounts value)
        {
            amounts[0] -= value.fire;
            amounts[1] -= value.water;
            amounts[2] -= value.air;
            amounts[3] -= value.earth;
            amounts[4] -= value.light;
            amounts[5] -= value.time;
            amounts[6] -= value.life;
            amounts[7] -= value.chaos;
        }

        public void add(Accumulator value)
        {
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                amounts[i] += value.amounts[i];
            }
        }

        public MagicAmounts toAmounts()
        {
            return new MagicAmounts(
                    amounts[0],
                    amounts[1],
                    amounts[2],
                    amounts[3],
                    amounts[4],
                    amounts[5],
                    amounts[6],
                    amounts[7]
            );
        }
    }
}

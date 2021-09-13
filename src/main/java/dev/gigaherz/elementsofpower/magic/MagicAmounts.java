package dev.gigaherz.elementsofpower.magic;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.MagicTooltips;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.CheckReturnValue;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MagicAmounts implements INBTSerializable<CompoundTag>
{
    public static final Codec<MagicAmounts> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("fire").forGetter(i -> i.get(0)),
                    Codec.FLOAT.fieldOf("water").forGetter(i -> i.get(1)),
                    Codec.FLOAT.fieldOf("air").forGetter(i -> i.get(2)),
                    Codec.FLOAT.fieldOf("earth").forGetter(i -> i.get(3)),
                    Codec.FLOAT.fieldOf("light").forGetter(i -> i.get(4)),
                    Codec.FLOAT.fieldOf("darkness").forGetter(i -> i.get(5)),
                    Codec.FLOAT.fieldOf("life").forGetter(i -> i.get(6)),
                    Codec.FLOAT.fieldOf("death").forGetter(i -> i.get(7))
            ).apply(instance, MagicAmounts::new));

    public static final MagicAmounts EMPTY = new MagicAmounts();
    public static final MagicAmounts INFINITE = infinite();
    public static final int ELEMENTS = 8;

    public final static String[] magicNames = {
            ElementsOfPowerMod.MODID + ".element.fire",
            ElementsOfPowerMod.MODID + ".element.water",
            ElementsOfPowerMod.MODID + ".element.air",
            ElementsOfPowerMod.MODID + ".element.earth",
            ElementsOfPowerMod.MODID + ".element.light",
            ElementsOfPowerMod.MODID + ".element.darkness",
            ElementsOfPowerMod.MODID + ".element.life",
            ElementsOfPowerMod.MODID + ".element.death",
    };

    public static MutableComponent getMagicName(int i)
    {
        return new TranslatableComponent(magicNames[i]);
    }

    private final float[] amounts = new float[ELEMENTS];

    private MagicAmounts()
    {
    }

    private MagicAmounts(float fire, float water, float air, float earth, float light, float darkness, float life, float death)
    {
        amounts[0] = fire;
        amounts[1] = water;
        amounts[2] = air;
        amounts[3] = earth;
        amounts[4] = light;
        amounts[5] = darkness;
        amounts[6] = life;
        amounts[7] = death;
    }

    private MagicAmounts(final MagicAmounts other)
    {
        System.arraycopy(other.amounts, 0, amounts, 0, ELEMENTS);
    }

    public MagicAmounts(CompoundTag tagCompound)
    {
        deserializeNBT(tagCompound);
    }

    public MagicAmounts(FriendlyByteBuf buf)
    {
        int numElements = buf.readByte();
        if (numElements > 0)
        {
            for (int i = 0; i < numElements; i++)
            {
                int which = buf.readByte();
                float amount = buf.readFloat();
                amounts[which] = amount;
            }
        }
    }

    public static MagicAmounts ofElement(Element value, float count)
    {
        return EMPTY.add(value, count);
    }

    public static MagicAmounts lerp(MagicGradient.GradientPoint pt0, MagicGradient.GradientPoint pt1, float t)
    {
        return pt0.value.add(pt1.value.subtract(pt0.value).multiply(t));
    }

    @Override
    public String toString()
    {
        if (isEmpty())
            return "{Empty}";

        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] == 0)
                continue;

            if (first)
                b.append("{");
            else
                b.append(", ");

            String magicName = getMagicName(i).getString();
            String str = String.format("%s: %f", magicName, amounts[i]);
            b.append(str);

            first = false;
        }
        b.append("}");

        return b.toString();
    }

    public String toShortString()
    {
        if (isEmpty())
            return "{Empty}";

        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] == 0)
                continue;

            if (first)
                b.append("{");
            else
                b.append(",");

            String str = MagicTooltips.PRETTY_NUMBER_FORMATTER.format(amounts[i]);
            b.append(str);

            first = false;
        }
        b.append("}");

        return b.toString();
    }

    public boolean isEmpty()
    {
        for (float amount : amounts)
        {
            if (amount > 0)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isNotEmpty()
    {
        return !isEmpty();
    }

    public float getTotalMagic()
    {
        float acc = 0;

        for (float amount : amounts)
        {
            acc += amount;
        }

        return acc;
    }

    public boolean hasEnough(MagicAmounts cost)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] < cost.amounts[i])
                return false;
        }
        return true;
    }

    @CheckReturnValue
    public MagicAmounts fire(float amount)
    {
        return add(0, amount);
    }

    @CheckReturnValue
    public MagicAmounts water(float amount)
    {
        return add(1, amount);
    }

    @CheckReturnValue
    public MagicAmounts air(float amount)
    {
        return add(2, amount);
    }

    @CheckReturnValue
    public MagicAmounts earth(float amount)
    {
        return add(3, amount);
    }

    @CheckReturnValue
    public MagicAmounts light(float amount)
    {
        return add(4, amount);
    }

    @CheckReturnValue
    public MagicAmounts darkness(float amount)
    {
        return add(5, amount);
    }

    @CheckReturnValue
    public MagicAmounts life(float amount)
    {
        return add(6, amount);
    }

    @CheckReturnValue
    public MagicAmounts death(float amount)
    {
        return add(7, amount);
    }

    @CheckReturnValue
    public MagicAmounts add(Element element, float amount)
    {
        return add(element.ordinal(), amount);
    }

    @CheckReturnValue
    public MagicAmounts add(int i, float amount)
    {
        if (Math.abs(amount) < 0.00001f)
            return this;
        return with(i, amounts[i] + amount);
    }

    @CheckReturnValue
    public MagicAmounts with(int i, float amount)
    {
        if (Math.abs(amount - amounts[i]) < 0.00001f)
            return this;
        MagicAmounts n = copy();
        n.amounts[i] = amount;
        return n;
    }

    @CheckReturnValue
    public MagicAmounts all(float amount)
    {
        MagicAmounts n = copy();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] += amount;
        }
        return n;
    }

    @CheckReturnValue
    public MagicAmounts add(MagicAmounts other)
    {
        MagicAmounts n = copy();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] += other.amounts[i];
        }
        return n;
    }

    @CheckReturnValue
    public MagicAmounts subtract(MagicAmounts cost)
    {
        MagicAmounts n = copy();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] -= cost.amounts[i];
        }
        return n;
    }

    @CheckReturnValue
    public MagicAmounts multiply(float scale)
    {
        MagicAmounts n = copy();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] *= scale;
        }
        return n;
    }

    @CheckReturnValue
    public MagicAmounts multiply(double scale)
    {
        MagicAmounts n = copy();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] *= scale;
        }
        return n;
    }

    @CheckReturnValue
    public static MagicAmounts infinite()
    {
        MagicAmounts n = new MagicAmounts();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] = Float.POSITIVE_INFINITY;
        }
        return n;
    }

    @CheckReturnValue
    public boolean isInfinite()
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (Float.isInfinite(amounts[i]))
                return true;
        }
        return false;
    }

    public static MagicAmounts min(MagicAmounts a, MagicAmounts b)
    {
        MagicAmounts n = new MagicAmounts();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] = Math.min(a.amounts[i], b.amounts[i]);
        }
        return n;
    }

    public static int compare(MagicAmounts a, MagicAmounts b)
    {
        return Float.compare(a.getTotalMagic(), b.getTotalMagic());
    }

    @CheckReturnValue
    private MagicAmounts copy()
    {
        return new MagicAmounts(this);
    }

    public float get(Element element)
    {
        return amounts[element.ordinal()];
    }

    public Stream<Float> stream()
    {
        return IntStream.range(0, amounts.length).mapToObj(i -> amounts[i]);
    }

    public static boolean isNullOrEmpty(MagicAmounts value)
    {
        return value == null || value.isEmpty();
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        ListTag itemList = new ListTag();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            CompoundTag tag = new CompoundTag();
            tag.putByte("Type", (byte) i);
            tag.putFloat("Count", amounts[i]);
            itemList.add(tag);
        }

        nbt.put("Essences", itemList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        ListTag tagList = nbt.getList("Essences", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag tag = (CompoundTag) tagList.get(i);
            byte slot = tag.getByte("Type");

            if (slot >= 0 && slot < 8)
            {
                amounts[slot] = tag.getFloat("Count");
            }
        }
    }

    public Element getDominantElement()
    {
        float domAmount = 0;
        int dominant = MagicAmounts.ELEMENTS;

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] > domAmount)
            {
                domAmount = amounts[i];
                dominant = i;
            }
        }

        return Element.values[dominant];
    }

    public void writeTo(FriendlyByteBuf buf)
    {
        int count = 0;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] > 0) count++;
        }

        buf.writeByte(count);

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] > 0)
            {
                buf.writeByte(i);
                buf.writeFloat(amounts[i]);
            }
        }
    }

    public float get(int i)
    {
        if (i < 0 || i >= ELEMENTS)
            throw new IndexOutOfBoundsException();
        return amounts[i];
    }

    public boolean lessEqual(MagicAmounts other)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] > other.amounts[i])
                return false;
        }

        return true;
    }

    public boolean lessThan(MagicAmounts other)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] < other.amounts[i])
                return true;
        }

        return false;
    }

    public boolean equals(MagicAmounts other)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] != other.amounts[i])
                return false;
        }

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
        return Arrays.hashCode(amounts);
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
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                amounts[i] += value.amounts[i];
            }
        }

        public void subtract(MagicAmounts value)
        {
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                amounts[i] -= value.amounts[i];
            }
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
            MagicAmounts am = new MagicAmounts();
            System.arraycopy(amounts, 0, am.amounts, 0, MagicAmounts.ELEMENTS);
            return am;
        }
    }

    public static class Serializer
            implements JsonSerializer<MagicAmounts>,
            JsonDeserializer<MagicAmounts>
    {
        @Override
        public JsonElement serialize(MagicAmounts src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonArray array = new JsonArray();
            for (float a : src.amounts)
            {
                array.add(new JsonPrimitive(a));
            }
            return array;
        }

        @Override
        public MagicAmounts deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            MagicAmounts amounts = new MagicAmounts();
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                amounts.amounts[i] = array.get(i).getAsInt();
            }
            return amounts;
        }
    }
}

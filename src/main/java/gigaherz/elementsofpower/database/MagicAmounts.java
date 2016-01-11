package gigaherz.elementsofpower.database;

import com.google.gson.*;

import java.lang.reflect.Type;

public class MagicAmounts
{
    public static final int FIRE = 0;
    public static final int WATER = 1;
    public static final int AIR = 2;
    public static final int EARTH = 3;
    public static final int LIGHT = 4;
    public static final int DARKNESS = 5;
    public static final int LIFE = 6;
    public static final int DEATH = 7;

    public final float[] amounts = new float[8];

    public MagicAmounts()
    {
    }

    public MagicAmounts(final MagicAmounts other)
    {
        System.arraycopy(other.amounts, 0, amounts, 0, amounts.length);
    }

    @Override
    public String toString()
    {
        if (isEmpty())
            return "{Empty}";

        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < 8; i++)
        {
            if (amounts[i] == 0)
                continue;

            if (first)
                b.append("{");
            else
                b.append(", ");

            String magicName = MagicDatabase.getMagicName(i);
            String str = String.format("%s: %f", magicName, amounts[i]);
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
        for (int i = 0; i < 8; i++)
        {
            if (amounts[i] < cost.amounts[i])
                return false;
        }
        return true;
    }

    public void subtract(MagicAmounts cost)
    {
        for (int i = 0; i < 8; i++)
        {
            amounts[i] -= cost.amounts[i];
        }
    }

    public MagicAmounts fire(int amount)
    {
        amounts[0] += amount;
        return this;
    }

    public MagicAmounts water(int amount)
    {
        amounts[1] += amount;
        return this;
    }

    public MagicAmounts air(int amount)
    {
        amounts[2] += amount;
        return this;
    }

    public MagicAmounts earth(int amount)
    {
        amounts[3] += amount;
        return this;
    }

    public MagicAmounts light(int amount)
    {
        amounts[4] += amount;
        return this;
    }

    public MagicAmounts darkness(int amount)
    {
        amounts[5] += amount;
        return this;
    }

    public MagicAmounts life(int amount)
    {
        amounts[6] += amount;
        return this;
    }

    public MagicAmounts death(int amount)
    {
        amounts[7] += amount;
        return this;
    }

    public MagicAmounts all(int amount)
    {
        for (int i = 0; i < amounts.length; i++)
        {
            amounts[i] += amount;
        }

        return this;
    }

    public void add(MagicAmounts other)
    {
        for (int i = 0; i < amounts.length; i++)
        {
            amounts[i] += other.amounts[i];
        }
    }

    public MagicAmounts copy()
    {
        return new MagicAmounts(this);
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
            for (int i = 0; i < amounts.amounts.length; i++)
            {
                amounts.amounts[i] = array.get(i).getAsInt();
            }
            return amounts;
        }
    }
}

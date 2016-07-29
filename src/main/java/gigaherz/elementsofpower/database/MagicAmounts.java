package gigaherz.elementsofpower.database;

import com.google.gson.*;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.gemstones.Element;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class MagicAmounts
{
    public static final int ELEMENTS = 8;

    public final static String[] magicNames = {
            ElementsOfPower.MODID + ".element.fire",
            ElementsOfPower.MODID + ".element.water",
            ElementsOfPower.MODID + ".element.air",
            ElementsOfPower.MODID + ".element.earth",
            ElementsOfPower.MODID + ".element.light",
            ElementsOfPower.MODID + ".element.darkness",
            ElementsOfPower.MODID + ".element.life",
            ElementsOfPower.MODID + ".element.death",
    };

    @SuppressWarnings("deprecation")
    public static String getMagicName(int i)
    {
        return net.minecraft.util.text.translation.I18n.translateToLocal(magicNames[i]);
    }

    public final float[] amounts = new float[ELEMENTS];

    public static boolean areAmountsEqual(@Nullable MagicAmounts a1, @Nullable MagicAmounts a2)
    {
        return (a1 == null && a2 == null) || (a1 != null && a1.equals(a2));
    }

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
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] == 0)
                continue;

            if (first)
                b.append("{");
            else
                b.append(", ");

            String magicName = getMagicName(i);
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

            String str = ElementsOfPower.prettyNumberFormatter.format(amounts[i]);
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
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            if (amounts[i] < cost.amounts[i])
                return false;
        }
        return true;
    }

    public void subtract(MagicAmounts cost)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            amounts[i] -= cost.amounts[i];
        }
    }

    public MagicAmounts fire(float amount)
    {
        amounts[0] += amount;
        return this;
    }

    public MagicAmounts water(float amount)
    {
        amounts[1] += amount;
        return this;
    }

    public MagicAmounts air(float amount)
    {
        amounts[2] += amount;
        return this;
    }

    public MagicAmounts earth(float amount)
    {
        amounts[3] += amount;
        return this;
    }

    public MagicAmounts light(float amount)
    {
        amounts[4] += amount;
        return this;
    }

    public MagicAmounts darkness(float amount)
    {
        amounts[5] += amount;
        return this;
    }

    public MagicAmounts life(float amount)
    {
        amounts[6] += amount;
        return this;
    }

    public MagicAmounts death(float amount)
    {
        amounts[7] += amount;
        return this;
    }

    public MagicAmounts all(float amount)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            amounts[i] += amount;
        }

        return this;
    }

    public MagicAmounts element(Element element, float amount)
    {
        amounts[element.ordinal()] += amount;
        return this;
    }

    public MagicAmounts add(@Nullable MagicAmounts other)
    {
        if (other == null)
            return this;
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            amounts[i] += other.amounts[i];
        }
        return this;
    }

    public MagicAmounts multiply(float scale)
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            amounts[i] *= scale;
        }
        return this;
    }

    public MagicAmounts infinite()
    {
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            amounts[i] = Integer.MAX_VALUE;
        }
        return this;
    }

    public float amount(Element element)
    {
        return amounts[element.ordinal()];
    }

    public MagicAmounts copy()
    {
        return new MagicAmounts(this);
    }

    public void readFromNBT(NBTTagCompound tagCompound)
    {
        NBTTagList tagList = tagCompound.getTagList("Essences", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound tag = (NBTTagCompound) tagList.get(i);
            byte slot = tag.getByte("Type");

            if (slot >= 0 && slot < 8)
            {
                amounts[slot] = tag.getFloat("Count");
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound)
    {
        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Type", (byte) i);
            tag.setFloat("Count", amounts[i]);
            itemList.appendTag(tag);
        }

        tagCompound.setTag("Essences", itemList);
    }

    @Nullable
    public static MagicAmounts copyOf(@Nullable MagicAmounts amounts)
    {
        return amounts == null ? null : amounts.copy();
    }

    public int getDominantElement()
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

        return dominant;
    }

    @Nullable
    public static MagicAmounts readAmounts(ByteBuf buf)
    {
        MagicAmounts amounts = null;
        int numElements = buf.readByte();
        if (numElements > 0)
        {
            amounts = new MagicAmounts();
            for (int i = 0; i < numElements; i++)
            {
                int which = buf.readByte();
                float amount = buf.readFloat();
                amounts.amounts[which] = amount;
            }
        }
        return amounts;
    }

    public static void writeAmounts(ByteBuf buf, @Nullable MagicAmounts amounts)
    {
        if (amounts != null)
        {
            int count = 0;
            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                if (amounts.amounts[i] > 0) count++;
            }

            buf.writeByte(count);

            for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
            {
                if (amounts.amounts[i] > 0)
                {
                    buf.writeByte(i);
                    buf.writeFloat(amounts.amounts[i]);
                }
            }
        }
        else
        {
            buf.writeByte(0);
        }
    }

    public static MagicAmounts empty()
    {
        return new MagicAmounts();
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

package gigaherz.elementsofpower.database;

import com.google.gson.*;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.spells.Element;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckReturnValue;
import java.lang.reflect.Type;
import java.util.Arrays;

@NotNull
public class MagicAmounts implements INBTSerializable<NBTTagCompound>
{
    public static final MagicAmounts EMPTY = new MagicAmounts();
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

    private final float[] amounts = new float[ELEMENTS];

    private MagicAmounts()
    {
    }

    private MagicAmounts(final MagicAmounts other)
    {
        System.arraycopy(other.amounts, 0, amounts, 0, ELEMENTS);
    }

    public MagicAmounts(NBTTagCompound tagCompound)
    {
        deserializeNBT(tagCompound);
    }

    public MagicAmounts(ByteBuf buf)
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
    public MagicAmounts infinite()
    {
        MagicAmounts n = new MagicAmounts();
        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            n.amounts[i] = Integer.MAX_VALUE;
        }
        return this;
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

    @CheckReturnValue
    private MagicAmounts copy()
    {
        return new MagicAmounts(this);
    }

    public float get(Element element)
    {
        return amounts[element.ordinal()];
    }


    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < MagicAmounts.ELEMENTS; i++)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Type", (byte) i);
            tag.setFloat("Count", amounts[i]);
            itemList.appendTag(tag);
        }

        nbt.setTag("Essences", itemList);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("Essences", Constants.NBT.TAG_COMPOUND);

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

    public void writeTo(ByteBuf buf)
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
        return obj instanceof MagicAmounts && equals((MagicAmounts)obj);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(amounts);
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

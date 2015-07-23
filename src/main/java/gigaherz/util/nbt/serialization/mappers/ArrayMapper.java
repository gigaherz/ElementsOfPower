package gigaherz.util.nbt.serialization.mappers;

import gigaherz.util.nbt.serialization.NBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.SerializationException;

import java.lang.reflect.Array;

public class ArrayMapper extends MapperBase
{
    public ArrayMapper(int priority)
    {
        super(priority);
    }

    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return clazz.isArray();
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return clazz.isArray();
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        serializeArray(tag2, object);
        parent.setTag(fieldName, tag2);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = (NBTTagCompound) parent.getTag(fieldName);
        return deserializeArray(tag2, clazz);
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object)
            throws ReflectiveOperationException
    {
        serializeArray(self, object);
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz)
            throws ReflectiveOperationException
    {
        return deserializeArray(self, clazz);
    }

    private void serializeArray(NBTTagCompound tag, Object a)
            throws ReflectiveOperationException
    {
        tag.setString("type", "array");
        tag.setString("className", a.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (int ii = 0; ii < Array.getLength(a); ii++)
        {
            Object o = Array.get(a, ii);
            NBTTagCompound tag2 = new NBTTagCompound();
            tag2.setInteger("index", ii);
            if (o != null)
            {
                NBTSerializer.serializeToField(tag2, "valueClass", o.getClass().getName());
                NBTSerializer.serializeToField(tag2, "value", o);
            }
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private Object deserializeArray(NBTTagCompound tag, Class<?> clazz)
            throws ReflectiveOperationException
    {
        if (!tag.getString("type").equals("array"))
            throw new SerializationException();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);

        Object o = Array.newInstance(clazz.getComponentType(), list.tagCount());

        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            int index = tag2.getInteger("index");

            if (!tag2.hasKey("value"))
            {
                continue;
            }

            Class<?> cls = Class.forName(tag2.getString("valueClass"));
            Object value = NBTSerializer.deserializeToField(tag2, "value", cls, null);

            if (cls == Byte.class || cls == byte.class)
            {
                Array.setByte(o, index, (Byte) value);
            }
            else if (cls == Short.class || cls == short.class)
            {
                Array.setShort(o, index, (Short) value);
            }
            else if (cls == Integer.class || cls == int.class)
            {
                Array.setInt(o, index, (Integer) value);
            }
            else if (cls == Long.class || cls == long.class)
            {
                Array.setLong(o, index, (Long) value);
            }
            else if (cls == Float.class || cls == float.class)
            {
                Array.setFloat(o, index, (Float) value);
            }
            else if (cls == Double.class || cls == double.class)
            {
                Array.setDouble(o, index, (Double) value);
            }
            else if (cls == Boolean.class || cls == boolean.class)
            {
                Array.setBoolean(o, index, (Boolean) value);
            }
            else if (cls == Character.class || cls == char.class)
            {
                Array.setChar(o, index, (Character) value);
            }
            else
            {
                Array.set(o, index, value);
            }
        }

        return o;
    }
}

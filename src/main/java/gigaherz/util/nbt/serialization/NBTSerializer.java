package gigaherz.util.nbt.serialization;

import gigaherz.util.nbt.serialization.mappers.*;
import net.minecraft.nbt.NBTTagCompound;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

public class NBTSerializer
{
    static final List<INBTMapper> mappers = new ArrayList<INBTMapper>();
    static final GenericObjectMapper generic = new GenericObjectMapper();

    static
    {
        // Must go first so that it's handled before the rest
        mappers.add(new CustomSerializableMapper());

        mappers.add(new EnumMapper());
        mappers.add(new ArrayMapper());

        mappers.add(new ListMapper());
        mappers.add(new MapMapper());
        mappers.add(new SetMapper());
    }

    public void registerNBTMapper(INBTMapper mapper)
    {
        if (mappers.contains(mapper))
            throw new KeyAlreadyExistsException();

        mappers.add(mapper);
    }

    // ==============================================================================================================
    // Serializing
    public static NBTTagCompound serialize(Object o)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag = new NBTTagCompound();
        serializeToCompound(tag, o);
        return tag;
    }

    public static void serializeToField(NBTTagCompound tag, String fieldName, Object o)
            throws ReflectiveOperationException
    {
        if (o == null)
        {
            generic.serializeField(tag, fieldName, o);
        }
        else if (o instanceof Byte)
        {
            tag.setByte(fieldName, (Byte) o);
        }
        else if (o instanceof Short)
        {
            tag.setShort(fieldName, (Short) o);
        }
        else if (o instanceof Integer)
        {
            tag.setInteger(fieldName, (Integer) o);
        }
        else if (o instanceof Long)
        {
            tag.setLong(fieldName, (Long) o);
        }
        else if (o instanceof Float)
        {
            tag.setFloat(fieldName, (Float) o);
        }
        else if (o instanceof Double)
        {
            tag.setDouble(fieldName, (Double) o);
        }
        else if (o instanceof Boolean)
        {
            tag.setBoolean(fieldName, (Boolean) o);
        }
        else if (o instanceof Character)
        {
            tag.setInteger(fieldName, (Character) o);
        }
        else if (o instanceof String)
        {
            tag.setString(fieldName, (String) o);
        }
        else
        {
            for (INBTMapper mapper : mappers)
            {
                if (mapper.canMapToField(o.getClass()))
                {
                    mapper.serializeField(tag, fieldName, o);
                    return;
                }
            }

            generic.serializeField(tag, fieldName, o);
        }
    }

    public static void serializeToCompound(NBTTagCompound tag, Object o)
            throws ReflectiveOperationException
    {
        // Basic types can't be serialized to compounds
        if (o == null)
        {
            generic.serializeCompound(tag, o);
        }
        else
        {
            for (INBTMapper mapper : mappers)
            {
                if (mapper.canMapToCompound(o.getClass()))
                {
                    mapper.serializeCompound(tag, o);
                    return;
                }
            }

            generic.serializeCompound(tag, o);
        }
    }

    // ==============================================================================================================
    // Deserializing
    public static <T> T deserialize(Class<? extends T> clazz, NBTTagCompound tag)
            throws ReflectiveOperationException
    {
        return (T) deserializeToCompound(tag, clazz);
    }

    public static Object deserializeToField(NBTTagCompound parent, String fieldName, Class<?> clazz, Object currentValue)
            throws ReflectiveOperationException
    {
        if (!parent.hasKey(fieldName))
            return currentValue;

        if (clazz == Byte.class || clazz == byte.class)
        {
            return parent.getByte(fieldName);
        }
        else if (clazz == Short.class || clazz == short.class)
        {
            return parent.getShort(fieldName);
        }
        else if (clazz == Integer.class || clazz == int.class)
        {
            return parent.getInteger(fieldName);
        }
        else if (clazz == Long.class || clazz == long.class)
        {
            return parent.getLong(fieldName);
        }
        else if (clazz == Float.class || clazz == float.class)
        {
            return parent.getFloat(fieldName);
        }
        else if (clazz == Double.class || clazz == double.class)
        {
            return parent.getDouble(fieldName);
        }
        else if (clazz == Boolean.class || clazz == boolean.class)
        {
            return parent.getBoolean(fieldName);
        }
        else if (clazz == Character.class || clazz == char.class)
        {
            return parent.getInteger(fieldName);
        }
        else if (clazz == String.class)
        {
            return parent.getString(fieldName);
        }
        else
        {
            for (INBTMapper mapper : mappers)
            {
                if (mapper.canMapToField(clazz))
                    return mapper.deserializeField(parent, fieldName, clazz);
            }

            return generic.deserializeField(parent, fieldName, clazz);
        }
    }

    public static Object deserializeToCompound(NBTTagCompound self, Class<?> clazz)
            throws ReflectiveOperationException
    {
        for (INBTMapper mapper : mappers)
        {
            if (mapper.canMapToCompound(clazz))
                return mapper.deserializeCompound(self, clazz);
        }

        return generic.deserializeCompound(self, clazz);
    }
}

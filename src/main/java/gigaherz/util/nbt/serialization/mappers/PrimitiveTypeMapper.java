package gigaherz.util.nbt.serialization.mappers;

import com.google.common.primitives.Primitives;
import net.minecraft.nbt.NBTTagCompound;

public class PrimitiveTypeMapper implements INBTMapper
{
    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return clazz.isPrimitive() || Primitives.isWrapperType(clazz);
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return false;
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object) throws ReflectiveOperationException
    {
        if (object instanceof Byte)
        {
            parent.setByte(fieldName, (Byte) object);
        }
        else if (object instanceof Short)
        {
            parent.setShort(fieldName, (Short) object);
        }
        else if (object instanceof Integer)
        {
            parent.setInteger(fieldName, (Integer) object);
        }
        else if (object instanceof Long)
        {
            parent.setLong(fieldName, (Long) object);
        }
        else if (object instanceof Float)
        {
            parent.setFloat(fieldName, (Float) object);
        }
        else if (object instanceof Double)
        {
            parent.setDouble(fieldName, (Double) object);
        }
        else if (object instanceof Boolean)
        {
            parent.setBoolean(fieldName, (Boolean) object);
        }
        else if (object instanceof Character)
        {
            parent.setInteger(fieldName, (Character) object);
        }
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz) throws ReflectiveOperationException
    {
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
        return null;
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object) throws ReflectiveOperationException
    {
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz) throws ReflectiveOperationException
    {
        return null;
    }
}

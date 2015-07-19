package gigaherz.util.nbt.serialization.mappers;

import gigaherz.util.nbt.serialization.ICustomNBTSerializable;
import gigaherz.util.nbt.serialization.INBTMapper;
import gigaherz.util.nbt.serialization.NBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.SerializationException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class GenericObjectMapper implements INBTMapper
{
    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return true;
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return true;
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object) throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        serializeObject(tag2, object);
        parent.setTag(fieldName, tag2);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz) throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = (NBTTagCompound) parent.getTag(fieldName);
        return deserializeObject(tag2, clazz);
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object) throws ReflectiveOperationException
    {
        serializeObject(self, object);
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz) throws ReflectiveOperationException
    {
        return deserializeObject(self, clazz);
    }

    private void serializeObject(NBTTagCompound tag, Object o)
            throws ReflectiveOperationException
    {
        if (o == null)
        {
            tag.setString("type", "null");
            return;
        }

        if (o instanceof ICustomNBTSerializable)
        {
            ((ICustomNBTSerializable) o).writeToNBT(tag);
            return;
        }

        tag.setString("type", "object");
        tag.setString("className", o.getClass().getName());

        Class<?> cls = o.getClass();

        // The loop skips Object
        while (cls.getSuperclass() != null)
        {
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields)
            {
                if (Modifier.isStatic(f.getModifiers()))
                    continue;

                f.setAccessible(true);
                NBTSerializer.serializeToField(tag, f.getName(), f.get(o));
            }

            cls = cls.getSuperclass();
        }
    }

    private Object deserializeObject(NBTTagCompound tag, Class<?> clazz)
            throws ReflectiveOperationException
    {
        if (ICustomNBTSerializable.class.isAssignableFrom(clazz))
        {
            Object o = clazz.newInstance();
            ((ICustomNBTSerializable) o).readFromNBT(tag);
            return o;
        }

        if (tag.getString("type").equals("null"))
            return null;

        if (!tag.getString("type").equals("object"))
            throw new SerializationException();

        Class<?> actual = Class.forName(tag.getString("className"));
        if (!clazz.isAssignableFrom(actual))
            throw new SerializationException();

        Class<?> cls = actual;

        Object o = cls.newInstance();

        // The loop skips Object
        while (cls.getSuperclass() != null)
        {
            Field[] fields = cls.getDeclaredFields();
            for (Field f : fields)
            {
                if (Modifier.isStatic(f.getModifiers()))
                    continue;

                f.setAccessible(true);
                f.set(o, NBTSerializer.deserializeToField(tag, f.getName(), f.getType(), f.get(o)));
            }

            cls = cls.getSuperclass();
        }

        return o;
    }
}

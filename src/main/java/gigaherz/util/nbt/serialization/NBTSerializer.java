package gigaherz.util.nbt.serialization;

import net.minecraft.crash.CrashReport;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.SerializationException;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NBTSerializer
{
    // ==============================================================================================================
    // Serializing
    public static NBTTagCompound serialize(Object o)
    {
        NBTTagCompound tag = new NBTTagCompound();
        serializeObject(tag, o);
        return tag;
    }

    private static void serializeObject(NBTTagCompound tag, Object o)
    {
        if(o == null)
        {
            serializeNull(tag);
            return;
        }

        tag.setString("type","object");
        tag.setString("className", o.getClass().getName());

        Class<?> cls = o.getClass();

        try
        {
            // The loop skips Object
            while(cls.getSuperclass()!=null)
            {
                Field[] fields = cls.getDeclaredFields();
                for(Field f : fields)
                {
                    if(Modifier.isStatic(f.getModifiers()))
                        continue;

                    f.setAccessible(true);
                    serializeField(tag, f.getName(), f.get(o));
                }

                cls = cls.getSuperclass();
            }
        }
        catch (IllegalAccessException e)
        {
            throw new ReportedException(new CrashReport("Exception serializing class to NBT", e));
        }
    }

    private static void serializeField(NBTTagCompound tag, String fieldName, Object o)
    {
        if(o == null)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeNull(tag2);
            tag.setTag(fieldName, tag2);
        }
        else if(o instanceof Byte)
        {
            tag.setByte(fieldName, (Byte)o);
        }
        else if(o instanceof Short)
        {
            tag.setShort(fieldName, (Short) o);
        }
        else if(o instanceof Integer)
        {
            tag.setInteger(fieldName, (Integer) o);
        }
        else if(o instanceof Long)
        {
            tag.setLong(fieldName, (Long) o);
        }
        else if(o instanceof Float)
        {
            tag.setFloat(fieldName, (Float) o);
        }
        else if(o instanceof Double)
        {
            tag.setDouble(fieldName, (Double) o);
        }
        else if(o instanceof Boolean)
        {
            tag.setBoolean(fieldName, (Boolean) o);
        }
        else if(o instanceof Character)
        {
            tag.setInteger(fieldName, (Character) o);
        }
        else if(o instanceof String)
        {
            tag.setString(fieldName, (String) o);
        }
        else if(o instanceof Enum)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeEnum(tag2, ((Enum) o));
            tag.setTag(fieldName, tag2);
        }
        else if(o.getClass().isArray())
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeArray(tag2, o);
            tag.setTag(fieldName, tag2);
        }
        else if(o instanceof List)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeList(tag2, ((List) o));
            tag.setTag(fieldName, tag2);
        }
        else if(o instanceof Map)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeMap(tag2, ((Map) o));
            tag.setTag(fieldName, tag2);
        }
        else if(o instanceof Set)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeSet(tag2, ((Set) o));
            tag.setTag(fieldName, tag2);
        }
        else
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeObject(tag2, o);
            tag.setTag(fieldName, tag2);
        }
    }

    private static void serializeEnum(NBTTagCompound tag, Enum o)
    {
        tag.setString("type","enum");
        tag.setString("className",o.getClass().getName());
        tag.setString("valueName", o.name());
    }

    private static void serializeNull(NBTTagCompound tag)
    {
        tag.setString("type", "null");
    }

    private static void serializeArray(NBTTagCompound tag, Object a)
    {
        tag.setString("type","array");
        tag.setString("className", a.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (int ii = 0; ii < Array.getLength(a); ii++)
        {
            Object o = Array.get(a, ii);
            NBTTagCompound tag2 = new NBTTagCompound();
            tag2.setInteger("index", ii);
            if(o != null)
            {
                serializeField(tag2, "valueClass", o.getClass().getName());
                serializeField(tag2, "value", o);
            }
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private static void serializeList(NBTTagCompound tag, List l)
    {
        tag.setString("type","list");
        tag.setString("className", l.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (int ii = 0; ii < l.size(); ii++)
        {
            Object o = l.get(ii);
            NBTTagCompound tag2 = new NBTTagCompound();
            tag2.setInteger("index", ii);
            if(o != null)
            {
                serializeField(tag2, "valueClass", o.getClass().getName());
                serializeField(tag2, "value", o);
            }
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private static void serializeSet(NBTTagCompound tag, Set s)
    {
        tag.setString("type","set");
        tag.setString("className", s.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (Object o : s)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            if(o != null)
            {
                serializeField(tag2, "valueClass", o.getClass().getName());
                serializeField(tag2, "value", o);
            }
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private static void serializeMap(NBTTagCompound tag, Map<Object,Object> m)
    {
        tag.setString("type","map");
        tag.setString("className", m.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (Map.Entry e : m.entrySet())
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            Object key = e.getKey();
            Object value = e.getValue();
            if(key != null)
            {
                serializeField(tag2, "keyClass", key.getClass().getName());
                serializeField(tag2, "key", key);
            }
            if(value != null)
            {
                serializeField(tag2, "valueClass", value.getClass().getName());
                serializeField(tag2, "value", value);
            }
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    // ==============================================================================================================
    // Deserializing
    public static <T> T deserialize(Class<? extends T> clazz, NBTTagCompound tag)
    {
        return (T)deserializeObject(tag, clazz);
    }

    private static Object deserializeObject(NBTTagCompound tag, Class<?> clazz)
    {
        if (tag.getString("type").equals("null"))
            return null;

        if (!tag.getString("type").equals("object"))
            throw new SerializationException();

        try
        {
            Class<?> actual = Class.forName(tag.getString("className"));
            if(!clazz.isAssignableFrom(actual))
                throw new SerializationException();

            Class<?> cls = actual;

            Object o = cls.newInstance();

            // The loop skips Object
            while(cls.getSuperclass()!=null)
            {
                Field[] fields = cls.getDeclaredFields();
                for(Field f : fields)
                {
                    if(Modifier.isStatic(f.getModifiers()))
                        continue;

                    f.setAccessible(true);
                    f.set(o, deserializeField(tag, f.getName(), f.getType(), f.get(o)));
                }

                cls = cls.getSuperclass();
            }

            return o;
        }
        catch (ReflectiveOperationException e)
        {
            throw new ReportedException(new CrashReport("Exception deserializing class to NBT", e));
        }
    }

    private static Object deserializeField(NBTTagCompound tag, String fieldName, Class<?> clazz, Object currentValue)
            throws IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        if(!tag.hasKey(fieldName))
            return currentValue;

        if(clazz == Byte.class || clazz == byte.class)
        {
            return tag.getByte(fieldName);
        }
        else if(clazz == Short.class || clazz == short.class)
        {
            return tag.getShort(fieldName);
        }
        else if(clazz == Integer.class || clazz == int.class)
        {
            return tag.getInteger(fieldName);
        }
        else if(clazz == Long.class || clazz == long.class)
        {
            return tag.getLong(fieldName);
        }
        else if(clazz == Float.class || clazz == float.class)
        {
            return tag.getFloat(fieldName);
        }
        else if(clazz == Double.class || clazz == double.class)
        {
            return tag.getDouble(fieldName);
        }
        else if(clazz == Boolean.class || clazz == boolean.class)
        {
            return tag.getBoolean(fieldName);
        }
        else if(clazz == Character.class || clazz == char.class)
        {
            return tag.getInteger(fieldName);
        }
        else if(clazz == String.class)
        {
            return tag.getString(fieldName);
        }
        else if(Enum.class.isAssignableFrom(clazz))
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeEnum(tag2, (Class<? extends Enum>) clazz);
        }
        else if(clazz.isArray())
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeArray(tag2, clazz);
        }
        else if(List.class.isAssignableFrom(clazz))
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeList(tag2, (Class<? extends List>) clazz);
        }
        else if(Map.class.isAssignableFrom(clazz))
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeMap(tag2, (Class<? extends Map>) clazz);
        }
        else if(Set.class.isAssignableFrom(clazz))
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeSet(tag2, (Class<? extends Set>)clazz);
        }
        else
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeObject(tag2, clazz);
        }
    }

    private static Object deserializeEnum(NBTTagCompound tag, Class<? extends Enum> clazz)
    {
        if (!tag.getString("type").equals("enum"))
            throw new SerializationException();
        if(!tag.getString("className").equals(clazz.getName()))
            throw new SerializationException();

        return Enum.valueOf(clazz, tag.getString("value"));
    }

    private static Object deserializeArray(NBTTagCompound tag, Class<?> clazz)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if (!tag.getString("type").equals("array"))
            throw new SerializationException();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);

        Object o = Array.newInstance(clazz.getComponentType(), list.tagCount());

        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            int index = tag2.getInteger("index");

            if(!tag2.hasKey("value"))
            {
                continue;
            }

            Class<?> cls = Class.forName(tag2.getString("valueClass"));
            Object value = deserializeField(tag2, "value", cls, null);

            if(cls == Byte.class || cls == byte.class)
            {
                Array.setByte(o, index, (Byte)value);
            }
            else if(cls == Short.class || cls == short.class)
            {
                Array.setShort(o, index, (Short) value);
            }
            else if(cls == Integer.class || cls == int.class)
            {
                Array.setInt(o, index, (Integer) value);
            }
            else if(cls == Long.class || cls == long.class)
            {
                Array.setLong(o, index, (Long) value);
            }
            else if(cls == Float.class || cls == float.class)
            {
                Array.setFloat(o, index, (Float) value);
            }
            else if(cls == Double.class || cls == double.class)
            {
                Array.setDouble(o, index, (Double) value);
            }
            else if(cls == Boolean.class || cls == boolean.class)
            {
                Array.setBoolean(o, index, (Boolean) value);
            }
            else if(cls == Character.class || cls == char.class)
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

    private static List deserializeList(NBTTagCompound tag, Class<? extends List> clazz)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if (!tag.getString("type").equals("list"))
            throw new SerializationException();

        Class<?> actual = Class.forName(tag.getString("className"));
        if(!clazz.isAssignableFrom(actual))
            throw new SerializationException();

        List l = (List)actual.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);

        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            l.add(null);
        }

        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            int index = tag2.getInteger("index");

            if(!tag2.hasKey("value"))
            {
                continue;
            }

            Class<?> cls = Class.forName(tag2.getString("valueClass"));
            Object value = deserializeField(tag2, "value", cls, null);

            l.set(index, value);
        }

        return l;
    }

    private static Set deserializeSet(NBTTagCompound tag, Class<? extends Set> clazz)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if (!tag.getString("type").equals("set"))
            throw new SerializationException();

        Class<?> actual = Class.forName(tag.getString("className"));
        if(!clazz.isAssignableFrom(actual))
            throw new SerializationException();

        Set s = (Set)actual.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            Object value = null;
            if(tag2.hasKey("value"))
            {
                Class<?> cls = Class.forName(tag2.getString("valueClass"));
                value = deserializeField(tag2, "value", cls, null);
            }

            s.add(value);
        }

        return s;
    }

    private static Map deserializeMap(NBTTagCompound tag, Class<? extends Map> clazz)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if (!tag.getString("type").equals("map"))
            throw new SerializationException();

        Class<?> actual = Class.forName(tag.getString("className"));
        if(!clazz.isAssignableFrom(actual))
            throw new SerializationException();

        Map m = (Map)actual.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            Object key = null;
            Object value = null;

            if(tag2.hasKey("key"))
            {
                Class<?> clsk = Class.forName(tag2.getString("keyClass"));
                key = deserializeField(tag2, "key", clsk, null);
            }


            if(tag2.hasKey("value"))
            {
                Class<?> cls = Class.forName(tag2.getString("valueClass"));
                value = deserializeField(tag2, "value", cls, null);
            }

            m.put(key, value);
        }

        return m;
    }
}

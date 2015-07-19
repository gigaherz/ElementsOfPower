package gigaherz.util.nbt.serialization;

import net.minecraft.crash.CrashReport;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.SerializationException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NBTSerializer
{
    public static NBTTagCompound serialize(Object o)
    {
        NBTTagCompound tag = new NBTTagCompound();
        serializeObject(tag, o);
        return tag;
    }

    private static void serializeObject(NBTTagCompound tag, Object o)
    {
        tag.setString("type","object");
        tag.setString("className", o.getClass().getName());

        Class<?> cls = o.getClass();

        try
        {
            for(Field f :cls.getFields())
            {
                f.setAccessible(true);
                serializeField(tag, f.getName(), f.get(o));
            }
        }
        catch (IllegalAccessException e)
        {
            throw new ReportedException(new CrashReport("Exception serializing class to NBT", e));
        }
    }

    private static void serializeField(NBTTagCompound tag, String fieldName, Object o)
    {
        if(o instanceof Byte)
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
            serializeField(tag2, "valueClass", o.getClass().getName());
            serializeField(tag2, "value", o);
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private static void serializeSet(NBTTagCompound tag, Set s)
    {
        tag.setString("type","list");
        tag.setString("className", s.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (Object o : s)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            serializeField(tag2, "valueClass", o.getClass().getName());
            serializeField(tag2, "value", o);
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private static void serializeMap(NBTTagCompound tag, Map<Object,Object> m)
    {
        tag.setString("type","list");
        tag.setString("className", m.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (Map.Entry e : m.entrySet())
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            Object key = e.getKey();
            Object value = e.getValue();
            serializeField(tag2, "keyClass", key.getClass().getName());
            serializeField(tag2, "key", key);
            serializeField(tag2, "valueClass", value.getClass().getName());
            serializeField(tag2, "value", value);
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    public static Object deserialize(Class<?> clazz, NBTTagCompound tag)
    {
        return deserializeObject(tag, clazz);
    }

    private static Object deserializeObject(NBTTagCompound tag, Class<?> clazz)
    {
        if (!tag.getString("type").equals("object"))
            throw new SerializationException();
        if(!tag.getString("className").equals(clazz.getName()))
            throw new SerializationException();

        try
        {
            Object o = clazz.newInstance();

            for(Field f : clazz.getFields())
            {
                f.setAccessible(true);
                f.set(o, deserializeField(tag, f.getName(), f.getType(), f.get(o)));
            }

            return o;
        }
        catch (ReflectiveOperationException e)
        {
            throw new ReportedException(new CrashReport("Exception serializing class to NBT", e));
        }
    }

    private static Object deserializeField(NBTTagCompound tag, String fieldName, Class<?> clazz, Object currentValue)
            throws IllegalAccessException, ClassNotFoundException, InstantiationException
    {
        if(!tag.hasKey(fieldName))
            return currentValue;

        if(clazz == Byte.class)
        {
            return tag.getByte(fieldName);
        }
        else if(clazz == Short.class)
        {
            return tag.getShort(fieldName);
        }
        else if(clazz == Integer.class)
        {
            return tag.getInteger(fieldName);
        }
        else if(clazz == Long.class)
        {
            return tag.getLong(fieldName);
        }
        else if(clazz == Float.class)
        {
            return tag.getFloat(fieldName);
        }
        else if(clazz == Double.class)
        {
            return tag.getDouble(fieldName);
        }
        else if(clazz == Boolean.class)
        {
            return tag.getBoolean(fieldName);
        }
        else if(clazz == Character.class)
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
            return deserializeEnum(tag2, (Class<? extends Enum>)clazz);
        }
        else if(List.class.isAssignableFrom(clazz))
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeList(tag2, (Class<? extends List>)clazz);
        }
        else if(Map.class.isAssignableFrom(clazz))
        {
            NBTTagCompound tag2 = (NBTTagCompound)tag.getTag(fieldName);
            return deserializeMap(tag2, (Class<? extends Map>)clazz);
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
        if (!tag.getString("type").equals("object"))
            throw new SerializationException();
        if(!tag.getString("className").equals(clazz.getName()))
            throw new SerializationException();

        return Enum.valueOf(clazz, tag.getString("value"));
    }

    private static List deserializeList(NBTTagCompound tag, Class<? extends List> clazz)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if (!tag.getString("type").equals("object"))
            throw new SerializationException();
        if(!tag.getString("className").equals(clazz.getName()))
            throw new SerializationException();

        List l = clazz.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            int index = tag2.getInteger("index");

            Class<?> cls = Class.forName(tag2.getString("valueClass"));
            Object value = deserializeField(tag2, "value", cls, null);

            l.set(index, value);
        }

        return l;
    }

    private static Set deserializeSet(NBTTagCompound tag, Class<? extends Set> clazz)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if (!tag.getString("type").equals("object"))
            throw new SerializationException();
        if(!tag.getString("className").equals(clazz.getName()))
            throw new SerializationException();

        Set s = clazz.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            Class<?> cls = Class.forName(tag2.getString("valueClass"));
            Object value = deserializeField(tag2, "value", cls, null);

            s.add(value);
        }

        return s;
    }

    private static Map deserializeMap(NBTTagCompound tag, Class<? extends Map> clazz)
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if (!tag.getString("type").equals("object"))
            throw new SerializationException();
        if(!tag.getString("className").equals(clazz.getName()))
            throw new SerializationException();

        Map m = clazz.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            Class<?> clsk = Class.forName(tag2.getString("keyClass"));
            Object key = deserializeField(tag2, "key", clsk, null);

            Class<?> cls = Class.forName(tag2.getString("valueClass"));
            Object value = deserializeField(tag2, "value", cls, null);

            m.put(key, value);
        }

        return m;
    }
}

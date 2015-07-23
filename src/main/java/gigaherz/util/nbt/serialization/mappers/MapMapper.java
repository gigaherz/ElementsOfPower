package gigaherz.util.nbt.serialization.mappers;

import gigaherz.util.nbt.serialization.NBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.SerializationException;

import java.util.Map;

@SuppressWarnings("unchecked")
public class MapMapper extends MapperBase
{
    public MapMapper(int priority)
    {
        super(priority);
    }

    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        serializeMap(tag2, (Map) object);
        parent.setTag(fieldName, tag2);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = (NBTTagCompound) parent.getTag(fieldName);
        return deserializeMap(tag2, (Class<? extends Map>) clazz);
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object)
            throws ReflectiveOperationException
    {
        serializeMap(self, (Map) object);
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz)
            throws ReflectiveOperationException
    {
        return deserializeMap(self, (Class<? extends Map>) clazz);
    }

    private void serializeMap(NBTTagCompound tag, Map<Object, Object> m)
            throws ReflectiveOperationException
    {
        tag.setString("type", "map");
        tag.setString("className", m.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (Map.Entry e : m.entrySet())
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            Object key = e.getKey();
            Object value = e.getValue();
            if (key != null)
            {
                NBTSerializer.serializeToField(tag2, "keyClass", key.getClass().getName());
                NBTSerializer.serializeToField(tag2, "key", key);
            }
            if (value != null)
            {
                NBTSerializer.serializeToField(tag2, "valueClass", value.getClass().getName());
                NBTSerializer.serializeToField(tag2, "value", value);
            }
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private Map deserializeMap(NBTTagCompound tag, Class<? extends Map> clazz)
            throws ReflectiveOperationException
    {
        if (!tag.getString("type").equals("map"))
            throw new SerializationException();

        Class<?> actual = Class.forName(tag.getString("className"));
        if (!clazz.isAssignableFrom(actual))
            throw new SerializationException();

        Map m = (Map) actual.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            Object key = null;
            Object value = null;

            if (tag2.hasKey("key"))
            {
                Class<?> clsk = Class.forName(tag2.getString("keyClass"));
                key = NBTSerializer.deserializeToField(tag2, "key", clsk, null);
            }


            if (tag2.hasKey("value"))
            {
                Class<?> cls = Class.forName(tag2.getString("valueClass"));
                value = NBTSerializer.deserializeToField(tag2, "value", cls, null);
            }

            m.put(key, value);
        }

        return m;
    }
}

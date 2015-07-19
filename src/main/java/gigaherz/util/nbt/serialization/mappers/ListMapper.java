package gigaherz.util.nbt.serialization.mappers;

import gigaherz.util.nbt.serialization.INBTMapper;
import gigaherz.util.nbt.serialization.NBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.SerializationException;

import java.util.List;

public class ListMapper implements INBTMapper
{
    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        serializeList(tag2, (List) object);
        parent.setTag(fieldName, tag2);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = (NBTTagCompound) parent.getTag(fieldName);
        return deserializeList(tag2, (Class<? extends List>) clazz);
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object)
            throws ReflectiveOperationException
    {
        serializeList(self, (List) object);
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz)
            throws ReflectiveOperationException
    {
        return deserializeList(self, (Class<? extends List>) clazz);
    }

    private void serializeList(NBTTagCompound tag, List l)
            throws ReflectiveOperationException
    {
        tag.setString("type", "list");
        tag.setString("className", l.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (int ii = 0; ii < l.size(); ii++)
        {
            Object o = l.get(ii);
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

    private List deserializeList(NBTTagCompound tag, Class<? extends List> clazz)
            throws ReflectiveOperationException
    {
        if (!tag.getString("type").equals("list"))
            throw new SerializationException();

        Class<?> actual = Class.forName(tag.getString("className"));
        if (!clazz.isAssignableFrom(actual))
            throw new SerializationException();

        List l = (List) actual.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);

        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            l.add(null);
        }

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

            l.set(index, value);
        }

        return l;
    }
}

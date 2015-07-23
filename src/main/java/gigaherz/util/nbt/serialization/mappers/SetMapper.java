package gigaherz.util.nbt.serialization.mappers;

import gigaherz.util.nbt.serialization.NBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.SerializationException;

import java.util.Set;

@SuppressWarnings("unchecked")
public class SetMapper extends MapperBase
{
    public SetMapper(int priority)
    {
        super(priority);
    }

    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return Set.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return Set.class.isAssignableFrom(clazz);
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        serializeSet(tag2, (Set) object);
        parent.setTag(fieldName, tag2);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = (NBTTagCompound) parent.getTag(fieldName);
        return deserializeSet(tag2, (Class<? extends Set>) clazz);
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object)
            throws ReflectiveOperationException
    {
        serializeSet(self, (Set) object);
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz)
            throws ReflectiveOperationException
    {
        return deserializeSet(self, (Class<? extends Set>) clazz);
    }

    private void serializeSet(NBTTagCompound tag, Set s)
            throws ReflectiveOperationException
    {
        tag.setString("type", "set");
        tag.setString("className", s.getClass().getName());

        NBTTagList list = new NBTTagList();
        for (Object o : s)
        {
            NBTTagCompound tag2 = new NBTTagCompound();
            if (o != null)
            {
                NBTSerializer.serializeToField(tag2, "valueClass", o.getClass().getName());
                NBTSerializer.serializeToField(tag2, "value", o);
            }
            list.appendTag(tag2);
        }
        tag.setTag("elements", list);
    }

    private Set deserializeSet(NBTTagCompound tag, Class<? extends Set> clazz)
            throws ReflectiveOperationException
    {
        if (!tag.getString("type").equals("set"))
            throw new SerializationException();

        Class<?> actual = Class.forName(tag.getString("className"));
        if (!clazz.isAssignableFrom(actual))
            throw new SerializationException();

        Set s = (Set) actual.newInstance();

        NBTTagList list = tag.getTagList("elements", Constants.NBT.TAG_COMPOUND);
        for (int ii = 0; ii < list.tagCount(); ii++)
        {
            NBTTagCompound tag2 = (NBTTagCompound) list.get(ii);

            Object value = null;
            if (tag2.hasKey("value"))
            {
                Class<?> cls = Class.forName(tag2.getString("valueClass"));
                value = NBTSerializer.deserializeToField(tag2, "value", cls, null);
            }

            s.add(value);
        }

        return s;
    }

}

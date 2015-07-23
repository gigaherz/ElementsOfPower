package gigaherz.util.nbt.serialization.mappers;

import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.SerializationException;

@SuppressWarnings("unchecked")
public class EnumMapper extends MapperBase
{
    public EnumMapper(int priority)
    {
        super(priority);
    }

    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return clazz.isEnum();
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return false;
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object)
            throws ReflectiveOperationException
    {
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz)
            throws ReflectiveOperationException
    {
        return null;
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        serializeEnum(tag2, ((Enum) object));
        parent.setTag(fieldName, tag2);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = (NBTTagCompound) parent.getTag(fieldName);
        return deserializeEnum(tag2, (Class<? extends Enum>) clazz);
    }

    private static void serializeEnum(NBTTagCompound tag, Enum o)
    {
        tag.setString("type", "enum");
        tag.setString("className", o.getClass().getName());
        tag.setString("valueName", o.name());
    }

    private static Object deserializeEnum(NBTTagCompound tag, Class<? extends Enum> clazz)
    {
        if (!tag.getString("type").equals("enum"))
            throw new SerializationException();
        if (!tag.getString("className").equals(clazz.getName()))
            throw new SerializationException();

        return Enum.valueOf(clazz, tag.getString("value"));
    }
}

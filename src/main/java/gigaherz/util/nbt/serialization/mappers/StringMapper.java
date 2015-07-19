package gigaherz.util.nbt.serialization.mappers;

import net.minecraft.nbt.NBTTagCompound;

public class StringMapper implements INBTMapper
{
    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return clazz == String.class;
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return false;
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object) throws ReflectiveOperationException
    {
        parent.setString(fieldName, (String)object);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz) throws ReflectiveOperationException
    {
        return parent.getString(fieldName);
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

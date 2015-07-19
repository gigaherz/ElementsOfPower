package gigaherz.util.nbt.serialization.mappers;

import gigaherz.util.nbt.serialization.ICustomNBTSerializable;
import net.minecraft.nbt.NBTTagCompound;

public class CustomSerializableMapper implements INBTMapper
{
    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return ICustomNBTSerializable.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return ICustomNBTSerializable.class.isAssignableFrom(clazz);
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object) throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        ((ICustomNBTSerializable) object).writeToNBT(tag2);
        parent.setTag(fieldName, tag2);
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz) throws ReflectiveOperationException
    {
        NBTTagCompound tag2 = new NBTTagCompound();
        ICustomNBTSerializable o = (ICustomNBTSerializable) clazz.newInstance();
        o.readFromNBT(tag2);
        parent.setTag(fieldName, tag2);
        return o;
    }

    @Override
    public void serializeCompound(NBTTagCompound self, Object object) throws ReflectiveOperationException
    {
        ((ICustomNBTSerializable) object).writeToNBT(self);
    }

    @Override
    public Object deserializeCompound(NBTTagCompound self, Class<?> clazz) throws ReflectiveOperationException
    {
        ICustomNBTSerializable o = (ICustomNBTSerializable) clazz.newInstance();
        o.readFromNBT(self);
        return o;
    }
}

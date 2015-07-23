package gigaherz.util.nbt.serialization.mappers;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class TagMapper extends MapperBase
{
    public TagMapper(int priority)
    {
        super(priority);
    }

    @Override
    public boolean canMapToField(Class<?> clazz)
    {
        return NBTBase.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canMapToCompound(Class<?> clazz)
    {
        return false;
    }

    @Override
    public void serializeField(NBTTagCompound parent, String fieldName, Object object) throws ReflectiveOperationException
    {
        parent.setTag(fieldName, ((NBTBase) object).copy());
    }

    @Override
    public Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz) throws ReflectiveOperationException
    {
        return parent.getTag(fieldName).copy();
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

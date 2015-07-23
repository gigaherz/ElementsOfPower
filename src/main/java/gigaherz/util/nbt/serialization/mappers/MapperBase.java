package gigaherz.util.nbt.serialization.mappers;

import net.minecraft.nbt.NBTTagCompound;

public abstract class MapperBase
{
    int priority;

    public int getPriority()
    {
        return priority;
    }

    public MapperBase(int priority)
    {
        this.priority = priority;
    }

    public abstract boolean canMapToField(Class<?> clazz);

    public abstract boolean canMapToCompound(Class<?> clazz);

    public abstract void serializeField(NBTTagCompound parent, String fieldName, Object object) throws ReflectiveOperationException;

    public abstract Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz) throws ReflectiveOperationException;

    public abstract void serializeCompound(NBTTagCompound self, Object object) throws ReflectiveOperationException;

    public abstract Object deserializeCompound(NBTTagCompound self, Class<?> clazz) throws ReflectiveOperationException;
}

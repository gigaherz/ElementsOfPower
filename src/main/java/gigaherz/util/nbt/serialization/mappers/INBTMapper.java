package gigaherz.util.nbt.serialization.mappers;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTMapper
{
    boolean canMapToField(Class<?> clazz);

    boolean canMapToCompound(Class<?> clazz);

    void serializeField(NBTTagCompound parent, String fieldName, Object object) throws ReflectiveOperationException;

    Object deserializeField(NBTTagCompound parent, String fieldName, Class<?> clazz) throws ReflectiveOperationException;

    void serializeCompound(NBTTagCompound self, Object object) throws ReflectiveOperationException;

    Object deserializeCompound(NBTTagCompound self, Class<?> clazz) throws ReflectiveOperationException;
}

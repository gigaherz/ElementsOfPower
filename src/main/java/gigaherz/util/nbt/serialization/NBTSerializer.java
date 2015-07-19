package gigaherz.util.nbt.serialization;

import gigaherz.util.nbt.serialization.mappers.*;
import net.minecraft.nbt.NBTTagCompound;

import javax.management.openmbean.KeyAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

public class NBTSerializer
{
    static final List<INBTMapper> mappers = new ArrayList<INBTMapper>();
    static final GenericObjectMapper generic = new GenericObjectMapper();

    static
    {
        // Must go first so that something such as "extends Map implements ICustomNBTSerializable",
        // favor the interface over the base class
        mappers.add(new CustomSerializableMapper());

        mappers.add(new PrimitiveTypeMapper());
        mappers.add(new StringMapper());
        mappers.add(new EnumMapper());
        mappers.add(new ArrayMapper());

        mappers.add(new ListMapper());
        mappers.add(new MapMapper());
        mappers.add(new SetMapper());
    }

    public static void registerNBTMapper(INBTMapper mapper)
    {
        if (mappers.contains(mapper))
            throw new KeyAlreadyExistsException();

        mappers.add(mapper);
    }

    // ==============================================================================================================
    // Serializing
    public static NBTTagCompound serialize(Object o)
            throws ReflectiveOperationException
    {
        NBTTagCompound tag = new NBTTagCompound();
        serializeToCompound(tag, o);
        return tag;
    }

    public static void serializeToField(NBTTagCompound tag, String fieldName, Object object)
            throws ReflectiveOperationException
    {
        if (object != null)
        {
            for (INBTMapper mapper : mappers)
            {
                if (mapper.canMapToField(object.getClass()))
                {
                    mapper.serializeField(tag, fieldName, object);
                    return;
                }
            }
        }

        generic.serializeField(tag, fieldName, object);
    }

    public static void serializeToCompound(NBTTagCompound tag, Object object)
            throws ReflectiveOperationException
    {
        if (object != null)
        {
            for (INBTMapper mapper : mappers)
            {
                if (mapper.canMapToCompound(object.getClass()))
                {
                    mapper.serializeCompound(tag, object);
                    return;
                }
            }
        }

        generic.serializeCompound(tag, object);
    }

    // ==============================================================================================================
    // Deserializing
    public static <T> T deserialize(Class<? extends T> clazz, NBTTagCompound tag)
            throws ReflectiveOperationException
    {
        return (T) deserializeToCompound(tag, clazz);
    }

    public static Object deserializeToField(NBTTagCompound parent, String fieldName, Class<?> clazz, Object currentValue)
            throws ReflectiveOperationException
    {
        if (!parent.hasKey(fieldName))
            return currentValue;

        for (INBTMapper mapper : mappers)
        {
            if (mapper.canMapToField(clazz))
                return mapper.deserializeField(parent, fieldName, clazz);
        }

        return generic.deserializeField(parent, fieldName, clazz);
    }

    public static Object deserializeToCompound(NBTTagCompound self, Class<?> clazz)
            throws ReflectiveOperationException
    {
        for (INBTMapper mapper : mappers)
        {
            if (mapper.canMapToCompound(clazz))
                return mapper.deserializeCompound(self, clazz);
        }

        return generic.deserializeCompound(self, clazz);
    }
}

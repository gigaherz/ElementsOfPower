package gigaherz.util.nbt.serialization;

import net.minecraft.nbt.NBTTagCompound;

public interface ICustomNBTMapper
{
    void writeToNBT(NBTTagCompound tag);
    void readFromNBT(NBTTagCompound tag);
}

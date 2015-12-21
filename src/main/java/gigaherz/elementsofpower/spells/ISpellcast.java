package gigaherz.elementsofpower.spells;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface ISpellcast<T extends ISpellEffect>
{
    float getRemainingCastTime();

    void init(EntityPlayer player);

    T getEffect();

    void update();

    void readFromNBT(NBTTagCompound tagData);
    void writeToNBT(NBTTagCompound tagData);
}

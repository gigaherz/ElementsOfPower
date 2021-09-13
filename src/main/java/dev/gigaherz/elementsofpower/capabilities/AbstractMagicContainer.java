package dev.gigaherz.elementsofpower.capabilities;

import dev.gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AbstractMagicContainer implements IMagicContainer, INBTSerializable<CompoundTag>
{
    private MagicAmounts containedMagic = MagicAmounts.EMPTY;

    @Override
    public MagicAmounts getContainedMagic()
    {
        return containedMagic;
    }

    @Override
    public void setContainedMagic(MagicAmounts containedMagic)
    {
        this.containedMagic = containedMagic;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.put("Magic", containedMagic.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        containedMagic = new MagicAmounts(nbt.getCompound("Magic"));
    }
}

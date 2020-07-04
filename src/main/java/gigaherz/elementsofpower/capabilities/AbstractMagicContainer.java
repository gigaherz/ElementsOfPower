package gigaherz.elementsofpower.capabilities;

import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class AbstractMagicContainer implements IMagicContainer, INBTSerializable<CompoundNBT>
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
    public CompoundNBT serializeNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("Magic", containedMagic.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        containedMagic = new MagicAmounts(nbt.getCompound("Magic"));
    }
}

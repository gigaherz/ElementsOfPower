package gigaherz.elementsofpower.common;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MaterialCushion extends Material
{
    public MaterialCushion(MapColor color)
    {
        super(color);
    }

    public boolean isReplaceable()
    {
        return true;
    }

    public boolean isSolid()
    {
        return false;
    }

    public boolean blocksLight()
    {
        return false;
    }

    public boolean isOpaque()
    {
        return false;
    }
}

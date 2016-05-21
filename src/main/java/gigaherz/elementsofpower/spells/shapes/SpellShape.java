package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

public abstract class SpellShape
{
    public void spellTick(Spellcast spellcast)
    {
    }

    @Nullable
    public abstract Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast);

    public void onImpact(Spellcast spellcast, RayTraceResult mop)
    {
    }

    public float getScale(Spellcast spellcast)
    {
        return 1;
    }

    public int getInstantAnimationLength()
    {
        return 8;
    }

    public abstract boolean isInstant();
}

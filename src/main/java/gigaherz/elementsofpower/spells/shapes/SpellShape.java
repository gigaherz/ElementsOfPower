package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

public abstract class SpellShape
{
    public void spellTick(InitializedSpellcast spellcast)
    {
    }

    @Nullable
    public abstract InitializedSpellcast castSpell(ItemStack stack, PlayerEntity player, Spellcast cast);

    public void onImpact(InitializedSpellcast spellcast, RayTraceResult mop)
    {
    }

    public float getScale(InitializedSpellcast spellcast)
    {
        return 1;
    }

    public int getInstantAnimationLength()
    {
        return 8;
    }

    public abstract boolean isInstant();
}

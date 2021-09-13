package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public abstract class SpellShape
{
    public void spellTick(InitializedSpellcast spellcast)
    {
    }

    @Nullable
    public abstract InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast);

    public void onImpact(InitializedSpellcast spellcast, HitResult mop)
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

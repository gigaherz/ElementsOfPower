package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public abstract class SpellShape
{
    public Spellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        return cast;
    }

    public void spellTick(SpellcastState spellcast)
    {
    }

    public void onImpact(SpellcastState spellcast, HitResult mop, Entity directEntity)
    {
    }

    public float getScale(SpellcastState spellcast)
    {
        return 1;
    }

    public int getInstantAnimationLength()
    {
        return 8;
    }

    public abstract boolean isInstant();
}

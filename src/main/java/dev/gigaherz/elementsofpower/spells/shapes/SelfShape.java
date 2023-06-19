package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SelfShape extends SpellShape
{
    @Override
    public InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        return cast.init(player.level(), player);
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        cast.getEffect().processDirectHit(cast, cast.player, cast.player.position());
    }
}

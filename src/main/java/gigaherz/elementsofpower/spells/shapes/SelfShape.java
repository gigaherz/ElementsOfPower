package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class SelfShape extends SpellShape
{
    @Override
    public InitializedSpellcast castSpell(ItemStack stack, PlayerEntity player, Spellcast cast)
    {
        return cast.init(player.world, player);
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        cast.getEffect().processDirectHit(cast, cast.player, cast.player.getPositionVec());
    }
}

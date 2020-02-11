package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class SelfShape extends SpellShape
{
    @Override
    public Spellcast castSpell(ItemStack stack, PlayerEntity player, Spellcast cast)
    {
        return cast;
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public void spellTick(Spellcast cast)
    {
        cast.getEffect().processDirectHit(cast, cast.player, cast.player.getPositionVec());
    }
}

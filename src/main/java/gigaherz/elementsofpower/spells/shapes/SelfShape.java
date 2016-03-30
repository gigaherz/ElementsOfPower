package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class SelfShape extends SpellShape
{
    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
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
        Vec3d hitVec = new Vec3d(
                cast.player.posX,
                cast.player.posY,
                cast.player.posZ);

        cast.getEffect().processDirectHit(cast, cast.player, hitVec);
    }
}

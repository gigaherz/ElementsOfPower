package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.cast.ISpellcast;
import gigaherz.elementsofpower.spells.cast.Teleport;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpellTeleport
        extends SpellBase<SpellTeleport, Teleport>
{

    @Override
    public Teleport getNewCast()
    {
        return new Teleport(this);
    }

    @Override
    public ISpellcast castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        Teleport cast = getNewCast();
        EntityBall entity = new EntityBall(world, cast, player);

        if (world.spawnEntityInWorld(entity))
            return cast;

        return null;
    }
}

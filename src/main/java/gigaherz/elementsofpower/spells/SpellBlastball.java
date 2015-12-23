package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.cast.Blastball;
import gigaherz.elementsofpower.spells.cast.ISpellcast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpellBlastball
        extends SpellBase<SpellBlastball, Blastball>
{
    public SpellBlastball()
    {
    }

    @Override
    public Blastball getNewCast()
    {
        return new Blastball(this);
    }

    @Override
    public ISpellcast castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        Blastball cast = getNewCast();
        EntityBall entity = new EntityBall(world, cast, player);

        if (world.spawnEntityInWorld(entity))
            return cast;

        return null;
    }
}

package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.cast.effects.SpellEffect;
import gigaherz.elementsofpower.spells.cast.shapes.SpellcastBall;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpellBall extends Spell<SpellBall, SpellcastBall>
{
    public SpellBall(SpellEffect effect)
    {
        super(effect);
    }

    @Override
    public SpellcastBall getNewCast()
    {
        return new SpellcastBall(this, effect);
    }

    @Override
    public SpellcastBall castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        SpellcastBall cast = getNewCast();
        EntityBall entity = new EntityBall(world, cast, player);

        if (world.spawnEntityInWorld(entity))
            return cast;

        return null;
    }
}

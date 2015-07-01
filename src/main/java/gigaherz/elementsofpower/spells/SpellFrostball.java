package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityFrostball;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpellFrostball extends SpellBase {

    int power;

    public SpellFrostball(int power) {
        this.power = power;
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer player) {
        World world = player.worldObj;

        world.spawnEntityInWorld(new EntityFrostball(world, power, player));
    }
}

package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityAirball;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpellAirball extends SpellBase {

    int power;

    public SpellAirball(int power) {
        this.power = power;
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer player) {
        World world = player.worldObj;

        world.spawnEntityInWorld(new EntityAirball(world, power, player));
    }
}

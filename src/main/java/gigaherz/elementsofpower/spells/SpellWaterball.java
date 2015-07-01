package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityWaterball;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SpellWaterball extends SpellBase {

    int power;

    public SpellWaterball(int power) {
        this.power = power;
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer player) {
        World world = player.worldObj;

        world.spawnEntityInWorld(new EntityWaterball(world, power, player));
    }
}

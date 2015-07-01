package gigaherz.elementsofpower.spells;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellBlastball extends SpellBase {

    int power;
    boolean explode;

    public SpellBlastball(int power, boolean explode) {
        this.power = power;
        this.explode = explode;
    }

    @Override
    public void castSpell(ItemStack stack, EntityPlayer player) {
        World world = player.worldObj;
        Vec3 lookAt = player.getLook(1.0F);

        EntityFireball fireball;

        if (explode) {
            EntityLargeFireball largeFb = new EntityLargeFireball(world, player, lookAt.xCoord * 10, lookAt.yCoord * 10, lookAt.zCoord * 10);
            largeFb.explosionPower = power;
            fireball = largeFb;
        } else {
            fireball = new EntitySmallFireball(world, player, lookAt.xCoord * 10, lookAt.yCoord * 10, lookAt.zCoord * 10);
        }

        fireball.posX = player.posX + lookAt.xCoord * player.width * 0.75f;
        fireball.posY = player.posY + 1.0f;
        fireball.posZ = player.posZ + lookAt.zCoord * player.width * 0.75f;

        world.spawnEntityInWorld(fireball);
    }
}

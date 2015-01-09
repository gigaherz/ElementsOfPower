package gigaherz.elementsofpower.spells;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellFireball extends SpellBase {

    int power;
    boolean explode;

    public SpellFireball(int power, boolean explode) {
        this.power = power;
        this.explode = explode;
    }


    public static void doExplodingFireball(EntityPlayer player, int power) {
        World world = player.worldObj;
        Vec3 lookAt = player.getLook(1.0F);

        EntityLargeFireball fireball = new EntityLargeFireball(world, player, lookAt.xCoord * 10, lookAt.yCoord * 10, lookAt.zCoord * 10);

        fireball.explosionPower = power;

        fireball.posX = player.posX + lookAt.xCoord * player.width * 0.75f;
        fireball.posY = player.posY + 1.0f;
        fireball.posZ = player.posZ + lookAt.zCoord * player.width * 0.75f;

        world.spawnEntityInWorld(fireball);
    }

    public static void doSmallFireball(EntityPlayer player, int power) {
        World world = player.worldObj;
        Vec3 lookAt = player.getLook(1.0F);

        EntitySmallFireball fireball = new EntitySmallFireball(world, player, lookAt.xCoord * 10, lookAt.yCoord * 10, lookAt.zCoord * 10);

        fireball.posX = player.posX + lookAt.xCoord * 2;
        fireball.posY = player.posY + 1.0f;
        fireball.posZ = player.posZ + lookAt.zCoord * 2;

        world.spawnEntityInWorld(fireball);
    }


    @Override
    public void castSpell(ItemStack stack, EntityPlayer player) {
        if (explode) {
            doExplodingFireball(player, power);
        } else {
            doSmallFireball(player, power);
        }
    }
}

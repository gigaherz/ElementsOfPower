package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.entities.EntityTeleporter;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SpellTeleport extends SpellBase
{
    @Override
    public void castSpell(ItemStack stack, EntityPlayer player)
    {
        World world = player.worldObj;
        Vec3 lookAt = player.getLook(1.0F);

        EntityTeleporter fireball;

        fireball = new EntityTeleporter(world, player);

        fireball.posX = player.posX + lookAt.xCoord * player.width * 0.75f;
        fireball.posY = player.posY + 1.0f;
        fireball.posZ = player.posZ + lookAt.zCoord * player.width * 0.75f;

        world.spawnEntityInWorld(fireball);
    }
}

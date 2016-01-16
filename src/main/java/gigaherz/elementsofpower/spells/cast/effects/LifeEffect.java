package gigaherz.elementsofpower.spells.cast.effects;

import gigaherz.elementsofpower.spells.cast.Spellcast;
import gigaherz.elementsofpower.spells.cast.shapes.SpellcastBall;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;

import java.util.List;

public class LifeEffect extends SpellEffect
{
    @Override
    public int getColor(Spellcast cast)
    {
        return 0xA0E0FF;
    }

    @Override
    public int getBeamDuration(Spellcast cast)
    {
        return 20 * 5;
    }

    @Override
    public int getBeamInterval(Spellcast cast)
    {
        return 8;
    }

    private void burnEntities(Spellcast cast, Vec3 hitVec, List<? extends EntityLivingBase> living)
    {
        for (EntityLivingBase e : living)
        {
            if (!e.isEntityAlive())
                continue;

            double dx = e.posX - hitVec.xCoord;
            double dy = e.posY - hitVec.yCoord;
            double dz = e.posZ - hitVec.zCoord;

            double ll = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double lv = Math.max(0, cast.getDamageForce() - ll);

            causePotionEffect(cast, e, Potion.heal, 0, lv * 0.5, 0.0);
            causePotionEffect(cast, e, Potion.regeneration, 0, lv, 100.0);
        }
    }

    private void causePotionEffect(Spellcast cast, EntityLivingBase e, Potion potion, int amplifier, double distance, double durationBase)
    {
        int id = potion.getId();
        if (Potion.potionTypes[id].isInstant())
        {
            Potion.potionTypes[id].affectEntity(cast.projectile, cast.player, e, amplifier, distance);
        }
        else
        {
            int j = (int) (distance * durationBase + 0.5D);

            if (j > 20)
            {
                e.addPotionEffect(new PotionEffect(id, j, amplifier));
            }
        }
    }

    @Override
    public void processDirectHit(Spellcast cast, Entity e)
    {

    }

    @Override
    public boolean processEntitiesAroundBefore(Spellcast cast, Vec3 hitVec)
    {
        return true;
    }

    @Override
    public void processEntitiesAroundAfter(Spellcast cast, Vec3 hitVec)
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                hitVec.xCoord - cast.getDamageForce(),
                hitVec.yCoord - cast.getDamageForce(),
                hitVec.zCoord - cast.getDamageForce(),
                hitVec.xCoord + cast.getDamageForce(),
                hitVec.yCoord + cast.getDamageForce(),
                hitVec.zCoord + cast.getDamageForce());

        List<EntityLivingBase> living = cast.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb);
        burnEntities(cast, hitVec, living);
    }

    @Override
    public void spawnBallParticles(Spellcast cast, MovingObjectPosition mop)
    {
        cast.spawnRandomParticle(EnumParticleTypes.FLAME,
                mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
    }

    @Override
    public void processBlockWithinRadius(Spellcast cast, BlockPos blockPos, IBlockState currentState, int layers)
    {
        Block block = currentState.getBlock();

        if (block == Blocks.dirt)
        {
            switch (currentState.getValue(BlockDirt.VARIANT))
            {
                case COARSE_DIRT:
                    cast.world.setBlockState(blockPos, Blocks.dirt.getDefaultState());
                    break;
                case DIRT:
                    cast.world.setBlockState(blockPos, Blocks.grass.getDefaultState());
                    break;
            }
        }
    }
}

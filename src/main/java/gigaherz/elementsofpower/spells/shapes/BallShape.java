package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BallShape extends SpellShape
{
    @Override
    public float getScale(Spellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
    {
        World world = player.worldObj;
        EntityBall entity = new EntityBall(world, cast, player);

        entity.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);

        if (world.spawnEntityInWorld(entity))
            return cast;

        return null;
    }

    public void onImpact(Spellcast cast, RayTraceResult mop)
    {
        SpellEffect effect = cast.getEffect();

        if (mop.entityHit != null)
        {
            effect.processDirectHit(cast, mop.entityHit, mop.hitVec);
        }

        effect.spawnBallParticles(cast, mop);

        if (!effect.processEntitiesAroundBefore(cast, mop.hitVec))
            return;

        int force = cast.getDamageForce();
        if (force > 0)
        {
            BlockPos bp = mop.getBlockPos();

            if (mop.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                bp = bp.offset(mop.sideHit);
            }
            else
            {
                bp = new BlockPos(mop.hitVec);
            }

            int px = bp.getX();
            int py = bp.getY();
            int pz = bp.getZ();
            for (int z = pz - force; z <= pz + force; z++)
            {
                for (int x = px - force; x <= px + force; x++)
                {
                    for (int y = py - force; y <= py + force; y++)
                    {
                        float dx = Math.abs(px - x);
                        float dy = Math.abs(py - y);
                        float dz = Math.abs(pz - z);
                        float r2 = (dx * dx + dy * dy + dz * dz);
                        boolean in_sphere = r2 <= (force * force);
                        if (!in_sphere)
                            continue;

                        float r = (float) Math.sqrt(r2);

                        BlockPos np = new BlockPos(x, y, z);

                        IBlockState currentState = cast.world.getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, mop.hitVec);
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }
}

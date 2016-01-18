package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class BallShape extends SpellShape
{
    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
    {
        World world = player.worldObj;
        EntityBall entity = new EntityBall(world, cast, player);

        if (world.spawnEntityInWorld(entity))
            return cast;

        return null;
    }

    public void onImpact(Spellcast cast, MovingObjectPosition mop)
    {
        SpellEffect effect = cast.getEffect();

        if (mop.entityHit != null)
        {
            effect.processDirectHit(cast, mop.entityHit);
        }

        effect.spawnBallParticles(cast, mop);

        if (!effect.processEntitiesAroundBefore(cast, mop.hitVec))
            return;

        int force = cast.getDamageForce();
        if (force > 0)
        {
            BlockPos bp = mop.getBlockPos();

            if (bp != null)
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

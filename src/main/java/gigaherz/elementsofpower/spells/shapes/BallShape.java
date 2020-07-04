package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.entities.BallEntity;
import gigaherz.elementsofpower.spells.InitializedSpellcast;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BallShape extends SpellShape
{
    @Override
    public float getScale(InitializedSpellcast cast)
    {
        return 1 + 0.25f * cast.getDamageForce();
    }

    @Override
    public InitializedSpellcast castSpell(ItemStack stack, PlayerEntity player, Spellcast cast)
    {
        InitializedSpellcast spellcast = cast.init(player.world, player);

        World world = player.world;
        BallEntity entity = new BallEntity(world, player, spellcast);

        entity.func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 2.5F, 1.0F);

        if (world.addEntity(entity))
            return spellcast;

        return null;
    }

    @Override
    public void onImpact(InitializedSpellcast cast, RayTraceResult mop)
    {
        SpellEffect effect = cast.getEffect();

        if (mop.getType() == RayTraceResult.Type.ENTITY)
        {
            effect.processDirectHit(cast, ((EntityRayTraceResult) mop).getEntity(), mop.getHitVec());
        }

        effect.spawnBallParticles(cast, mop);

        if (!effect.processEntitiesAroundBefore(cast, mop.getHitVec()))
            return;

        int force = cast.getDamageForce();
        if (force > 0)
        {
            BlockPos bp;
            Vector3d vec;
            Vector3d dir;
            if (mop.getType() == RayTraceResult.Type.BLOCK)
            {
                BlockRayTraceResult blockTrace = (BlockRayTraceResult) mop;
                bp = blockTrace.getPos();
                bp = bp.offset(blockTrace.getFace());
                vec = Vector3d.func_237491_b_(bp);
                dir = Vector3d.func_237491_b_(blockTrace.getFace().getDirectionVec());
            }
            else
            {
                bp = new BlockPos(mop.getHitVec());
                vec = mop.getHitVec();
                dir = new Vector3d(0, 0, 0);
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

                        BlockPos np = new BlockPos(x, y, z);

                        Vector3d start = vec.add(dir.scale(0.5));
                        Vector3d end = new Vector3d(px + 0.5, py + 0.5, pz + 0.5);
                        BlockRayTraceResult mop2 = cast.world.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, cast.player));
                        if (mop2.getType() != RayTraceResult.Type.MISS)
                            if (!mop2.getPos().equals(np))
                                continue;

                        float r = (float) Math.sqrt(r2);


                        BlockState currentState = cast.world.getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, mop.getHitVec());
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }
}

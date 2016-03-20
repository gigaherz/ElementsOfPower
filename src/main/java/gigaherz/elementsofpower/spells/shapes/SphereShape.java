package gigaherz.elementsofpower.spells.shapes;

import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class SphereShape extends SpellShape
{
    @Override
    public Spellcast castSpell(ItemStack stack, EntityPlayer player, Spellcast cast)
    {
        return cast;
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public float getScale(Spellcast spellcast)
    {
        return 1 + spellcast.getDamageForce();
    }

    @Override
    public void spellTick(Spellcast cast)
    {
        SpellEffect effect = cast.getEffect();

        EntityPlayer player = cast.player;

        if (!effect.processEntitiesAroundBefore(cast, player.getPositionVector()))
            return;

        int force = cast.getDamageForce();
        if (force > 0)
        {
            BlockPos bp = player.getPosition();

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

        effect.processEntitiesAroundAfter(cast, player.getPositionVector());
    }
}
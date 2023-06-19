package dev.gigaherz.elementsofpower.spells.shapes;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SphereShape extends SpellShape
{
    @Override
    public InitializedSpellcast castSpell(ItemStack stack, Player player, Spellcast cast)
    {
        return cast.init(player.level(), player);
    }

    @Override
    public boolean isInstant()
    {
        return true;
    }

    @Override
    public float getScale(InitializedSpellcast spellcast)
    {
        return 1 + spellcast.getDamageForce();
    }

    @Override
    public void spellTick(InitializedSpellcast cast)
    {
        SpellEffect effect = cast.getEffect();

        Player player = cast.player;

        if (!effect.processEntitiesAroundBefore(cast, player.getEyePosition(1.0f)))
            return;

        int force = cast.getDamageForce();
        if (force > 0)
        {
            BlockPos bp = player.blockPosition().relative(Direction.UP, Mth.floor(player.getEyeHeight() + 0.25));

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

                        BlockState currentState = cast.world.getBlockState(np);

                        effect.processBlockWithinRadius(cast, np, currentState, r, null);
                    }
                }
            }
        }

        effect.processEntitiesAroundAfter(cast, player.position());
    }
}
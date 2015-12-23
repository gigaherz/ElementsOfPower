package gigaherz.elementsofpower.spells.cast;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.SpellBlastball;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

public class Blastball extends Spellcast<SpellBlastball> implements ISpellcastBall<SpellBlastball>
{
    public Blastball(SpellBlastball parent)
    {
        super(parent);
    }

    @Override
    public void setProjectile(EntityBall entityBall)
    {
    }

    public void onImpact(MovingObjectPosition mop, Random rand)
    {
        if (!world.isRemote)
        {
            boolean flag = world.getGameRules().getBoolean("mobGriefing");
            world.newExplosion(null, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                    getDamageForce(), flag, flag);
        }
    }
}

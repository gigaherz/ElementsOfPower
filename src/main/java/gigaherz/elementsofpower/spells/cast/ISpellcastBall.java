package gigaherz.elementsofpower.spells.cast;

import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.spells.SpellBase;
import net.minecraft.util.MovingObjectPosition;

import java.util.Random;

public interface ISpellcastBall<T extends SpellBase> extends ISpellcast<T>
{
    void onImpact(MovingObjectPosition mop, Random rand);
    int getDamageForce();

    void setProjectile(EntityBall entityBall);
}

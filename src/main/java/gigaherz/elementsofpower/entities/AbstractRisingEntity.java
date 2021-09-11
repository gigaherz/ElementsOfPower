package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class AbstractRisingEntity extends AbstractSpellEntity
{
    public AbstractRisingEntity(EntityType<?> entityTypeIn, World worldIn, InitializedSpellcast spellcast)
    {
        super(entityTypeIn, worldIn, spellcast);
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    @Override
    public float getCollisionBorderSize() {
        return 0.0F;
    }

}

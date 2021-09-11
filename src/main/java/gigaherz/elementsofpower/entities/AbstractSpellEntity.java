package gigaherz.elementsofpower.entities;

import gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class AbstractSpellEntity extends Entity implements IEntityAdditionalSpawnData
{
    InitializedSpellcast spellcast;

    public AbstractSpellEntity(EntityType<?> entityTypeIn, World worldIn, InitializedSpellcast spellcast)
    {
        super(entityTypeIn, worldIn);

        this.spellcast = spellcast;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

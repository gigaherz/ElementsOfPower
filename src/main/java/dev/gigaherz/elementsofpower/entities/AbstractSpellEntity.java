package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public abstract class AbstractSpellEntity extends Entity implements IEntityAdditionalSpawnData
{
    InitializedSpellcast spellcast;

    public AbstractSpellEntity(EntityType<?> entityTypeIn, Level worldIn, InitializedSpellcast spellcast)
    {
        super(entityTypeIn, worldIn);

        this.spellcast = spellcast;
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityAdditionalSpawnData;
import net.neoforged.neoforge.network.NetworkHooks;

public abstract class AbstractSpellEntity extends Entity implements IEntityAdditionalSpawnData
{
    Spellcast spellcast;

    public AbstractSpellEntity(EntityType<?> entityTypeIn, Level worldIn, Spellcast spellcast)
    {
        super(entityTypeIn, worldIn);

        this.spellcast = spellcast;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

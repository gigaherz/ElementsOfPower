package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class AbstractRisingEntity extends AbstractSpellEntity
{
    public AbstractRisingEntity(EntityType<?> entityTypeIn, Level worldIn, InitializedSpellcast spellcast)
    {
        super(entityTypeIn, worldIn, spellcast);
    }

    @Override
    public void push(Entity entityIn)
    {
    }

    @Override
    public float getPickRadius()
    {
        return 0.0F;
    }
}

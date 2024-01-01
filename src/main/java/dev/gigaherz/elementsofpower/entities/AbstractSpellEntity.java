package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public abstract class AbstractSpellEntity extends Entity implements IEntityWithComplexSpawn
{
    Spellcast spellcast;

    public AbstractSpellEntity(EntityType<?> entityTypeIn, Level worldIn, Spellcast spellcast)
    {
        super(entityTypeIn, worldIn);

        this.spellcast = spellcast;
    }
}

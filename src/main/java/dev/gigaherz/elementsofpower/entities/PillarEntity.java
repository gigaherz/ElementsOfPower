package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class PillarEntity extends Entity implements IEntityAdditionalSpawnData
{
    private InitializedSpellcast spellcast;

    public PillarEntity(Level level, LivingEntity caster, InitializedSpellcast spellcast)
    {
        super(ElementsOfPowerMod.PILLAR_ENTITY_TYPE.get(), level);

        this.spellcast = spellcast;
    }

    public PillarEntity(EntityType<PillarEntity> pEntityType, Level pLevel)
    {
        super(pEntityType, pLevel);
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    protected void defineSynchedData()
    {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound)
    {

    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {

    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData)
    {

    }
}

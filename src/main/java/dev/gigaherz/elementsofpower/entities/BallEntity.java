package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class BallEntity extends ThrowableProjectile implements IEntityAdditionalSpawnData
{
    InitializedSpellcast spellcast;

    public BallEntity(Level worldIn, LivingEntity thrower, InitializedSpellcast spellcast)
    {
        super(ElementsOfPowerMod.BALL_ENTITY_TYPE.get(), thrower, worldIn);

        this.spellcast = spellcast;
    }

    public BallEntity(EntityType<BallEntity> type, Level world)
    {
        super(type, world);
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    protected float getGravity()
    {
        return 0.001F;
    }

    @Override
    protected void onHit(HitResult pos)
    {
        if (!this.level().isClientSide)
        {
            if (getSpellcast() != null)
                spellcast.onImpact(pos, random, this);

            this.remove(RemovalReason.DISCARDED);
        }
    }

    public float getScale()
    {
        if (getSpellcast() != null)
            return 0.6f * (1 + spellcast.getDamageForce());
        return 0;
    }

    public int getColor()
    {
        if (getSpellcast() != null)
            return spellcast.getColor();
        return 0xFFFFFF;
    }

    @Nullable
    public InitializedSpellcast getSpellcast()
    {
        return spellcast;
    }


    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        CompoundTag spellcastData = new CompoundTag();
        spellcast.write(spellcastData);
        pCompound.put("spellcast", spellcastData);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        var player = level().getPlayerByUUID(pCompound.getUUID("owner"));
        var spellcastData = pCompound.getCompound("spellcast");
        spellcast = InitializedSpellcast.read(spellcastData, level(), player);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        CompoundTag spellcastData = new CompoundTag();
        spellcast.write(spellcastData);
        buffer.writeNbt(spellcastData);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData)
    {
        var spellcastData = additionalData.readNbt();
        if (spellcastData != null)
            spellcast.read(spellcastData);
    }
}

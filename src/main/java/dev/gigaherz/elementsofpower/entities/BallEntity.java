package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import dev.gigaherz.elementsofpower.spells.SpellcastState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BallEntity extends ThrowableProjectile implements IEntityWithComplexSpawn
{
    private Player player;
    private UUID playerUUID;
    private Spellcast spellcast;


    public BallEntity(Level worldIn, Player caster, Spellcast spellcast)
    {
        super(ElementsOfPowerMod.BALL_ENTITY_TYPE.get(), caster, worldIn);

        this.playerUUID = caster.getUUID();
        this.player = caster;
        this.spellcast = spellcast;
    }

    public BallEntity(EntityType<BallEntity> type, Level world)
    {
        super(type, world);
    }

    @Nullable
    public Player getCaster()
    {
        if (playerUUID != null && player == null)
        {
            player = level().getPlayerByUUID(playerUUID);
            if (player == null) // not found, give up
                playerUUID = null;
        }
        return player;
    }

    public SpellcastState getState()
    {
        return SpellcastState.get(getCaster());
    }

    public float getScale()
    {
        if (getSpellcast() != null)
            return 0.6f * (1 + getState().damageForce());
        return 0;
    }

    public int getColor()
    {
        if (getSpellcast() != null)
            return getState().color();
        return 0xFFFFFFFF;
    }

    @Nullable
    public Spellcast getSpellcast()
    {
        return spellcast;
    }

    // Entity impl

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
            SpellcastState state = getState();
            if (getSpellcast() == state.spellcast())
                state.onImpact(pos, random, this);

            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound)
    {
        super.addAdditionalSaveData(pCompound);
        var spellcastData = spellcast.serializeNBT();
        pCompound.put("spellcast", spellcastData);
        pCompound.putUUID("owner", playerUUID);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound)
    {
        super.readAdditionalSaveData(pCompound);
        playerUUID = pCompound.getUUID("owner");
        var spellcastData = pCompound.getCompound("spellcast");
        spellcast = Spellcast.read(spellcastData);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        var spellcastData = spellcast.serializeNBT();
        buffer.writeNbt(spellcastData);
        buffer.writeUUID(playerUUID);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        var spellcastData = buffer.readNbt();
        if (spellcastData != null)
            spellcast = Spellcast.read(spellcastData);
        playerUUID = buffer.readUUID();
    }
}

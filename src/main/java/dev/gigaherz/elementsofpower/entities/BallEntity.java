package dev.gigaherz.elementsofpower.entities;

import dev.gigaherz.elementsofpower.spells.InitializedSpellcast;
import dev.gigaherz.elementsofpower.spells.SpellManager;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.nbt.Tag;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class BallEntity extends ThrowableProjectile implements IEntityAdditionalSpawnData
{
    @ObjectHolder("elementsofpower:ball")
    public static EntityType<BallEntity> TYPE;

    InitializedSpellcast spellcast;

    private static final EntityDataAccessor<CompoundTag> SEQ = SynchedEntityData.defineId(BallEntity.class, EntityDataSerializers.COMPOUND_TAG);

    public BallEntity(Level worldIn, LivingEntity thrower, InitializedSpellcast spellcast)
    {
        super(TYPE, thrower, worldIn);

        this.spellcast = spellcast;
        spellcast.setProjectile(this);

        CompoundTag tag = new CompoundTag();
        tag.putInt("caster", spellcast.player.getId());
        tag.put("sequence", spellcast.getSequenceNBT());
        getEntityData().set(SEQ, tag);
    }

    public BallEntity(EntityType<BallEntity> type, Level world)
    {
        super(type, world);
    }

    @Override
    protected void defineSynchedData()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("sequence", new ListTag());
        getEntityData().define(SEQ, tag);
    }

    @Override
    protected float getGravity()
    {
        return 0.001F;
    }

    @Override
    protected void onHit(HitResult pos)
    {
        if (!this.level.isClientSide)
        {
            if (getSpellcast() != null)
                spellcast.onImpact(pos, random);

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
        if (spellcast == null)
        {
            CompoundTag tag = getEntityData().get(SEQ);
            if (tag.contains("sequence", Tag.TAG_LIST) && tag.contains("caster", Tag.TAG_INT))
            {
                Player e = (Player) this.level.getEntity(tag.getInt("caster"));
                if (e != null)
                {
                    ListTag sequence = tag.getList("sequence", Tag.TAG_STRING);
                    Spellcast ccast = SpellManager.makeSpell(sequence);
                    if (ccast != null)
                    {
                        spellcast = ccast.init(level, e);
                    }
                }
            }
        }
        return spellcast;
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
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

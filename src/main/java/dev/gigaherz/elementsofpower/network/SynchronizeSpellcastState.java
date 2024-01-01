package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.Nullable;

public class SynchronizeSpellcastState implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("sync_spellcast_state");

    public enum ChangeMode
    {
        BEGIN,
        END,
        INTERRUPT,
        CANCEL;
        public static final ChangeMode values[] = values();
    }

    public final ChangeMode changeMode;
    public final int casterID;
    public final CompoundTag spellcast;
    public final int remainingCastTime;
    public final int remainingInterval;
    public final int totalCastTime;

    public SynchronizeSpellcastState(ChangeMode mode, Player player, @Nullable Spellcast cast, int remainingCastTime, int remainingInterval, int totalCastTime)
    {
        this.changeMode = mode;
        this.casterID = player.getId();
        this.spellcast = cast != null ? cast.serializeNBT() : new CompoundTag();
        this.remainingCastTime = remainingCastTime;
        this.remainingInterval = remainingInterval;
        this.totalCastTime = totalCastTime;
    }

    public SynchronizeSpellcastState(FriendlyByteBuf buf)
    {
        this.changeMode = ChangeMode.values[buf.readInt()];
        this.casterID = buf.readInt();
        this.spellcast = buf.readNbt();
        this.remainingCastTime = buf.readVarInt();
        this.remainingInterval = buf.readVarInt();
        this.totalCastTime = buf.readVarInt();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeInt(changeMode.ordinal());
        buf.writeInt(casterID);
        buf.writeNbt(spellcast);
        buf.writeVarInt(remainingCastTime);
        buf.writeVarInt(remainingInterval);
        buf.writeVarInt(totalCastTime);
    }

    @Override
    public ResourceLocation id()
    {
        return ID;
    }

    public void handle(PlayPayloadContext context)
    {
        ClientPacketHandlers.handleSpellcastSync(this);
    }
}

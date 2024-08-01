package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.client.ClientPacketHandlers;
import dev.gigaherz.elementsofpower.spells.Spellcast;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record SynchronizeSpellcastState(
        ChangeMode changeMode,
        int casterID,
        CompoundTag spellcast,
        int remainingCastTime,
        int remainingInterval,
        int totalCastTime
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("sync_spellcast_state");
    public static final Type<SynchronizeSpellcastState> TYPE = new Type<>(ID);

    public enum ChangeMode
    {
        BEGIN,
        END,
        INTERRUPT,
        CANCEL;
        public static final StreamCodec<FriendlyByteBuf, ChangeMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ChangeMode.class);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SynchronizeSpellcastState> STREAM_CODEC = StreamCodec.composite(
            ChangeMode.STREAM_CODEC, SynchronizeSpellcastState::changeMode,
            ByteBufCodecs.VAR_INT, SynchronizeSpellcastState::casterID,
            ByteBufCodecs.COMPOUND_TAG, SynchronizeSpellcastState::spellcast,
            ByteBufCodecs.VAR_INT, SynchronizeSpellcastState::remainingCastTime,
            ByteBufCodecs.VAR_INT, SynchronizeSpellcastState::remainingInterval,
            ByteBufCodecs.VAR_INT, SynchronizeSpellcastState::totalCastTime,
            SynchronizeSpellcastState::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        ClientPacketHandlers.handleSpellcastSync(this);
    }
}

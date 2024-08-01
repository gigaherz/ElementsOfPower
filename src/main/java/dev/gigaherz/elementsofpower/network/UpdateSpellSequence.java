package dev.gigaherz.elementsofpower.network;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.spells.Element;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record UpdateSpellSequence(
        int slotNumber, ChangeMode changeMode,
        List<Element> sequence,
        int useTicks
) implements CustomPacketPayload
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("update_spell_sequence");
    public static final Type<UpdateSpellSequence> TYPE = new Type<>(ID);

    public enum ChangeMode
    {
        BEGIN,
        PARTIAL,
        COMMIT,
        CANCEL;
        public static final StreamCodec<FriendlyByteBuf, ChangeMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ChangeMode.class);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSpellSequence> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UpdateSpellSequence::slotNumber,
            ChangeMode.STREAM_CODEC, UpdateSpellSequence::changeMode,
            nullableStreamCodec(ByteBufCodecs.collection(ArrayList::new, Element.STREAM_CODEC)), UpdateSpellSequence::sequence,
            ByteBufCodecs.VAR_INT, UpdateSpellSequence::useTicks,
            UpdateSpellSequence::new
    );

    private static <B extends ByteBuf, V> StreamCodec<B, @Nullable V> nullableStreamCodec(StreamCodec<B,V> inner)
    {
        return new StreamCodec<B, @Nullable V>()
        {
            @Nullable
            @Override
            public V decode(B buf)
            {
                var isPresent = buf.readBoolean();
                return isPresent ? inner.decode(buf) : null;
            }

            @Override
            public void encode(B buf, @Nullable V value)
            {
                buf.writeBoolean(value != null);
                if (value != null)
                {
                    inner.encode(buf, value);
                }
            }
        };
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public void handle(IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            Player player = context.player();
            ItemStack stack = player.getInventory().getItem(slotNumber);

            if (stack.getItem() instanceof WandItem wand)
            {
                wand.processSequenceUpdate(this, stack, player, useTicks);
            }
        });
    }
}

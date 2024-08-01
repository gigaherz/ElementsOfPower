package dev.gigaherz.elementsofpower.items;

import com.mojang.serialization.Codec;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum TransferMode
{
    PASSIVE,
    ACTIVE,
    DISABLED;

    static TransferMode[] values = values();

    public static final IntFunction<TransferMode> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<TransferMode> CODEC = Codec.INT.xmap(BY_ID::apply, Enum::ordinal);
    public static final StreamCodec<ByteBuf, TransferMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

}

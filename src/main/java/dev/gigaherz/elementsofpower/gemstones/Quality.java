package dev.gigaherz.elementsofpower.gemstones;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Rarity;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public enum Quality
{
    ROUGH(0, Rarity.COMMON, 0.9f, "text.elementsofpower.gemstone.quality.rough", "text.elementsofpower.gem_container.quality.rough"),
    COMMON(1, Rarity.COMMON, 1.0f, "text.elementsofpower.gemstone.quality.common", "text.elementsofpower.gem_container.quality.common"),
    SMOOTH(2, Rarity.UNCOMMON, 1.25f, "text.elementsofpower.gemstone.quality.smooth", "text.elementsofpower.gem_container.quality.smooth"),
    FLAWLESS(3, Rarity.RARE, 1.5f, "text.elementsofpower.gemstone.quality.flawless", "text.elementsofpower.gem_container.quality.flawless"),
    PURE(4, Rarity.EPIC, 2.0f, "text.elementsofpower.gemstone.quality.pure", "text.elementsofpower.gem_container.quality.pure");

    public static final Quality[] values = values();


    public static final IntFunction<Quality> BY_ID = ByIdMap.continuous(i -> i.index, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<Quality> CODEC = Codec.INT.xmap(BY_ID::apply, i -> i.index);
    public static final StreamCodec<ByteBuf, Quality> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, i -> i.index);

    private final int index;
    private final String translationKey;
    private final Rarity rarity;
    private final float transferSpeed;
    private final String containerTranslationKey;

    Quality(int index, Rarity rarity, float transferSpeed, String translationKey, String containerTranslationKey)
    {
        this.index = index;
        this.translationKey = translationKey;
        this.rarity = rarity;
        this.transferSpeed = transferSpeed;
        this.containerTranslationKey = containerTranslationKey;
    }

    public String getTranslationKey()
    {
        return translationKey;
    }

    public Rarity getRarity()
    {
        return rarity;
    }

    public float getTransferSpeed()
    {
        return transferSpeed;
    }

    public String getContainerTranslationKey()
    {
        return containerTranslationKey;
    }

    public int getIndex()
    {
        return index;
    }

    @Nullable
    public static Quality byIndex(int index)
    {
        for (Quality q : values)
        {
            if (q.index == index)
                return q;
        }

        return null;
    }
}

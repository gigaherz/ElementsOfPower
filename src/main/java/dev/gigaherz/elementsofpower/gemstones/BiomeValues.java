package dev.gigaherz.elementsofpower.gemstones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.spells.Element;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public record BiomeValues(BiomeValue heat, BiomeValue humidity, BiomeValue life)
{
    public static final Codec<BiomeValues> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BiomeValue.CODEC.fieldOf("heat").forGetter(BiomeValues::heat),
            BiomeValue.CODEC.fieldOf("humidity").forGetter(BiomeValues::humidity),
            BiomeValue.CODEC.fieldOf("life").forGetter(BiomeValues::life)
    ).apply(inst, BiomeValues::new));

    private static final Pattern STRING_REGEX = Pattern.compile("^(for|neutral|against)_(for|neutral|against)_(for|neutral|against)$");

    public static final Codec<BiomeValues> STRING_CODEC = Codec.STRING.comapFlatMap(
            str -> {
                var split = STRING_REGEX.matcher(str);

                if (!split.matches())
                    return DataResult.error(() -> "Incorrect format '" + str + "' must match " + STRING_REGEX.pattern());

                var heat = BiomeValue.byName(split.group(1));
                if (heat == null)
                    return DataResult.error(() -> "No BiomeValue found for name '" + str + "'");

                var humidity = BiomeValue.byName(split.group(2));
                if (humidity == null)
                    return DataResult.error(() -> "No BiomeValue found for name '" + str + "'");

                var life = BiomeValue.byName(split.group(3));
                if (life == null)
                    return DataResult.error(() -> "No BiomeValue found for name '" + str + "'");

                return DataResult.success(new BiomeValues(heat, humidity, life));
            },
            val -> val.heat.getSerializedName() + "_" + val.humidity.getSerializedName() + "_" + val.life.getSerializedName()
    );

    public int getBiomeBonus(@Nullable Element e)
    {
        if (e == null)
            return 1;
        return switch (e)
        {
            case FIRE -> heat.value();
            case WATER -> humidity.value();
            case LIFE -> life.value();
            case CHAOS -> 2 - life.value();
            default -> 1;
        };
    }
}

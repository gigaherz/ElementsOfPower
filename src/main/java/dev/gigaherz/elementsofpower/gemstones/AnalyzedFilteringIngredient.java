package dev.gigaherz.elementsofpower.gemstones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public record AnalyzedFilteringIngredient(Ingredient inner) implements ICustomIngredient
{
    @Override
    public boolean test(@Nullable ItemStack stack)
    {
        if (stack != null)
        {
            var gemProperties = GemstoneProperties.getProperties(stack);
            if (gemProperties == null)
                return false;
        }
        return inner.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems()
    {
        return Arrays.stream(inner.getItems());
    }

    @Override
    public boolean isSimple()
    {
        return inner.isSimple();
    }

    @Override
    public IngredientType<?> getType()
    {
        return ElementsOfPowerMod.ANALYZED_FILTERING_INGREDIENT.get();
    }

    public static MapCodec<AnalyzedFilteringIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("inner").forGetter(i -> i.inner)
    ).apply(instance, AnalyzedFilteringIngredient::new));

    public static StreamCodec<RegistryFriendlyByteBuf, AnalyzedFilteringIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, i -> i.inner,
            AnalyzedFilteringIngredient::new);
}

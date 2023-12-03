package dev.gigaherz.elementsofpower.gemstones;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class AnalyzedFilteringIngredient extends Ingredient
{
    public static final ResourceLocation ID = ElementsOfPowerMod.location("not_analyzed");

    final Ingredient inner;

    protected AnalyzedFilteringIngredient(Ingredient inner)
    {
        super(Stream.empty());
        this.inner = inner;
    }

    public static Ingredient wrap(Ingredient ingredient)
    {
        return new AnalyzedFilteringIngredient(ingredient);
    }

    @Override
    public ItemStack[] getItems()
    {
        return inner.getItems();
    }

    @Override
    public boolean test(@Nullable ItemStack stack)
    {
        if (stack != null && stack.getItem() instanceof GemstoneItem)
        {
            if (((GemstoneItem) stack.getItem()).getQuality(stack) != null)
                return false;
        }
        return inner.test(stack);
    }

    @Override
    public IntList getStackingIds()
    {
        return inner.getStackingIds();
    }

    @Override
    public boolean isEmpty()
    {
        return inner.isEmpty();
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

    public static Codec<AnalyzedFilteringIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("inner").forGetter(i -> i.inner)
    ).apply(instance, AnalyzedFilteringIngredient::new));

    public static Codec<AnalyzedFilteringIngredient> NON_EMPTY_CODEC= RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("inner").forGetter(i -> i.inner)
    ).apply(instance, AnalyzedFilteringIngredient::new));;
}

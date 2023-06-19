package dev.gigaherz.elementsofpower.gemstones;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

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
    public JsonElement toJson()
    {
        JsonObject object = new JsonObject();
        object.addProperty("type", ID.toString());
        object.add("inner", inner.toJson());
        return object;
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
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<AnalyzedFilteringIngredient>
    {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public AnalyzedFilteringIngredient parse(JsonObject json)
        {
            Ingredient inner = CraftingHelper.getIngredient(GsonHelper.getAsJsonObject(json, "inner"), false);
            return new AnalyzedFilteringIngredient(inner);
        }

        @Override
        public AnalyzedFilteringIngredient parse(FriendlyByteBuf buffer)
        {
            Ingredient inner = Ingredient.fromNetwork(buffer);
            return new AnalyzedFilteringIngredient(inner);
        }

        @Override
        public void write(FriendlyByteBuf buffer, AnalyzedFilteringIngredient ingredient)
        {
            CraftingHelper.write(buffer, ingredient.inner);
        }
    }
}

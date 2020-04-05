package gigaherz.elementsofpower.gemstones;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
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
    public ItemStack[] getMatchingStacks()
    {
        return inner.getMatchingStacks();
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
    public IntList getValidItemStacksPacked()
    {
        return inner.getValidItemStacksPacked();
    }

    @Override
    public JsonElement serialize()
    {
        JsonObject object = new JsonObject();
        object.addProperty("type", ID.toString());
        object.add("inner", inner.serialize());
        return object;
    }

    @Override
    public boolean hasNoMatchingItems()
    {
        return inner.hasNoMatchingItems();
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
            Ingredient inner = CraftingHelper.getIngredient(JSONUtils.getJsonObject(json, "inner"));
            return new AnalyzedFilteringIngredient(inner);
        }

        @Override
        public AnalyzedFilteringIngredient parse(PacketBuffer buffer)
        {
            Ingredient inner = Ingredient.read(buffer);
            return new AnalyzedFilteringIngredient(inner);
        }

        @Override
        public void write(PacketBuffer buffer, AnalyzedFilteringIngredient ingredient)
        {
            CraftingHelper.write(buffer, ingredient.inner);
        }
    }
}

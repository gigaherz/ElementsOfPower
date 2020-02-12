package gigaherz.elementsofpower.recipes;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class GemstoneChangeRecipeFactory extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<GemstoneChangeRecipe>
{
    public static final GemstoneChangeRecipeFactory INSTANCE = new GemstoneChangeRecipeFactory();

    @Override
    public GemstoneChangeRecipe read(ResourceLocation recipeId, JsonObject json)
    {
        return new GemstoneChangeRecipe(recipeId);
    }

    @Nullable
    @Override
    public GemstoneChangeRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
    {
        return new GemstoneChangeRecipe(recipeId);
    }

    @Override
    public void write(PacketBuffer buffer, GemstoneChangeRecipe recipe)
    {

    }
}

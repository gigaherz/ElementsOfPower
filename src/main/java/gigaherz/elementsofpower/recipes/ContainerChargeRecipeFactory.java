package gigaherz.elementsofpower.recipes;

import com.google.gson.JsonObject;;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class ContainerChargeRecipeFactory extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ContainerChargeRecipe>
{
    public static final ContainerChargeRecipeFactory INSTANCE = new ContainerChargeRecipeFactory();

    @Override
    public ContainerChargeRecipe read(ResourceLocation recipeId, JsonObject json)
    {
        return new ContainerChargeRecipe(recipeId);
    }

    @Nullable
    @Override
    public ContainerChargeRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
    {
        return new ContainerChargeRecipe(recipeId);
    }

    @Override
    public void write(PacketBuffer buffer, ContainerChargeRecipe recipe)
    {
    }
}


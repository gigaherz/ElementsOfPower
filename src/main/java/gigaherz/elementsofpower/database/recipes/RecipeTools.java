package gigaherz.elementsofpower.database.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.ConversionCache;
import gigaherz.elementsofpower.database.InternalConversionProcess;
import gigaherz.elementsofpower.database.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

public class RecipeTools
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<RecipeEnumerator> recipeEnumerators = Lists.newArrayList();

    static
    {
        recipeEnumerators.add(new RecipeEnumerator.Crafting());
    }

    public static List<IRecipeInfoProvider> getAllRecipes(MinecraftServer server)
    {
        List<IRecipeInfoProvider> list = Lists.newArrayList();
        for (RecipeEnumerator re : recipeEnumerators)
        {
            re.enumerate(server, list::add);
        }
        return list;
    }
}
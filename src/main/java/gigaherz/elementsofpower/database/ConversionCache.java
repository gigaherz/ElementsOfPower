package gigaherz.elementsofpower.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.world.World;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConversionCache implements IConversionCache
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static IConversionCache get(@Nullable World world)
    {
        return conversionGetter.apply(world);
    }

    private static ConversionCache DUMMY = new ConversionCache();
    public static Function<World, IConversionCache> conversionGetter = w -> DUMMY;

    public static void dumpItemsWithoutEssences(World world)
    {
        IConversionCache cache = get(world);
        List<String> lines = Lists.newArrayList();
        ForgeRegistries.ITEMS.getValues().stream().sorted((a, b) ->
                String.CASE_INSENSITIVE_ORDER.compare(a.getRegistryName().toString(), b.getRegistryName().toString()))
                .forEach(item -> {
                    if (!cache.hasEssences(item))
                    {
                        if (!(item instanceof SpawnEggItem))
                            lines.add(item.getRegistryName().toString());
                    }
                });
        try
        {
            Path folder = FMLPaths.GAMEDIR.get().resolve("logs/elementsofpower");
            folder.toFile().mkdirs();
            Files.write(folder.resolve(new SimpleDateFormat("yyyyMMddHHmmss'.log'").format(new Date())), lines);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void dumpEssences(World world)
    {
        IConversionCache cache = get(world);
        List<String> found = Lists.newArrayList();
        List<String> missing = Lists.newArrayList();
        ForgeRegistries.ITEMS.getValues().stream().sorted((a, b) ->
                String.CASE_INSENSITIVE_ORDER.compare(a.getRegistryName().toString(), b.getRegistryName().toString()))
                .forEach(item -> {
                    if (cache.hasEssences(item))
                    {
                        MagicAmounts ma = cache.getEssences(item);
                        found.add("\t" + item.getRegistryName().toString() + " = " + ma);
                    }
                    else
                    {
                        missing.add("\t" + item.getRegistryName().toString());
                    }
                });
        List<String> lines = Lists.newArrayList();
        lines.add("Found:");
        lines.addAll(found);
        lines.add("Missing:");
        lines.addAll(missing);
        try
        {
            Path folder = FMLPaths.GAMEDIR.get().resolve("logs/elementsofpower");
            folder.toFile().mkdirs();
            Files.write(folder.resolve(new SimpleDateFormat("yyyyMMddHHmmss'.log'").format(new Date())), lines);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private final Map<Item, MagicAmounts> essenceMappings = Maps.newHashMap();

    public Map<Item, MagicAmounts> getEssenceMappings()
    {
        return essenceMappings;
    }

    @Override
    public boolean hasEssences(ItemStack stack)
    {
        return essenceMappings.containsKey(stack.getItem());
    }

    @Override
    public MagicAmounts getEssences(ItemStack stack, boolean wholeStack)
    {
        int count = stack.getCount();
        if (count > 1)
        {
            stack = stack.copy();
            stack.setCount(1);
        }

        MagicAmounts m = getEssences(stack.getItem());

        if (count > 1 && wholeStack)
        {
            m = m.multiply(count);
        }

        return m;
    }

    public MagicAmounts getEssences(Item stack)
    {
        return essenceMappings.getOrDefault(stack, MagicAmounts.EMPTY);
    }

    public MagicAmounts computeIfAbsent(Item stack, Function<Item, MagicAmounts> compute)
    {
        return essenceMappings.computeIfAbsent(stack.getItem(), compute);
    }

    public void addConversion(Item item, MagicAmounts amounts)
    {
        if (item == Items.AIR)
        {
            ElementsOfPowerMod.LOGGER.error("Attempted to insert amounts for AIR!");
            return;
        }

        /*if (essenceMappings.containsKey(item))
        {
            ElementsOfPowerMod.LOGGER.error("Stack already inserted! " + item.toString());
            return;
        }*/

        essenceMappings.put(item, amounts);
    }

    public void putAll(Map<Item, MagicAmounts> data)
    {
        data.forEach(this::addConversion);
    }

    public Map<Item, MagicAmounts> getAllConversions()
    {
        return Collections.unmodifiableMap(essenceMappings);
    }

    public void clear()
    {
        essenceMappings.clear();
    }
}

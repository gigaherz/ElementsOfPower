package gigaherz.elementsofpower.database;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import gigaherz.elementsofpower.network.SyncEssenceConversions;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class EssenceConversions
{
    public static final Supplier<Path> OVERRIDES_PATH = () -> FMLPaths.CONFIGDIR.get().resolve("elementsofpower-essence_overrides.json");
    public static final Supplier<Path> CACHE_PATH = () -> FMLPaths.CONFIGDIR.get().resolve("elementsofpower-essence_cache.json");

    private static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(MagicAmounts.class, new MagicAmounts.Serializer()).create();

    public static final EssenceConversions CLIENT = new EssenceConversions();
    public static final EssenceConversions SERVER = new EssenceConversions();

    public static EssenceConversions get(@Nullable World world)
    {
        return (world != null && world.isRemote) ? CLIENT : SERVER;
    }

    private final Map<Item, MagicAmounts> essenceMappings = Maps.newHashMap();

    public boolean itemHasEssence(Item item)
    {
        return essenceMappings.containsKey(item);
    }

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

    public void addConversion(Item item, MagicAmounts amounts)
    {
        if (essenceMappings.containsKey(item))
        {
            ElementsOfPowerMod.LOGGER.error("Stack already inserted! " + item.toString());
            return;
        }

        essenceMappings.put(item, amounts);
    }

    public Map<Item, MagicAmounts> getAllConversions()
    {
        return Collections.unmodifiableMap(essenceMappings);
    }

    public void clear()
    {
        essenceMappings.clear();
    }

    public void receiveFromServer(Map<Item, MagicAmounts> data)
    {
        essenceMappings.clear();
        essenceMappings.putAll(data);
    }

    public static void registerEssencesForRecipes()
    {
        Map<Item, RecipeTools.ItemSource> itemSources = RecipeTools.gatherRecipes();

        for (Map.Entry<Item, RecipeTools.ItemSource> it : itemSources.entrySet())
        {
            Item output = it.getKey();
            RecipeTools.ItemSource inputs = it.getValue();

            float count = inputs.numProduced;
            if (count < 1)
            {
                ElementsOfPowerMod.LOGGER.warn("StackSize is invalid! " + output.toString());
                continue;
            }

            if (SERVER.itemHasEssence(output))
                continue;

            boolean allFound = true;
            MagicAmounts am = MagicAmounts.EMPTY;
            for (ItemStack b : inputs.sources)
            {
                MagicAmounts m = SERVER.getEssences(b, true);

                if (m.isEmpty())
                {
                    allFound = false;
                    break;
                }

                am = am.add(m);
            }

            if (!allFound)
                continue;

            if (count > 1)
            {
                am = am.multiply(1.0f / count);
            }

            SERVER.addConversion(output.getItem(), am);
        }
    }

    public static void loadOverrides()
    {
        Path path = OVERRIDES_PATH.get();
        if (Files.exists(path))
        {
            try
            {
                Map<String, MagicAmounts> ovr = loadConfig(OVERRIDES_PATH.get());
                if (ovr != null)
                {
                    applyConversions(ovr);
                }
            }
            catch (FileNotFoundException e)
            {
                ElementsOfPowerMod.LOGGER.warn("Unexpected error", e);
            }
        }
    }

    @Nullable
    public static Map<String, MagicAmounts> loadConfig(Path path) throws FileNotFoundException
    {
        Reader r = new FileReader(path.toFile());
        Type type = new TypeToken<Map<String, MagicAmounts>>()
        {
        }.getType();

        return SERIALIZER.fromJson(r, type);
    }

    public static void applyConversions(Map<String, MagicAmounts> map)
    {
        for (Map.Entry<String, MagicAmounts> e : map.entrySet())
        {
            String itemName = e.getKey();

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item != null)
            {
                MagicAmounts m = e.getValue();
                EssenceConversions.SERVER.addConversion(item, m);
            }
        }
    }

    public static void saveConversions(Path path, Map<Item, MagicAmounts> map)
    {
        try
        {
            Map<String, MagicAmounts> am = Maps.newHashMap();
            for (Map.Entry<Item, MagicAmounts> entry : map.entrySet())
            {
                am.put(entry.getKey().getRegistryName().toString(), entry.getValue());
            }
            Writer w = new FileWriter(path.toFile());
            w.write(SERIALIZER.toJson(am));
            w.flush();
            w.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void recalculateConversions()
    {
        ElementsOfPowerMod.LOGGER.info("Recalculating essence conversion table...");
        Stopwatch sw = Stopwatch.createStarted();
        EssenceConversions.SERVER.clear();
        StockConversions.addStockConversions();
        EssenceConversions.loadOverrides();
        EssenceConversions.registerEssencesForRecipes();
        EssenceConversions.saveConversions(CACHE_PATH.get(), EssenceConversions.SERVER.getAllConversions());
        sw.stop();
        ElementsOfPowerMod.LOGGER.info("Done. Time elapsed: {}", sw);
    }

    public static void invalidateCache()
    {
        Path path = CACHE_PATH.get();
        if (Files.exists(path))
        {
            try
            {
                Files.delete(path);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void registerSubcommands(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder
        .then(Commands.literal("invalidate")
                .requires(cs -> cs.hasPermissionLevel(4)) //permission
                .executes(ctx -> {
                            invalidateCache();
                            ctx.getSource().sendFeedback(new StringTextComponent("Cache invalidated. Will recalculate conversions on next restart"), true);
                            return 0;
                        }
                )
        )
        .then(Commands.literal("recalculate")
                .requires(cs -> cs.hasPermissionLevel(4)) //permission
                .executes(ctx -> {
                            recalculateConversions();
                            ctx.getSource().sendFeedback(new StringTextComponent("Conversions recalculated."), true);
                            return 0;
                        }
                )
        );
    }

    public static void registerResourceReloadListener(IReloadableResourceManager serverResourceManager)
    {
        serverResourceManager.addReloadListener(
                new ReloadListener<Map<String, MagicAmounts>>()
                {
                    @Nullable
                    @Override
                    protected Map<String, MagicAmounts> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn)
                    {
                        Path path = CACHE_PATH.get();
                        if (Files.exists(path))
                        {
                            try
                            {
                                return loadConfig(path);
                            }
                            catch (Exception e)
                            {
                                ElementsOfPowerMod.LOGGER.warn("Error loading conversion cache. Recalculation required", e);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void apply(@Nullable Map<String, MagicAmounts> conversions, IResourceManager resourceManagerIn, IProfiler profilerIn)
                    {
                        if (conversions != null)
                        {
                            ElementsOfPowerMod.LOGGER.info("Cache found and loaded. Applying...");
                            SERVER.clear();
                            applyConversions(conversions);
                        }
                        else
                        {
                            recalculateConversions();
                        }
                        ElementsOfPowerMod.channel.send(PacketDistributor.ALL.with(null), new SyncEssenceConversions());
                    }
                }
        );
    }
}

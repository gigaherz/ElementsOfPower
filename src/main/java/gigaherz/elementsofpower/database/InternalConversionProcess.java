package gigaherz.elementsofpower.database;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.graph.ItemGraph;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import gigaherz.elementsofpower.magic.MagicAmounts;
import gigaherz.elementsofpower.network.SyncEssenceConversions;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InternalConversionProcess
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static final Supplier<Path> OVERRIDES_PATH = () -> FMLPaths.CONFIGDIR.get().resolve("elementsofpower-essence_overrides.json");
    private static final Supplier<Path> CACHE_PATH = () -> FMLPaths.CONFIGDIR.get().resolve("elementsofpower-essence_cache.json");

    private static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(MagicAmounts.class, new MagicAmounts.Serializer())
            .create();

    public static final InternalConversionCache CLIENT = new InternalConversionCache();
    public static final InternalConversionCache SERVER = new InternalConversionCache();

    public static IConversionCache get(@Nullable World world)
    {
        return (world != null && world.isRemote) ? CLIENT : SERVER;
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(InternalConversionProcess::playerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(InternalConversionProcess::addReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(InternalConversionProcess::serverStarted);
        ConversionCache.conversionGetter = InternalConversionProcess::get;
    }

    private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        PlayerEntity player = event.getPlayer();
        if (player.isServerWorld())
            ElementsOfPowerMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SyncEssenceConversions());
    }

    private static void addReloadListeners(AddReloadListenerEvent event)
    {
        registerResourceReloadListener(event::addListener);
    }

    private static void serverStarted(FMLServerStartedEvent event)
    {
        if (!SERVER.isReady())
        {
            recalculateConversions(event.getServer(), (ok,msg)->{});
        }
    }

    private static void registerEssencesForRecipesNew(MinecraftServer server, Consumer<Throwable> doneCallback)
    {
        List<IRecipeInfoProvider> recipes = RecipeTools.getAllRecipes(server);
        (new Thread(() -> {
            try
            {
                ItemGraph<MagicAmounts> graph = new ItemGraph<>();
                for (IRecipeInfoProvider p : recipes)
                {
                    ItemStack recipeOutput = p.getRecipeOutput();
                    Item consumer = recipeOutput.getItem();
                    double scale = recipeOutput.getCount();

                    Map<Item, Double> providers = Maps.newHashMap();
                    for (ItemStack t : p.getRecipeInputs())
                    {
                        providers.compute(t.getItem(), (i,v) -> (v != null ? v : 0)+t.getCount());
                    }

                    graph.addEdgeBundle(consumer, scale, providers.entrySet());
                }

                Map<Item, MagicAmounts> existing = SERVER.getAllConversions();

                for(Map.Entry<Item, MagicAmounts> entry : existing.entrySet())
                {
                    graph.floodFill(entry.getKey(), entry.getValue(), MagicAmounts::multiply, MagicAmounts::add, (old, cur) -> {
                        return old.getTotalMagic() > cur.getTotalMagic();
                    });
                }
                List<ItemGraph.Neuron<MagicAmounts>> l = graph.getValues().filter(v -> v.fillData != null).collect(Collectors.toList());
                graph.finishFill(SERVER::addConversion);

                doneCallback.accept(null);
            }
            catch(Exception e)
            {
                server.execute(() -> doneCallback.accept(e));
            }
        })).start();
    }

    private static void registerEssencesForRecipes(MinecraftServer server, Consumer<Throwable> doneCallback)
    {
        List<IRecipeInfoProvider> recipes = RecipeTools.getAllRecipes(server);

        (new Thread(() -> {
            try
            {
                RecipeTools.Processor p = new RecipeTools.Processor();

                for(IRecipeInfoProvider recipe : recipes)
                {
                    p.processRecipe(recipe);
                }

                server.execute(() -> {
                    for (Map.Entry<Item, RecipeTools.ItemSource> it : p.itemSources.entrySet())
                    {
                        Item output = it.getKey();
                        RecipeTools.ItemSource inputs = it.getValue();

                        float count = inputs.numProduced;
                        if (count < 1)
                        {
                            ElementsOfPowerMod.LOGGER.warn("StackSize is invalid! " + output.toString());
                            continue;
                        }

                        if (SERVER.hasEssences(new ItemStack(output)))
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
                    doneCallback.accept(null);
                });
            }
            catch(Exception e)
            {
                server.execute(() -> doneCallback.accept(e));
            }
        })).start();
    }

    private static void loadOverrides()
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
            catch (IOException e)
            {
                ElementsOfPowerMod.LOGGER.warn("Unexpected error", e);
            }
        }
    }

    @Nullable
    private static Map<String, MagicAmounts> loadConfig(Path path) throws IOException
    {
        try (Reader r = new FileReader(path.toFile()))
        {
            Type type = new TypeToken<Map<String, MagicAmounts>>()
            {
            }.getType();

            return SERIALIZER.fromJson(r, type);
        }
    }

    private static void applyConversions(Map<String, MagicAmounts> map)
    {
        for (Map.Entry<String, MagicAmounts> e : map.entrySet())
        {
            String itemName = e.getKey();

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item != null && item != Items.AIR)
            {
                MagicAmounts m = e.getValue();
                SERVER.addConversion(item, m);
            }
        }
    }

    private static void saveConversions(Path path, Map<Item, MagicAmounts> map)
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

    private static void recalculateConversionsNew(MinecraftServer server, BiConsumer<Boolean, String> onDone)
    {
        try
        {
            ElementsOfPowerMod.LOGGER.info("Recalculating essence conversion table...");
            Stopwatch sw = Stopwatch.createStarted();
            SERVER.clear();
            StockConversions.addStockConversions((rl, items) -> {
                ITag<Item> tag = server.func_244266_aF().getItemTags().get(rl);
                return tag == null ? items : tag.getAllElements();
            }, SERVER::addConversion);
            loadOverrides();
            registerEssencesForRecipesNew(server, throwable -> {
                if (throwable != null)
                {
                    throwable.printStackTrace();
                    onDone.accept(false, throwable.getMessage());
                }
                else
                {
                    //saveConversions(CACHE_PATH.get(), SERVER.getAllConversions());
                    sw.stop();
                    ElementsOfPowerMod.LOGGER.info("Done. Time elapsed: {}", sw);
                    if (ServerLifecycleHooks.getCurrentServer() != null)
                        ElementsOfPowerMod.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncEssenceConversions());
                    onDone.accept(true, String.format("Done. Time elapsed: %s", sw));
                }
            });
        }
        catch (Exception e)
        {
            ElementsOfPowerMod.LOGGER.error("Error. Essence conversion calculations failed", e);
        }
    }

    private static void recalculateConversions(MinecraftServer server, BiConsumer<Boolean, String> onDone)
    {
        try
        {
            ElementsOfPowerMod.LOGGER.info("Recalculating essence conversion table...");
            Stopwatch sw = Stopwatch.createStarted();
            SERVER.clear();
            StockConversions.addStockConversions((rl, items) -> {
                ITag<Item> tag = server.func_244266_aF().getItemTags().get(rl);
                return tag == null ? items : tag.getAllElements();
            }, SERVER::addConversion);
            loadOverrides();
            registerEssencesForRecipes(server, throwable -> {
                if (throwable != null)
                {
                    throwable.printStackTrace();
                    onDone.accept(false, throwable.getMessage());
                }
                else
                {
                    saveConversions(CACHE_PATH.get(), SERVER.getAllConversions());
                    sw.stop();
                    ElementsOfPowerMod.LOGGER.info("Done. Time elapsed: {}", sw);
                    if (ServerLifecycleHooks.getCurrentServer() != null)
                        ElementsOfPowerMod.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncEssenceConversions());
                    onDone.accept(true, String.format("Done. Time elapsed: %s", sw));
                }
            });
        }
        catch (Exception e)
        {
            ElementsOfPowerMod.LOGGER.error("Error. Essence conversion calculations failed", e);
        }
    }

    private static void invalidateCache()
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

    public static LiteralArgumentBuilder<CommandSource> registerSubcommands(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        return argumentBuilder
                .then(Commands.literal("cache")
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
                                            recalculateConversions(ctx.getSource().getServer(), (ok, msg) -> {
                                                if (ok)
                                                    ctx.getSource().sendFeedback(new StringTextComponent("Finished: " + msg), true);
                                                else
                                                    ctx.getSource().sendFeedback(new StringTextComponent("Error calculating: " + msg), true);
                                            });
                                            return 0;
                                        }
                                )
                        )
                        .then(Commands.literal("recalculate_new")
                                .requires(cs -> cs.hasPermissionLevel(4)) //permission
                                .executes(ctx -> {
                                            recalculateConversionsNew(ctx.getSource().getServer(), (ok, msg) -> {
                                                if (ok)
                                                    ctx.getSource().sendFeedback(new StringTextComponent("Finished: " + msg), true);
                                                else
                                                    ctx.getSource().sendFeedback(new StringTextComponent("Error calculating: " + msg), true);
                                            });
                                            return 0;
                                        }
                                )
                        )
                );
    }

    private static void registerResourceReloadListener(Consumer<IFutureReloadListener> listenerAdd)
    {
        listenerAdd.accept(
                new ReloadListener<Either<Map<String, MagicAmounts>, Unit>>()
                {
                    @Override
                    protected Either<Map<String, MagicAmounts>, Unit> prepare(IResourceManager resourceManagerIn, IProfiler profilerIn)
                    {
                        Path path = CACHE_PATH.get();
                        if (Files.exists(path))
                        {
                            try
                            {
                                return Either.left(loadConfig(path));
                            }
                            catch (Exception e)
                            {
                                ElementsOfPowerMod.LOGGER.warn("Error loading conversion cache. Recalculation required", e);
                            }
                        }
                        return Either.right(Unit.INSTANCE);
                    }

                    @Override
                    protected void apply(@Nonnull Either<Map<String, MagicAmounts>, Unit> data, IResourceManager resourceManagerIn, IProfiler profilerIn)
                    {
                        SERVER.clear();
                        data.ifLeft(conversions -> {
                            ElementsOfPowerMod.LOGGER.info("Cache found and loaded. Applying...");
                            applyConversions(conversions);
                            SERVER.markReady();
                        }).ifRight(unit -> {
                            if (ServerLifecycleHooks.getCurrentServer() != null)
                            {
                                recalculateConversions(ServerLifecycleHooks.getCurrentServer(), (ok,msg)->{});
                                SERVER.markReady();
                            }
                        });
                        if (ServerLifecycleHooks.getCurrentServer() != null)
                        {
                            ElementsOfPowerMod.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncEssenceConversions());
                        }
                    }
                }
        );
    }

    public static class InternalConversionCache extends ConversionCache
    {
        private boolean isReady = false;

        public boolean isReady()
        {
            return isReady;
        }

        @Override
        public void clear()
        {
            super.clear();
            isReady = false;
        }

        public void markReady()
        {
            isReady = true;
        }

        public void receiveFromServer(Map<Item, MagicAmounts> data)
        {
            clear();
            putAll(data);
            markReady();
        }
    }
}

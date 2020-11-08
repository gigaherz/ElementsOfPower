package gigaherz.elementsofpower.database;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import gigaherz.elementsofpower.ConfigManager;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.database.graph.ItemGraph;
import gigaherz.elementsofpower.database.recipes.IRecipeInfoProvider;
import gigaherz.elementsofpower.database.recipes.RecipeTools;
import gigaherz.elementsofpower.database.recipes.ScaledIngredient;
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
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
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
import java.util.concurrent.CompletableFuture;
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
    }

    private static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (ElementsOfPowerMod.isInternalRecipeScannerEnabled())
        {
            if (SERVER.isReady())
            {
                PlayerEntity player = event.getPlayer();
                if (player.isServerWorld())
                    ElementsOfPowerMod.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new SyncEssenceConversions());
            }
        }
    }

    private static void addReloadListeners(AddReloadListenerEvent event)
    {
        if (ElementsOfPowerMod.isInternalRecipeScannerEnabled())
        {
            registerResourceReloadListener(event::addListener);
        }
    }

    private static void serverStarted(FMLServerStartedEvent event)
    {
        if (ElementsOfPowerMod.isInternalRecipeScannerEnabled())
        {
            if (!SERVER.isReady())
            {
                recalculateConversions(event.getServer());
            }
        }
    }

    private static Map<Item, MagicAmounts> processRecipesNew(Map<Item, MagicAmounts> stock, List<IRecipeInfoProvider> recipes)
    {
        Map<Ingredient, JsonElement> serializations = Maps.newHashMap();
        Map<JsonElement, Ingredient> references = Maps.newHashMap();

        if (ConfigManager.COMMON.verboseDebug.get())
            LOGGER.debug("Constructing graph...");
        ItemGraph<MagicAmounts> graph = new ItemGraph<>(MagicAmounts::multiply, MagicAmounts::add, MagicAmounts::compare, MagicAmounts::min, MagicAmounts::isNullOrEmpty, MagicAmounts.EMPTY);
        for (IRecipeInfoProvider p : recipes)
        {
            ItemStack recipeOutput = p.getRecipeOutput();

            Map<Ingredient, Double> providers = Maps.newHashMap();
            for (ScaledIngredient t : p.getRecipeInputs())
            {
                JsonElement serialization = serializations.computeIfAbsent(t.ingredient, Ingredient::serialize);
                Ingredient deduplicated = references.computeIfAbsent(serialization, json -> t.ingredient);
                providers.compute(deduplicated, (i,v) -> v != null ? v+t.scale : t.scale);
            }

            graph.addRecipe(recipeOutput, providers.entrySet().stream().map(e -> new ScaledIngredient(e.getKey(), e.getValue())).collect(Collectors.toList()), p);
        }

        if (ConfigManager.COMMON.verboseDebug.get())
            LOGGER.debug("Adding recipe information to graph...");
        stock.forEach(graph::addData);

        if (ConfigManager.COMMON.verboseDebug.get())
            LOGGER.debug("Spreading unambiguous information...");
        graph.spread();

        if (ConfigManager.COMMON.verboseDebug.get())
            LOGGER.debug("Calculating final values...");

        graph.computeFinalValues(stock::put);

        if (ConfigManager.COMMON.verboseDebug.get())
            LOGGER.debug("Done.");

        return stock;
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
        SERVER.markReady();
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

    private static CompletableFuture<String> recalculateConversions(MinecraftServer server)
    {
        ElementsOfPowerMod.LOGGER.info("Recalculating essence conversion table...");
        Stopwatch sw = Stopwatch.createStarted();
        SERVER.clear();

        Map<Item, MagicAmounts> stock = Maps.newHashMap();
        StockConversions.addStockConversions((rl, items) -> {
            ITag<Item> tag = server.func_244266_aF().getItemTags().get(rl);
            return tag == null ? items : tag.getAllElements();
        }, stock::put);

        loadOverrides();
        List<IRecipeInfoProvider> recipes = RecipeTools.getAllRecipes(server);
        return CompletableFuture.supplyAsync(() -> InternalConversionProcess.processRecipesNew(stock, recipes))
        .thenApply(value -> {
            SERVER.setEssenceMappings(value);
            saveConversions(CACHE_PATH.get(), SERVER.getAllConversions());
            sw.stop();
            ElementsOfPowerMod.LOGGER.info("Done. Time elapsed: {}", sw);
            if (ServerLifecycleHooks.getCurrentServer() != null)
                ElementsOfPowerMod.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncEssenceConversions());
            return String.format("Done. Time elapsed: %s", sw);
        }).whenComplete((value,throwable) -> {
            if (throwable != null) {
                ElementsOfPowerMod.LOGGER.error("Error. Essence conversion calculations failed", throwable);
            }
            SERVER.markReady();
        });
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
                        .requires(cs -> ElementsOfPowerMod.isInternalRecipeScannerEnabled())
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
                                            recalculateConversions(ctx.getSource().getServer())
                                            .whenComplete((msg, err) -> ctx.getSource()
                                                    .sendFeedback(new StringTextComponent((msg != null ? "Finished: " + msg :"Error calculating: " + err)), true));
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
                        if (!"true".equals(System.getProperty("elementsOfPower.disableCaches", "false")))
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
                        }).ifRight(unit -> {
                            if (ServerLifecycleHooks.getCurrentServer() != null)
                            {
                                recalculateConversions(ServerLifecycleHooks.getCurrentServer());
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

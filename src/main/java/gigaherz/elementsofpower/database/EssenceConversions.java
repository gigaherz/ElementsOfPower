package gigaherz.elementsofpower.database;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Either;
import gigaherz.elementsofpower.ElementsOfPowerMod;
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
import net.minecraft.item.SpawnEggItem;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
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
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EssenceConversions
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static final Supplier<Path> OVERRIDES_PATH = () -> FMLPaths.CONFIGDIR.get().resolve("elementsofpower-essence_overrides.json");
    private static final Supplier<Path> CACHE_PATH = () -> FMLPaths.CONFIGDIR.get().resolve("elementsofpower-essence_cache.json");

    private static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(MagicAmounts.class, new MagicAmounts.Serializer())
            .create();

    public static final EssenceConversions CLIENT = new EssenceConversions();
    public static final EssenceConversions SERVER = new EssenceConversions();

    public static EssenceConversions get(@Nullable World world)
    {
        return (world != null && world.isRemote) ? CLIENT : SERVER;
    }

    private final Map<Item, MagicAmounts> essenceMappings = Maps.newHashMap();
    private boolean isReady = false;

    public boolean isReady()
    {
        return isReady;
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(EssenceConversions::playerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(EssenceConversions::addReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(EssenceConversions::serverStarted);
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
            recalculateConversions(event.getServer());
        }
    }

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
        if (item == Items.AIR)
        {
            ElementsOfPowerMod.LOGGER.error("Attempted to insert amounts for AIR!");
            return;
        }

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
        isReady = false;
    }

    private void markReady()
    {
        isReady = true;
    }

    public void receiveFromServer(Map<Item, MagicAmounts> data)
    {
        essenceMappings.clear();
        essenceMappings.putAll(data);
    }

    private static void registerEssencesForRecipes()
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
                EssenceConversions.SERVER.addConversion(item, m);
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

    private static void recalculateConversions(MinecraftServer server)
    {
        try
        {
            ElementsOfPowerMod.LOGGER.info("Recalculating essence conversion table...");
            Stopwatch sw = Stopwatch.createStarted();
            EssenceConversions.SERVER.clear();
            StockConversions.addStockConversions(server);
            EssenceConversions.loadOverrides();
            EssenceConversions.registerEssencesForRecipes();
            EssenceConversions.saveConversions(CACHE_PATH.get(), EssenceConversions.SERVER.getAllConversions());
            if ("TRUE".equals(System.getProperty("elementsofpower.dumpItemsWithoutEssences")))
                dumpItemsWithoutEssences();
            sw.stop();
            ElementsOfPowerMod.LOGGER.info("Done. Time elapsed: {}", sw);
            if (ServerLifecycleHooks.getCurrentServer() != null)
                ElementsOfPowerMod.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncEssenceConversions());
        }
        catch(Exception e)
        {
            ElementsOfPowerMod.LOGGER.error("Error. Essence conversion calculations failed", e);
        }
    }

    private static void dumpItemsWithoutEssences()
    {
        ForgeRegistries.ITEMS.getValues().stream().sorted((a,b) ->
                    String.CASE_INSENSITIVE_ORDER.compare(a.getRegistryName().toString(), b.getRegistryName().toString()))
                .forEach(item -> {
            if (!SERVER.itemHasEssence(item))
            {
                if (!(item instanceof SpawnEggItem))
                    LOGGER.warn("Item is not assigned any essences: {}", item.getRegistryName());
            }
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
                                    recalculateConversions(ctx.getSource().getServer());
                                    ctx.getSource().sendFeedback(new StringTextComponent("Conversions recalculated."), true);
                                    return 0;
                                }
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
                                recalculateConversions(ServerLifecycleHooks.getCurrentServer());
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
}

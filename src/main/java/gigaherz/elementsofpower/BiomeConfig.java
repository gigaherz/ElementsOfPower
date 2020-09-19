package gigaherz.elementsofpower;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import gigaherz.elementsofpower.magic.MagicAmounts;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BiomeConfig
{
    public static final String BIOMELABELS_JSON = "elementsofpower.biomelabels.json";
    public static final Supplier<File> BIOMELABELS_JSON_FILE = () -> FMLPaths.CONFIGDIR.get().resolve(BIOMELABELS_JSON).toFile();

    private static final Multimap<ResourceLocation, String> biomeLabels = ArrayListMultimap.create();
    private static final Map<String, MagicAmounts> labelAmounts = new HashMap<>();

    public static void loadBiomeConfig()
    {
        if (!BIOMELABELS_JSON_FILE.get().exists())
        {
            registerVanillaLabels();
            saveLabels();
        }
        try(FileInputStream is = new FileInputStream(BIOMELABELS_JSON_FILE.get());
            InputStreamReader reader = new InputStreamReader(is))
        {
            JsonElement document = new JsonParser().parse(reader);
            JsonObject root = document.getAsJsonObject();
            JsonObject biomes = root.get("biomes").getAsJsonObject();
            for(Map.Entry<String, JsonElement> kv : biomes.entrySet())
            {
                String key = kv.getKey();
                JsonElement value = kv.getValue();
                if (value.isJsonPrimitive())
                {
                    biomeLabels.put(new ResourceLocation(key), value.getAsString());
                }
                else if (value.isJsonArray())
                {
                    JsonArray array = value.getAsJsonArray();
                    for(JsonElement e : array)
                    {
                        biomeLabels.put(new ResourceLocation(key), e.getAsString());
                    }
                }
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Error parsing " + BIOMELABELS_JSON, ex);
        }
    }

    private static void saveLabels()
    {
        JsonObject biomes = new JsonObject();
        for(ResourceLocation key : biomeLabels.keySet())
        {
            JsonArray labels = new JsonArray();
            for(String label : biomeLabels.get(key))
            {
                labels.add(label);
            }
            if (labels.size() != 1)
                biomes.add(key.toString(), labels);
            else
                biomes.addProperty(key.toString(), labels.get(0).getAsString());
        }
        JsonObject root = new JsonObject();
        root.add("biomes", biomes);
        Gson gson = new Gson();
        try (
                FileOutputStream out = new FileOutputStream(BIOMELABELS_JSON_FILE.get());
                OutputStreamWriter writer = new OutputStreamWriter(out);
        )
        {
            gson.toJson(root, writer);
        }
        catch(IOException ex)
        {
            throw new RuntimeException("Error writing to " + BIOMELABELS_JSON, ex);
        }
    }

    public static boolean hasType(ResourceLocation biome, String label)
    {
        return biomeLabels.containsEntry(biome, label);
    }

    private static void registerVanillaLabels()
    {
        putMany(biomeLabels, Biomes.OCEAN, "ocean", "overworld");
        putMany(biomeLabels, Biomes.PLAINS, "plains", "overworld");
        putMany(biomeLabels, Biomes.DESERT, "hot", "dry", "sandy", "overworld");
        putMany(biomeLabels, Biomes.MOUNTAINS, "mountain", "hills", "overworld");
        putMany(biomeLabels, Biomes.FOREST, "forest", "overworld");
        putMany(biomeLabels, Biomes.TAIGA, "cold", "coniferous", "forest", "overworld");
        putMany(biomeLabels, Biomes.SWAMP, "wet", "swamp", "overworld");
        putMany(biomeLabels, Biomes.RIVER, "river", "overworld");
        putMany(biomeLabels, Biomes.NETHER_WASTES, "hot", "dry", "nether");
        putMany(biomeLabels, Biomes.THE_END, "cold", "dry", "end");
        putMany(biomeLabels, Biomes.FROZEN_OCEAN, "cold", "ocean", "snowy", "overworld");
        putMany(biomeLabels, Biomes.FROZEN_RIVER, "cold", "river", "snowy", "overworld");
        putMany(biomeLabels, Biomes.SNOWY_TUNDRA, "cold", "snowy", "wasteland", "overworld");
        putMany(biomeLabels, Biomes.SNOWY_MOUNTAINS, "cold", "snowy", "mountain", "overworld");
        putMany(biomeLabels, Biomes.MUSHROOM_FIELDS, "mushroom", "rare", "overworld");
        putMany(biomeLabels, Biomes.MUSHROOM_FIELD_SHORE, "mushroom", "beach", "rare", "overworld");
        putMany(biomeLabels, Biomes.BEACH, "beach", "overworld");
        putMany(biomeLabels, Biomes.DESERT_HILLS, "hot", "dry", "sandy", "hills", "overworld");
        putMany(biomeLabels, Biomes.WOODED_HILLS, "forest", "hills", "overworld");
        putMany(biomeLabels, Biomes.TAIGA_HILLS, "cold", "coniferous", "forest", "hills", "overworld");
        putMany(biomeLabels, Biomes.MOUNTAIN_EDGE, "mountain", "overworld");
        putMany(biomeLabels, Biomes.JUNGLE, "hot", "wet", "dense", "jungle", "overworld");
        putMany(biomeLabels, Biomes.JUNGLE_HILLS, "hot", "wet", "dense", "jungle", "hills", "overworld");
        putMany(biomeLabels, Biomes.JUNGLE_EDGE, "hot", "wet", "jungle", "forest", "rare", "overworld");
        putMany(biomeLabels, Biomes.DEEP_OCEAN, "ocean", "overworld");
        putMany(biomeLabels, Biomes.STONE_SHORE, "beach", "overworld");
        putMany(biomeLabels, Biomes.SNOWY_BEACH, "cold", "beach", "snowy", "overworld");
        putMany(biomeLabels, Biomes.BIRCH_FOREST, "forest", "overworld");
        putMany(biomeLabels, Biomes.BIRCH_FOREST_HILLS, "forest", "hills", "overworld");
        putMany(biomeLabels, Biomes.DARK_FOREST, "spooky", "dense", "forest", "overworld");
        putMany(biomeLabels, Biomes.SNOWY_TAIGA, "cold", "coniferous", "forest", "snowy", "overworld");
        putMany(biomeLabels, Biomes.SNOWY_TAIGA_HILLS, "cold", "coniferous", "forest", "snowy", "hills", "overworld");
        putMany(biomeLabels, Biomes.GIANT_TREE_TAIGA, "cold", "coniferous", "forest", "overworld");
        putMany(biomeLabels, Biomes.GIANT_TREE_TAIGA_HILLS, "cold", "coniferous", "forest", "hills", "overworld");
        putMany(biomeLabels, Biomes.WOODED_MOUNTAINS, "mountain", "forest", "sparse", "overworld");
        putMany(biomeLabels, Biomes.SAVANNA, "hot", "savanna", "plains", "sparse", "overworld");
        putMany(biomeLabels, Biomes.SAVANNA_PLATEAU, "hot", "savanna", "plains", "sparse", "rare", "overworld", "plateau");
        putMany(biomeLabels, Biomes.BADLANDS, "mesa", "sandy", "dry", "overworld");
        putMany(biomeLabels, Biomes.WOODED_BADLANDS_PLATEAU, "mesa", "sandy", "dry", "sparse", "overworld", "plateau");
        putMany(biomeLabels, Biomes.BADLANDS_PLATEAU, "mesa", "sandy", "dry", "overworld", "plateau");
        putMany(biomeLabels, Biomes.SMALL_END_ISLANDS, "end");
        putMany(biomeLabels, Biomes.END_MIDLANDS, "end");
        putMany(biomeLabels, Biomes.END_HIGHLANDS, "end");
        putMany(biomeLabels, Biomes.END_BARRENS, "end");
        putMany(biomeLabels, Biomes.WARM_OCEAN, "ocean", "hot", "overworld");
        putMany(biomeLabels, Biomes.LUKEWARM_OCEAN, "ocean", "overworld");
        putMany(biomeLabels, Biomes.COLD_OCEAN, "ocean", "cold", "overworld");
        putMany(biomeLabels, Biomes.DEEP_WARM_OCEAN, "ocean", "hot", "overworld");
        putMany(biomeLabels, Biomes.DEEP_LUKEWARM_OCEAN, "ocean", "overworld");
        putMany(biomeLabels, Biomes.DEEP_COLD_OCEAN, "ocean", "cold", "overworld");
        putMany(biomeLabels, Biomes.DEEP_FROZEN_OCEAN, "ocean", "cold", "overworld");
        putMany(biomeLabels, Biomes.THE_VOID, "void");
        putMany(biomeLabels, Biomes.SUNFLOWER_PLAINS, "plains", "rare", "overworld");
        putMany(biomeLabels, Biomes.DESERT_LAKES, "hot", "dry", "sandy", "rare", "overworld");
        putMany(biomeLabels, Biomes.GRAVELLY_MOUNTAINS, "mountain", "sparse", "rare", "overworld");
        putMany(biomeLabels, Biomes.FLOWER_FOREST, "forest", "hills", "rare", "overworld");
        putMany(biomeLabels, Biomes.TAIGA_MOUNTAINS, "cold", "coniferous", "forest", "mountain", "rare", "overworld");
        putMany(biomeLabels, Biomes.SWAMP_HILLS, "wet", "swamp", "hills", "rare", "overworld");
        putMany(biomeLabels, Biomes.ICE_SPIKES, "cold", "snowy", "hills", "rare", "overworld");
        putMany(biomeLabels, Biomes.MODIFIED_JUNGLE, "hot", "wet", "dense", "jungle", "mountain", "rare", "overworld", "modified");
        putMany(biomeLabels, Biomes.MODIFIED_JUNGLE_EDGE, "hot", "sparse", "jungle", "hills", "rare", "overworld", "modified");
        putMany(biomeLabels, Biomes.TALL_BIRCH_FOREST, "forest", "dense", "hills", "rare", "overworld");
        putMany(biomeLabels, Biomes.TALL_BIRCH_HILLS, "forest", "dense", "mountain", "rare", "overworld");
        putMany(biomeLabels, Biomes.DARK_FOREST_HILLS, "spooky", "dense", "forest", "mountain", "rare", "overworld");
        putMany(biomeLabels, Biomes.SNOWY_TAIGA_MOUNTAINS, "cold", "coniferous", "forest", "snowy", "mountain", "rare", "overworld");
        putMany(biomeLabels, Biomes.GIANT_SPRUCE_TAIGA, "dense", "forest", "rare", "overworld");
        putMany(biomeLabels, Biomes.GIANT_SPRUCE_TAIGA_HILLS, "dense", "forest", "hills", "rare", "overworld");
        putMany(biomeLabels, Biomes.MODIFIED_GRAVELLY_MOUNTAINS, "mountain", "sparse", "rare", "overworld", "modified");
        putMany(biomeLabels, Biomes.SHATTERED_SAVANNA, "hot", "dry", "sparse", "savanna", "mountain", "rare", "overworld");
        putMany(biomeLabels, Biomes.SHATTERED_SAVANNA_PLATEAU, "hot", "dry", "sparse", "savanna", "hills", "rare", "overworld", "plateau");
        putMany(biomeLabels, Biomes.ERODED_BADLANDS, "hot", "dry", "sparse", "mountain", "rare", "overworld");
        putMany(biomeLabels, Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, "hot", "dry", "sparse", "hills", "rare", "overworld", "plateau", "modified");
        putMany(biomeLabels, Biomes.MODIFIED_BADLANDS_PLATEAU, "hot", "dry", "sparse", "mountain", "rare", "overworld", "plateau", "modified");

    }

    private static void putMany(Multimap<ResourceLocation, String> map, RegistryKey<Biome> biome, String... labels)
    {
        map.putAll(biome.getRegistryName(), Arrays.asList(labels));
    }
}

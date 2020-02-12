package gigaherz.elementsofpower.database;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

public class EssenceOverrides
{
    public static final Supplier<Path> OVERRIDES = () -> FMLPaths.CONFIGDIR.get().resolve("elementsofpower_essences.json");

    private static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(MagicAmounts.class, new MagicAmounts.Serializer()).create();

    public static Map<String, MagicAmounts> essenceOverrides = Maps.newHashMap();

    public static void loadOverrides()
    {
        loadConfigOverrides();
        applyOverrides();
    }

    private static void loadConfigOverrides()
    {
        try
        {
            Reader r = new FileReader(OVERRIDES.get().toFile());
            Type type = new TypeToken<Map<String, MagicAmounts>>()
            {
            }.getType();

            Map<String, MagicAmounts> ovr = SERIALIZER.fromJson(r, type);
            if (ovr != null)
            {
                essenceOverrides.putAll(ovr);
            }
        }
        catch (FileNotFoundException e)
        {
            saveConfigOverrides();
        }
    }

    private static void saveConfigOverrides()
    {
        try
        {
            Writer w = new FileWriter(OVERRIDES.get().toFile());
            w.write(SERIALIZER.toJson(essenceOverrides));
            w.flush();
            w.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void addCustomOverride(ItemStack stack, MagicAmounts amounts)
    {
        loadConfigOverrides();

        ResourceLocation resloc = stack.getItem().getRegistryName();
        assert resloc != null;
        String itemName = resloc.toString();

        essenceOverrides.put(itemName, amounts);

        saveConfigOverrides();
    }

    private static void applyOverrides()
    {
        for (Map.Entry<String, MagicAmounts> e : essenceOverrides.entrySet())
        {
            String itemName = e.getKey();

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item != null)
            {
                ItemStack stack = new ItemStack(item, 1);
                MagicAmounts m = e.getValue();

                EssenceConversions.addConversion(stack.getItem(), m);
            }
        }
    }
}

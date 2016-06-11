package gigaherz.elementsofpower.database;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public class EssenceOverrides
{
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
            Reader r = new FileReader(ElementsOfPower.overrides);
            Type type = new TypeToken<Map<String, MagicAmounts>>()
            {
            }.getType();

            Map<String, MagicAmounts> ovr = SERIALIZER.<Map<String, MagicAmounts>>fromJson(r, type);
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
            Writer w = new FileWriter(ElementsOfPower.overrides);
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

        ResourceLocation resloc = Item.REGISTRY.getNameForObject(stack.getItem());
        assert resloc != null;
        String itemName = resloc.toString();
        String entryName = String.format("%s@%d", itemName, stack.getMetadata());

        essenceOverrides.put(entryName, amounts);

        saveConfigOverrides();
    }

    private static void applyOverrides()
    {
        for (Map.Entry<String, MagicAmounts> e : essenceOverrides.entrySet())
        {
            String itemName;
            String entryName = e.getKey();
            int meta;
            int pos = entryName.lastIndexOf('@');
            if (pos <= 0)
            {
                itemName = entryName;
                meta = 0;
            }
            else
            {
                itemName = entryName.substring(0, pos);
                meta = Integer.parseInt(entryName.substring(pos + 1));
            }

            Item item = Item.REGISTRY.getObject(new ResourceLocation(itemName));
            if (item != null)
            {
                ItemStack stack = new ItemStack(item, 1, meta);
                MagicAmounts m = e.getValue();

                EssenceConversions.addConversion(stack, m);
            }
        }
    }
}

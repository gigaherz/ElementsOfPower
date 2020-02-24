package gigaherz.elementsofpower.database;

import com.google.common.collect.ImmutableMap;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;

import java.util.Map;
import java.util.Random;

public class GemstoneExaminer
{
    public static final Tag<Item> GEMSTONES = new ItemTags.Wrapper(ElementsOfPowerMod.location("gemstones"));
    public static final Tag<Item> GEM_RUBY = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/ruby"));
    public static final Tag<Item> GEM_SAPPHIRE = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/sapphire"));
    public static final Tag<Item> GEM_CITRINE = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/citrine"));
    public static final Tag<Item> GEM_AGATE = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/agate"));
    public static final Tag<Item> GEM_QUARTZ = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/quartz"));
    public static final Tag<Item> GEM_SERENDIBITE = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/serendibite"));
    public static final Tag<Item> GEM_EMERALD = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/emerald"));
    public static final Tag<Item> GEM_AMETHYST = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/amethyst"));
    public static final Tag<Item> GEM_DIAMOND = new ItemTags.Wrapper(ElementsOfPowerMod.location("gems/diamond"));

    private static final Map<Gemstone, Tag<Item>> GEMS = ImmutableMap.<Gemstone, Tag<Item>>builder()
            .put(Gemstone.RUBY, GEM_RUBY)
            .put(Gemstone.SAPPHIRE, GEM_SAPPHIRE)
            .put(Gemstone.CITRINE, GEM_CITRINE)
            .put(Gemstone.AGATE, GEM_AGATE)
            .put(Gemstone.QUARTZ, GEM_QUARTZ)
            .put(Gemstone.SERENDIBITE, GEM_SERENDIBITE)
            .put(Gemstone.EMERALD, GEM_EMERALD)
            .put(Gemstone.AMETHYST, GEM_AMETHYST)
            .put(Gemstone.DIAMOND, GEM_DIAMOND)
            .build();

    static Random rand = new Random();

    public static ItemStack identifyQuality(ItemStack stack)
    {
        if (stack.getCount() <= 0)
            return ItemStack.EMPTY;

        Item item = stack.getItem();
        if (item instanceof GemstoneItem)
        {
            if (((GemstoneItem) item).getQuality(stack) != null)
                return stack;
        }

        return GEMS.entrySet().stream()
                .filter(kv -> kv.getValue().contains(item))
                .findFirst()
                .map(kv -> setRandomQualityVariant(kv.getKey()))
                .orElse(stack);
    }

    private static ItemStack setRandomQualityVariant(Gemstone target)
    {
        float rnd = rand.nextFloat();
        if (rnd > 0.3f)
            return target.getItem().getStack(Quality.ROUGH);
        if (rnd > 0.1f)
            return target.getItem().getStack(Quality.COMMON);
        if (rnd > 0.01f)
            return target.getItem().getStack(Quality.SMOOTH);
        if (rnd > 0.001f)
            return target.getItem().getStack(Quality.FLAWLESS);

        return target.getItem().getStack(Quality.PURE);
    }
}

package dev.gigaherz.elementsofpower.database;

import com.google.common.collect.ImmutableMap;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Random;

public class GemstoneExaminer
{
    public static final Tag.Named<Item> GEMSTONES = ItemTags.bind(ElementsOfPowerMod.location("gemstones").toString());
    public static final Tag.Named<Item> GEM_RUBY = ItemTags.bind(ElementsOfPowerMod.location("gems/ruby").toString());
    public static final Tag.Named<Item> GEM_SAPPHIRE = ItemTags.bind(ElementsOfPowerMod.location("gems/sapphire").toString());
    public static final Tag.Named<Item> GEM_CITRINE = ItemTags.bind(ElementsOfPowerMod.location("gems/citrine").toString());
    public static final Tag.Named<Item> GEM_AGATE = ItemTags.bind(ElementsOfPowerMod.location("gems/agate").toString());
    public static final Tag.Named<Item> GEM_QUARTZ = ItemTags.bind(ElementsOfPowerMod.location("gems/quartz").toString());
    public static final Tag.Named<Item> GEM_SERENDIBITE = ItemTags.bind(ElementsOfPowerMod.location("gems/serendibite").toString());
    public static final Tag.Named<Item> GEM_EMERALD = ItemTags.bind(ElementsOfPowerMod.location("gems/emerald").toString());
    public static final Tag.Named<Item> GEM_AMETHYST = ItemTags.bind(ElementsOfPowerMod.location("gems/amethyst").toString());
    public static final Tag.Named<Item> GEM_DIAMOND = ItemTags.bind(ElementsOfPowerMod.location("gems/diamond").toString());

    public static final Map<Gemstone, Tag.Named<Item>> GEMS = ImmutableMap.<Gemstone, Tag.Named<Item>>builder()
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
        if (rnd >= 0.8f)
            return target.getItem().getStack(Quality.ROUGH);
        if (rnd >= 0.1f)
            return target.getItem().getStack(Quality.COMMON);
        if (rnd >= 0.01f)
            return target.getItem().getStack(Quality.SMOOTH);
        if (rnd >= 0.001f)
            return target.getItem().getStack(Quality.FLAWLESS);

        return target.getItem().getStack(Quality.PURE);
    }
}

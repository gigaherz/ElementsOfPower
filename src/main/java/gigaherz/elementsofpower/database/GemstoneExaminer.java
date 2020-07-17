package gigaherz.elementsofpower.database;

import com.google.common.collect.ImmutableMap;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.ITag;

import java.util.Map;
import java.util.Random;

public class GemstoneExaminer
{
    public static final ITag.INamedTag<Item> GEMSTONES = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gemstones").toString());
    public static final ITag.INamedTag<Item> GEM_RUBY = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/ruby").toString());
    public static final ITag.INamedTag<Item> GEM_SAPPHIRE = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/sapphire").toString());
    public static final ITag.INamedTag<Item> GEM_CITRINE = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/citrine").toString());
    public static final ITag.INamedTag<Item> GEM_AGATE = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/agate").toString());
    public static final ITag.INamedTag<Item> GEM_QUARTZ = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/quartz").toString());
    public static final ITag.INamedTag<Item> GEM_SERENDIBITE = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/serendibite").toString());
    public static final ITag.INamedTag<Item> GEM_EMERALD = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/emerald").toString());
    public static final ITag.INamedTag<Item> GEM_AMETHYST = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/amethyst").toString());
    public static final ITag.INamedTag<Item> GEM_DIAMOND = ItemTags.makeWrapperTag(ElementsOfPowerMod.location("gems/diamond").toString());

    public static final Map<Gemstone, ITag.INamedTag<Item>> GEMS = ImmutableMap.<Gemstone, ITag.INamedTag<Item>>builder()
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

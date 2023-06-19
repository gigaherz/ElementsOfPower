package dev.gigaherz.elementsofpower.database;

import com.google.common.collect.ImmutableMap;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.gemstones.Quality;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Random;

public class GemstoneExaminer
{
    public static final TagKey<Item> GEMSTONES = bind(ElementsOfPowerMod.location("gemstones").toString());
    public static final TagKey<Item> GEM_RUBY = bind(ElementsOfPowerMod.location("gems/ruby").toString());
    public static final TagKey<Item> GEM_SAPPHIRE = bind(ElementsOfPowerMod.location("gems/sapphire").toString());
    public static final TagKey<Item> GEM_CITRINE = bind(ElementsOfPowerMod.location("gems/citrine").toString());
    public static final TagKey<Item> GEM_AGATE = bind(ElementsOfPowerMod.location("gems/agate").toString());
    public static final TagKey<Item> GEM_QUARTZ = bind(ElementsOfPowerMod.location("gems/quartz").toString());
    public static final TagKey<Item> GEM_SERENDIBITE = bind(ElementsOfPowerMod.location("gems/serendibite").toString());
    public static final TagKey<Item> GEM_EMERALD = bind(ElementsOfPowerMod.location("gems/emerald").toString());
    public static final TagKey<Item> GEM_ELBAITE = bind(ElementsOfPowerMod.location("gems/elbaite").toString());
    public static final TagKey<Item> GEM_DIAMOND = bind(ElementsOfPowerMod.location("gems/diamond").toString());

    public static final Map<Gemstone, TagKey<Item>> GEMS = ImmutableMap.<Gemstone, TagKey<Item>>builder()
            .put(Gemstone.RUBY, GEM_RUBY)
            .put(Gemstone.SAPPHIRE, GEM_SAPPHIRE)
            .put(Gemstone.CITRINE, GEM_CITRINE)
            .put(Gemstone.AGATE, GEM_AGATE)
            .put(Gemstone.QUARTZ, GEM_QUARTZ)
            .put(Gemstone.SERENDIBITE, GEM_SERENDIBITE)
            .put(Gemstone.EMERALD, GEM_EMERALD)
            .put(Gemstone.ELBAITE, GEM_ELBAITE)
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

        var rk = ForgeRegistries.ITEMS.getResourceKey(item);
        var h = rk.flatMap(ForgeRegistries.ITEMS::getHolder);
        return h.flatMap(holder ->
                GEMS.entrySet().stream()
                .filter(kv -> holder.is(kv.getValue()))
                .findFirst()
                .map(kv -> setRandomQualityVariant(kv.getKey())))
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

    private static TagKey<Item> bind(String p_203855_) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(p_203855_));
    }

}

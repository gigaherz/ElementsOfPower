package dev.gigaherz.elementsofpower;

import dev.gigaherz.elementsofpower.analyzer.AnalyzerItem;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.items.*;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ElementsOfPowerItems
{
    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ElementsOfPowerMod.MODID);

    private static DeferredItem<BlockItem> registerBlockItem(DeferredHolder<Block, ? extends Block> block)
    {
        if (!block.getId().getNamespace().equals(ElementsOfPowerMod.MODID))
            throw new IllegalStateException("Wrong block item registered");

        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static final DeferredItem<BlockItem> ESSENTIALIZER = registerBlockItem(ElementsOfPowerBlocks.ESSENTIALIZER);
    public static final DeferredItem<AnalyzerItem> ANALYZER = ITEMS.register("analyzer", () -> new AnalyzerItem(ElementsOfPowerBlocks.ANALYZER.get(), new Item.Properties().stacksTo(1))        );
    public static final DeferredItem<WandItem> WAND = ITEMS.register("wand", () -> new WandItem(new Item.Properties().stacksTo(1))        );
    public static final DeferredItem<StaffItem> STAFF = ITEMS.register("staff", () -> new StaffItem(new Item.Properties().stacksTo(1))        );
    public static final DeferredItem<BaubleItem> RING = ITEMS.register("ring", () -> new BaubleItem(new Item.Properties().stacksTo(1))        );
    public static final DeferredItem<BaubleItem> BRACELET = ITEMS.register("bracelet", () -> new BaubleItem(new Item.Properties().stacksTo(1))        );
    public static final DeferredItem<BaubleItem> NECKLACE = ITEMS.register("necklace", () -> new BaubleItem(new Item.Properties().stacksTo(1))        );

    public static final DeferredItem<GemPouchItem> GEM_POUCH = ITEMS.register("gem_pouch", () -> new GemPouchItem(new Item.Properties().stacksTo(1))        );

    private static DeferredItem<GemstoneItem> registerGemstone(Gemstone type)
    {
        return ITEMS.register(type.getSerializedName(), () -> new GemstoneItem(type, new Item.Properties().stacksTo(1)));
    }

    public static final DeferredItem<GemstoneItem> RUBY = registerGemstone(Gemstone.RUBY);
    public static final DeferredItem<GemstoneItem> SAPPHIRE = registerGemstone(Gemstone.SAPPHIRE);
    public static final DeferredItem<GemstoneItem> CITRINE = registerGemstone(Gemstone.CITRINE);
    public static final DeferredItem<GemstoneItem> AGATE = registerGemstone(Gemstone.AGATE);
    public static final DeferredItem<GemstoneItem> QUARTZ = registerGemstone(Gemstone.QUARTZ);
    public static final DeferredItem<GemstoneItem> ONYX = registerGemstone(Gemstone.ONYX);
    public static final DeferredItem<GemstoneItem> EMERALD = registerGemstone(Gemstone.EMERALD);
    public static final DeferredItem<GemstoneItem> RUBELLITE = registerGemstone(Gemstone.RUBELLITE);
    public static final DeferredItem<GemstoneItem> DIAMOND = registerGemstone(Gemstone.DIAMOND);
    public static final DeferredItem<GemstoneItem> CREATIVITE = registerGemstone(Gemstone.CREATIVITE);

    public static final DeferredItem<BlockItem> RUBY_ORE = registerBlockItem(ElementsOfPowerBlocks.RUBY_ORE);
    public static final DeferredItem<BlockItem> SAPPHIRE_ORE = registerBlockItem(ElementsOfPowerBlocks.SAPPHIRE_ORE);
    public static final DeferredItem<BlockItem> CITRINE_ORE = registerBlockItem(ElementsOfPowerBlocks.CITRINE_ORE);
    public static final DeferredItem<BlockItem> AGATE_ORE = registerBlockItem(ElementsOfPowerBlocks.AGATE_ORE);
    public static final DeferredItem<BlockItem> ONYX_ORE = registerBlockItem(ElementsOfPowerBlocks.ONYX_ORE);
    public static final DeferredItem<BlockItem> rubellite_ORE = registerBlockItem(ElementsOfPowerBlocks.RUBELLITE_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_RUBY_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_RUBY_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_SAPPHIRE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_SAPPHIRE_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_CITRINE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_CITRINE_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_AGATE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_AGATE_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_ONYX_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_ONYX_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_rubellite_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_RUBELLITE_ORE);
    public static final DeferredItem<BlockItem> RUBY_BLOCK = registerBlockItem(ElementsOfPowerBlocks.RUBY_BLOCK);
    public static final DeferredItem<BlockItem> SAPPHIRE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.SAPPHIRE_BLOCK);
    public static final DeferredItem<BlockItem> CITRINE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.CITRINE_BLOCK);
    public static final DeferredItem<BlockItem> AGATE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.AGATE_BLOCK);
    public static final DeferredItem<BlockItem> ONYX_BLOCK = registerBlockItem(ElementsOfPowerBlocks.ONYX_BLOCK);
    public static final DeferredItem<BlockItem> rubellite_BLOCK = registerBlockItem(ElementsOfPowerBlocks.rubellite_BLOCK);

    private static DeferredItem<MagicOrbItem> registerOrb(Element type)
    {
        return ITEMS.register(type.getName() + "_orb", () ->
                new MagicOrbItem(type, new Item.Properties())
        );
    }

    public static final DeferredItem<MagicOrbItem> FIRE_ORB = registerOrb(Element.FIRE);
    public static final DeferredItem<MagicOrbItem> WATER_ORB = registerOrb(Element.WATER);
    public static final DeferredItem<MagicOrbItem> AIR_ORB = registerOrb(Element.AIR);
    public static final DeferredItem<MagicOrbItem> EARTH_ORB = registerOrb(Element.EARTH);
    public static final DeferredItem<MagicOrbItem> LIGHT_ORB = registerOrb(Element.LIGHT);
    public static final DeferredItem<MagicOrbItem> TIME_ORB = registerOrb(Element.TIME);
    public static final DeferredItem<MagicOrbItem> LIFE_ORB = registerOrb(Element.LIFE);
    public static final DeferredItem<MagicOrbItem> CHAOS_ORB = registerOrb(Element.CHAOS);

    public static final DeferredItem<BlockItem> FIRE_COCOON = registerBlockItem(ElementsOfPowerBlocks.FIRE_COCOON);
    public static final DeferredItem<BlockItem> WATER_COCOON = registerBlockItem(ElementsOfPowerBlocks.WATER_COCOON);
    public static final DeferredItem<BlockItem> AIR_COCOON = registerBlockItem(ElementsOfPowerBlocks.AIR_COCOON);
    public static final DeferredItem<BlockItem> EARTH_COCOON = registerBlockItem(ElementsOfPowerBlocks.EARTH_COCOON);
    public static final DeferredItem<BlockItem> LIGHT_COCOON = registerBlockItem(ElementsOfPowerBlocks.LIGHT_COCOON);
    public static final DeferredItem<BlockItem> TIME_COCOON = registerBlockItem(ElementsOfPowerBlocks.TIME_COCOON);
    public static final DeferredItem<BlockItem> LIFE_COCOON = registerBlockItem(ElementsOfPowerBlocks.LIFE_COCOON);
    public static final DeferredItem<BlockItem> CHAOS_COCOON = registerBlockItem(ElementsOfPowerBlocks.CHAOS_COCOON);

}

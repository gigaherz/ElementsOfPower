package dev.gigaherz.elementsofpower;

import dev.gigaherz.elementsofpower.analyzer.AnalyzerItem;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneItem;
import dev.gigaherz.elementsofpower.items.BaubleItem;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.items.StaffItem;
import dev.gigaherz.elementsofpower.items.WandItem;
import dev.gigaherz.elementsofpower.spells.Element;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ElementsOfPowerItems
{
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ElementsOfPowerMod.MODID);

    private static RegistryObject<BlockItem> registerBlockItem(RegistryObject<? extends Block> block)
    {
        if (!block.getId().getNamespace().equals(ElementsOfPowerMod.MODID))
            throw new IllegalStateException("Wrong block item registered");

        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static final RegistryObject<BlockItem> ESSENTIALIZER = registerBlockItem(ElementsOfPowerBlocks.ESSENTIALIZER);
    public static final RegistryObject<AnalyzerItem> ANALYZER = ITEMS.register("analyzer", () -> new AnalyzerItem(new Item.Properties().stacksTo(1))        );
    public static final RegistryObject<WandItem> WAND = ITEMS.register("wand", () -> new WandItem(new Item.Properties().stacksTo(1))        );
    public static final RegistryObject<StaffItem> STAFF = ITEMS.register("staff", () -> new StaffItem(new Item.Properties().stacksTo(1))        );
    public static final RegistryObject<BaubleItem> RING = ITEMS.register("ring", () -> new BaubleItem(new Item.Properties().stacksTo(1))        );
    public static final RegistryObject<BaubleItem> BRACELET = ITEMS.register("bracelet", () -> new BaubleItem(new Item.Properties().stacksTo(1))        );
    public static final RegistryObject<BaubleItem> NECKLACE = ITEMS.register("necklace", () -> new BaubleItem(new Item.Properties().stacksTo(1))        );

    private static RegistryObject<GemstoneItem> registerGemstone(Gemstone type)
    {
        return ITEMS.register(type.getSerializedName(), () -> new GemstoneItem(type, new Item.Properties().stacksTo(1)));
    }

    public static final RegistryObject<GemstoneItem> RUBY = registerGemstone(Gemstone.RUBY);
    public static final RegistryObject<GemstoneItem> SAPPHIRE = registerGemstone(Gemstone.SAPPHIRE);
    public static final RegistryObject<GemstoneItem> CITRINE = registerGemstone(Gemstone.CITRINE);
    public static final RegistryObject<GemstoneItem> AGATE = registerGemstone(Gemstone.AGATE);
    public static final RegistryObject<GemstoneItem> QUARTZ = registerGemstone(Gemstone.QUARTZ);
    public static final RegistryObject<GemstoneItem> SERENDIBITE = registerGemstone(Gemstone.SERENDIBITE);
    public static final RegistryObject<GemstoneItem> EMERALD = registerGemstone(Gemstone.EMERALD);
    public static final RegistryObject<GemstoneItem> ELBAITE = registerGemstone(Gemstone.ELBAITE);
    public static final RegistryObject<GemstoneItem> DIAMOND = registerGemstone(Gemstone.DIAMOND);
    public static final RegistryObject<GemstoneItem> CREATIVITE = registerGemstone(Gemstone.CREATIVITE);

    public static final RegistryObject<BlockItem> RUBY_ORE = registerBlockItem(ElementsOfPowerBlocks.RUBY_ORE);
    public static final RegistryObject<BlockItem> SAPPHIRE_ORE = registerBlockItem(ElementsOfPowerBlocks.SAPPHIRE_ORE);
    public static final RegistryObject<BlockItem> CITRINE_ORE = registerBlockItem(ElementsOfPowerBlocks.CITRINE_ORE);
    public static final RegistryObject<BlockItem> AGATE_ORE = registerBlockItem(ElementsOfPowerBlocks.AGATE_ORE);
    public static final RegistryObject<BlockItem> SERENDIBITE_ORE = registerBlockItem(ElementsOfPowerBlocks.SERENDIBITE_ORE);
    public static final RegistryObject<BlockItem> ELBAITE_ORE = registerBlockItem(ElementsOfPowerBlocks.ELBAITE_ORE);
    public static final RegistryObject<BlockItem> DEEPSLATE_RUBY_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_RUBY_ORE);
    public static final RegistryObject<BlockItem> DEEPSLATE_SAPPHIRE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_SAPPHIRE_ORE);
    public static final RegistryObject<BlockItem> DEEPSLATE_CITRINE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_CITRINE_ORE);
    public static final RegistryObject<BlockItem> DEEPSLATE_AGATE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_AGATE_ORE);
    public static final RegistryObject<BlockItem> DEEPSLATE_SERENDIBITE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_SERENDIBITE_ORE);
    public static final RegistryObject<BlockItem> DEEPSLATE_ELBAITE_ORE = registerBlockItem(ElementsOfPowerBlocks.DEEPSLATE_ELBAITE_ORE);
    public static final RegistryObject<BlockItem> RUBY_BLOCK = registerBlockItem(ElementsOfPowerBlocks.RUBY_BLOCK);
    public static final RegistryObject<BlockItem> SAPPHIRE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.SAPPHIRE_BLOCK);
    public static final RegistryObject<BlockItem> CITRINE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.CITRINE_BLOCK);
    public static final RegistryObject<BlockItem> AGATE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.AGATE_BLOCK);
    public static final RegistryObject<BlockItem> SERENDIBITE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.SERENDIBITE_BLOCK);
    public static final RegistryObject<BlockItem> ELBAITE_BLOCK = registerBlockItem(ElementsOfPowerBlocks.ELBAITE_BLOCK);

    private static RegistryObject<MagicOrbItem> registerOrb(Element type)
    {
        return ITEMS.register(type.getName() + "_orb", () ->
                new MagicOrbItem(type, new Item.Properties())
        );
    }

    public static final RegistryObject<MagicOrbItem> FIRE_ORB = registerOrb(Element.FIRE);
    public static final RegistryObject<MagicOrbItem> WATER_ORB = registerOrb(Element.WATER);
    public static final RegistryObject<MagicOrbItem> AIR_ORB = registerOrb(Element.AIR);
    public static final RegistryObject<MagicOrbItem> EARTH_ORB = registerOrb(Element.EARTH);
    public static final RegistryObject<MagicOrbItem> LIGHT_ORB = registerOrb(Element.LIGHT);
    public static final RegistryObject<MagicOrbItem> TIME_ORB = registerOrb(Element.TIME);
    public static final RegistryObject<MagicOrbItem> LIFE_ORB = registerOrb(Element.LIFE);
    public static final RegistryObject<MagicOrbItem> CHAOS_ORB = registerOrb(Element.CHAOS);

    public static final RegistryObject<BlockItem> FIRE_COCOON = registerBlockItem(ElementsOfPowerBlocks.FIRE_COCOON);
    public static final RegistryObject<BlockItem> WATER_COCOON = registerBlockItem(ElementsOfPowerBlocks.WATER_COCOON);
    public static final RegistryObject<BlockItem> AIR_COCOON = registerBlockItem(ElementsOfPowerBlocks.AIR_COCOON);
    public static final RegistryObject<BlockItem> EARTH_COCOON = registerBlockItem(ElementsOfPowerBlocks.EARTH_COCOON);
    public static final RegistryObject<BlockItem> LIGHT_COCOON = registerBlockItem(ElementsOfPowerBlocks.LIGHT_COCOON);
    public static final RegistryObject<BlockItem> TIME_COCOON = registerBlockItem(ElementsOfPowerBlocks.TIME_COCOON);
    public static final RegistryObject<BlockItem> LIFE_COCOON = registerBlockItem(ElementsOfPowerBlocks.LIFE_COCOON);
    public static final RegistryObject<BlockItem> CHAOS_COCOON = registerBlockItem(ElementsOfPowerBlocks.CHAOS_COCOON);

}

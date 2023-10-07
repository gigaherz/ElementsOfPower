package dev.gigaherz.elementsofpower;

import dev.gigaherz.elementsofpower.analyzer.AnalyzerBlock;
import dev.gigaherz.elementsofpower.analyzer.AnalyzerItem;
import dev.gigaherz.elementsofpower.cocoons.CocoonBlock;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlock;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import dev.gigaherz.elementsofpower.gemstones.GemstoneBlock;
import dev.gigaherz.elementsofpower.gemstones.GemstoneOreBlock;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ElementsOfPowerBlocks
{
    static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ElementsOfPowerMod.MODID);

    public static final RegistryObject<EssentializerBlock> ESSENTIALIZER = BLOCKS.register("essentializer", () ->
            new EssentializerBlock(Block.Properties.of().mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops().strength(15.0F)
            .sound(SoundType.METAL).lightLevel(b -> 1)));

    public static final RegistryObject<AnalyzerBlock> ANALYZER = BLOCKS.register("analyzer", () ->
            new AnalyzerBlock(BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.0f)));

    public static final RegistryObject<DustBlock> DUST = BLOCKS.register("dust", () ->
            new DustBlock(Block.Properties.of().forceSolidOff().replaceable().pushReaction(PushReaction.DESTROY).noLootTable().noCollission().noOcclusion()
            .isSuffocating((s, w, p) -> true).isViewBlocking((s, w, p) -> false)
            .strength(0.1F).sound(SoundType.WOOL).dynamicShape()));
    public static final RegistryObject<MistBlock> MIST = BLOCKS.register("mist", () ->
            new MistBlock(Block.Properties.of().forceSolidOff().replaceable().pushReaction(PushReaction.DESTROY).noLootTable().noCollission().noOcclusion()
            .isSuffocating((s, w, p) -> false).isViewBlocking((s, w, p) -> false)
            .strength(0.1F).sound(SoundType.WOOL).dynamicShape()));
    public static final RegistryObject<LightBlock> LIGHT = BLOCKS.register("light", () ->
            new LightBlock(Block.Properties.of().forceSolidOff().replaceable().pushReaction(PushReaction.DESTROY).noLootTable().noCollission().noOcclusion()
            .isSuffocating((s, w, p) -> false).isViewBlocking((s, w, p) -> false)
            .strength(15.0F).lightLevel(b -> 15).sound(SoundType.METAL)));
    public static final RegistryObject<CushionBlock> CUSHION = BLOCKS.register("cushion", () ->
            new CushionBlock(Block.Properties.of().forceSolidOff().replaceable().pushReaction(PushReaction.DESTROY).noLootTable().noCollission().noOcclusion()
            .isSuffocating((s, w, p) -> false).isViewBlocking((s, w, p) -> false)
            .strength(15.0F).sound(SoundType.METAL).dynamicShape()));


    private static RegistryObject<CocoonBlock> registerCocoon(Element type)
    {
        return BLOCKS.register(type.getName() + "_cocoon", () ->
                new CocoonBlock(type, Block.Properties.of().mapColor(MapColor.PLANT).strength(1F).pushReaction(PushReaction.DESTROY)
                        .sound(SoundType.WOOD).lightLevel(b -> 11))
        );
    }

    public static final RegistryObject<CocoonBlock> FIRE_COCOON = registerCocoon(Element.FIRE);
    public static final RegistryObject<CocoonBlock> WATER_COCOON = registerCocoon(Element.WATER);
    public static final RegistryObject<CocoonBlock> AIR_COCOON = registerCocoon(Element.AIR);
    public static final RegistryObject<CocoonBlock> EARTH_COCOON = registerCocoon(Element.EARTH);
    public static final RegistryObject<CocoonBlock> LIGHT_COCOON = registerCocoon(Element.LIGHT);
    public static final RegistryObject<CocoonBlock> TIME_COCOON = registerCocoon(Element.TIME);
    public static final RegistryObject<CocoonBlock> LIFE_COCOON = registerCocoon(Element.LIFE);
    public static final RegistryObject<CocoonBlock> CHAOS_COCOON = registerCocoon(Element.CHAOS);

    private static RegistryObject<GemstoneOreBlock> registerGemstoneOre(Gemstone type)
    {
        return BLOCKS.register(type.getSerializedName() + "_ore", ()->
                new GemstoneOreBlock(type, Block.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()
                        .strength(3.0F, 3.0F)
                )
        );
    }

    private static RegistryObject<GemstoneOreBlock> registerDeepslateGemstoneOre(Gemstone type)
    {
        return BLOCKS.register("deepslate_" + type.getSerializedName() + "_ore", ()->
                new GemstoneOreBlock(type, Block.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                        .sound(SoundType.DEEPSLATE)
                        .requiresCorrectToolForDrops()
                        .strength(4.5F, 3.0F)
                )
        );
    }

    public static final RegistryObject<GemstoneOreBlock> RUBY_ORE = registerGemstoneOre(Gemstone.RUBY);
    public static final RegistryObject<GemstoneOreBlock> SAPPHIRE_ORE = registerGemstoneOre(Gemstone.SAPPHIRE);
    public static final RegistryObject<GemstoneOreBlock> CITRINE_ORE = registerGemstoneOre(Gemstone.CITRINE);
    public static final RegistryObject<GemstoneOreBlock> AGATE_ORE = registerGemstoneOre(Gemstone.AGATE);
    public static final RegistryObject<GemstoneOreBlock> ONYX_ORE = registerGemstoneOre(Gemstone.ONYX);
    public static final RegistryObject<GemstoneOreBlock> RUBELLITE_ORE = registerGemstoneOre(Gemstone.RUBELLITE);
    public static final RegistryObject<GemstoneOreBlock> DEEPSLATE_RUBY_ORE = registerDeepslateGemstoneOre(Gemstone.RUBY);
    public static final RegistryObject<GemstoneOreBlock> DEEPSLATE_SAPPHIRE_ORE = registerDeepslateGemstoneOre(Gemstone.SAPPHIRE);
    public static final RegistryObject<GemstoneOreBlock> DEEPSLATE_CITRINE_ORE = registerDeepslateGemstoneOre(Gemstone.CITRINE);
    public static final RegistryObject<GemstoneOreBlock> DEEPSLATE_AGATE_ORE = registerDeepslateGemstoneOre(Gemstone.AGATE);
    public static final RegistryObject<GemstoneOreBlock> DEEPSLATE_ONYX_ORE = registerDeepslateGemstoneOre(Gemstone.ONYX);
    public static final RegistryObject<GemstoneOreBlock> DEEPSLATE_RUBELLITE_ORE = registerDeepslateGemstoneOre(Gemstone.RUBELLITE);


    private static RegistryObject<GemstoneBlock> registerGemstoneBlock(Gemstone type)
    {
        return BLOCKS.register(type.getSerializedName() + "_block", () ->
                new GemstoneBlock(type, Block.Properties.of().mapColor(MapColor.METAL)
                        .strength(5F, 6F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                )
        );
    }

    public static final RegistryObject<GemstoneBlock> RUBY_BLOCK = registerGemstoneBlock(Gemstone.RUBY);
    public static final RegistryObject<GemstoneBlock> SAPPHIRE_BLOCK = registerGemstoneBlock(Gemstone.SAPPHIRE);
    public static final RegistryObject<GemstoneBlock> CITRINE_BLOCK = registerGemstoneBlock(Gemstone.CITRINE);
    public static final RegistryObject<GemstoneBlock> AGATE_BLOCK = registerGemstoneBlock(Gemstone.AGATE);
    public static final RegistryObject<GemstoneBlock> ONYX_BLOCK = registerGemstoneBlock(Gemstone.ONYX);
    public static final RegistryObject<GemstoneBlock> rubellite_BLOCK = registerGemstoneBlock(Gemstone.RUBELLITE);

}

package dev.gigaherz.elementsofpower;

import dev.gigaherz.elementsofpower.analyzer.AnalyzerBlock;
import dev.gigaherz.elementsofpower.cocoons.CocoonBlock;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlock;
import dev.gigaherz.elementsofpower.spells.Element;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ElementsOfPowerBlocks
{
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ElementsOfPowerMod.MODID);

    public static final DeferredBlock<EssentializerBlock> ESSENTIALIZER = BLOCKS.register("essentializer", () ->
            new EssentializerBlock(Block.Properties.of().mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops().strength(15.0F)
            .sound(SoundType.METAL).lightLevel(b -> 1)));

    public static final DeferredBlock<AnalyzerBlock>
            ANALYZER = BLOCKS.register("analyzer", () -> new AnalyzerBlock(BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY).mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.0f)));

    public static final DeferredBlock<DustBlock>
            DUST = registerGassyBlock("dust", DustBlock::new, true,0.1F,SoundType.WOOL);
    public static final DeferredBlock<MistBlock>
            MIST = registerGassyBlock("mist", MistBlock::new, false, 0.1F, SoundType.WOOL);
    public static final DeferredBlock<LightBlock>
            LIGHT = registerGassyBlock("light", p -> new LightBlock(p.lightLevel(b -> 15)), false, 15.0F, SoundType.METAL);
    public static final DeferredBlock<CushionBlock>
            CUSHION = registerGassyBlock("cushion", CushionBlock::new, false, 15.0F, SoundType.METAL);

    public static final DeferredBlock<CocoonBlock> FIRE_COCOON = registerCocoon(Element.FIRE);
    public static final DeferredBlock<CocoonBlock> WATER_COCOON = registerCocoon(Element.WATER);
    public static final DeferredBlock<CocoonBlock> AIR_COCOON = registerCocoon(Element.AIR);
    public static final DeferredBlock<CocoonBlock> EARTH_COCOON = registerCocoon(Element.EARTH);
    public static final DeferredBlock<CocoonBlock> LIGHT_COCOON = registerCocoon(Element.LIGHT);
    public static final DeferredBlock<CocoonBlock> TIME_COCOON = registerCocoon(Element.TIME);
    public static final DeferredBlock<CocoonBlock> LIFE_COCOON = registerCocoon(Element.LIFE);
    public static final DeferredBlock<CocoonBlock> CHAOS_COCOON = registerCocoon(Element.CHAOS);

    private static <T extends Block> DeferredBlock<T> registerGassyBlock(String name, Function<BlockBehaviour.Properties, T> factory, boolean doesSuffocate, float strength, SoundType soundType)
    {
        return BLOCKS.register(name, () -> factory.apply(Block.Properties.of()
                .forceSolidOff().replaceable().pushReaction(PushReaction.DESTROY)
                .noLootTable().noCollission().noOcclusion()
                .isSuffocating((s, w, p) -> doesSuffocate)
                .isViewBlocking((s, w, p) -> false)
                .strength(strength).sound(soundType).dynamicShape()));
    }

    private static DeferredBlock<CocoonBlock> registerCocoon(Element type)
    {
        return BLOCKS.register(type.getName() + "_cocoon", () ->
                new CocoonBlock(type, Block.Properties.of().mapColor(MapColor.PLANT).strength(1F).pushReaction(PushReaction.DESTROY)
                        .sound(SoundType.WOOD).lightLevel(b -> 11))
        );
    }

}

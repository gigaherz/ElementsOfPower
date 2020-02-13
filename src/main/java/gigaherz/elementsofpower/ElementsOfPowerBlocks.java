package gigaherz.elementsofpower;

import gigaherz.elementsofpower.cocoons.CocoonBlock;
import gigaherz.elementsofpower.essentializer.EssentializerBlock;
import gigaherz.elementsofpower.gemstones.GemstoneBlock;
import gigaherz.elementsofpower.spells.blocks.CushionBlock;
import gigaherz.elementsofpower.spells.blocks.DustBlock;
import gigaherz.elementsofpower.spells.blocks.MistBlock;
import gigaherz.elementsofpower.spells.blocks.LightBlock;
import net.minecraft.block.Block;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder("elementsofpower")
public class ElementsOfPowerBlocks
{
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static <T extends Block> T toBeInitialized() {
        return null;
    }

    public static final EssentializerBlock essentializer = toBeInitialized();
    public static final DustBlock dust = toBeInitialized();
    public static final MistBlock mist = toBeInitialized();
    public static final LightBlock light = toBeInitialized();
    public static final CushionBlock cushion = toBeInitialized();
    
    public static final CocoonBlock FIRE_COCOON = toBeInitialized();
    public static final CocoonBlock WATER_COCOON = toBeInitialized();
    public static final CocoonBlock AIR_COCOON = toBeInitialized();
    public static final CocoonBlock EARTH_COCOON = toBeInitialized();
    public static final CocoonBlock LIGHT_COCOON = toBeInitialized();
    public static final CocoonBlock DARKNESS_COCOON = toBeInitialized();
    public static final CocoonBlock LIFE_COCOON = toBeInitialized();
    public static final CocoonBlock DEATH_COCOON = toBeInitialized();

    public static final GemstoneBlock RUBY_ORE = toBeInitialized();
    public static final GemstoneBlock SAPPHIRE_ORE = toBeInitialized();
    public static final GemstoneBlock CITRINE_ORE = toBeInitialized();
    public static final GemstoneBlock AGATE_ORE = toBeInitialized();
    public static final GemstoneBlock SERENDIBITE_ORE = toBeInitialized();
    public static final GemstoneBlock AMETHYST_ORE = toBeInitialized();

    public static final GemstoneBlock RUBY_BLOCK = toBeInitialized();
    public static final GemstoneBlock SAPPHIRE_BLOCK = toBeInitialized();
    public static final GemstoneBlock CITRINE_BLOCK = toBeInitialized();
    public static final GemstoneBlock AGATE_BLOCK = toBeInitialized();
    public static final GemstoneBlock SERENDIBITE_BLOCK = toBeInitialized();
    public static final GemstoneBlock AMETHYST_BLOCK = toBeInitialized();
}

package dev.gigaherz.elementsofpower;

import dev.gigaherz.elementsofpower.cocoons.CocoonBlock;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlock;
import dev.gigaherz.elementsofpower.gemstones.GemstoneBlock;
import dev.gigaherz.elementsofpower.gemstones.GemstoneOreBlock;
import dev.gigaherz.elementsofpower.spells.blocks.CushionBlock;
import dev.gigaherz.elementsofpower.spells.blocks.DustBlock;
import dev.gigaherz.elementsofpower.spells.blocks.LightBlock;
import dev.gigaherz.elementsofpower.spells.blocks.MistBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder("elementsofpower")
public class ElementsOfPowerBlocks
{
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static <T extends Block> T toBeInitialized()
    {
        return null;
    }

    public static final EssentializerBlock ESSENTIALIZER = toBeInitialized();
    public static final DustBlock DUST = toBeInitialized();
    public static final MistBlock MIST = toBeInitialized();
    public static final LightBlock LIGHT = toBeInitialized();
    public static final CushionBlock CUSHION = toBeInitialized();

    public static final CocoonBlock FIRE_COCOON = toBeInitialized();
    public static final CocoonBlock WATER_COCOON = toBeInitialized();
    public static final CocoonBlock AIR_COCOON = toBeInitialized();
    public static final CocoonBlock EARTH_COCOON = toBeInitialized();
    public static final CocoonBlock LIGHT_COCOON = toBeInitialized();
    public static final CocoonBlock DARKNESS_COCOON = toBeInitialized();
    public static final CocoonBlock LIFE_COCOON = toBeInitialized();
    public static final CocoonBlock DEATH_COCOON = toBeInitialized();

    public static final GemstoneOreBlock RUBY_ORE = toBeInitialized();
    public static final GemstoneOreBlock SAPPHIRE_ORE = toBeInitialized();
    public static final GemstoneOreBlock CITRINE_ORE = toBeInitialized();
    public static final GemstoneOreBlock AGATE_ORE = toBeInitialized();
    public static final GemstoneOreBlock SERENDIBITE_ORE = toBeInitialized();
    public static final GemstoneOreBlock AMETHYST_ORE = toBeInitialized();

    public static final GemstoneBlock RUBY_BLOCK = toBeInitialized();
    public static final GemstoneBlock SAPPHIRE_BLOCK = toBeInitialized();
    public static final GemstoneBlock CITRINE_BLOCK = toBeInitialized();
    public static final GemstoneBlock AGATE_BLOCK = toBeInitialized();
    public static final GemstoneBlock SERENDIBITE_BLOCK = toBeInitialized();
    public static final GemstoneBlock AMETHYST_BLOCK = toBeInitialized();

    public static class BlockMaterials
    {
        public static Material DUST = (new Material.Builder(MaterialColor.COLOR_BLACK)).notSolidBlocking().nonSolid().replaceable().destroyOnPush().build();
        public static Material MIST = (new Material.Builder(MaterialColor.COLOR_BLACK)).noCollider().notSolidBlocking().nonSolid().replaceable().destroyOnPush().build();
        public static Material LIGHT = (new Material.Builder(MaterialColor.COLOR_BLACK)).noCollider().notSolidBlocking().nonSolid().replaceable().destroyOnPush().build();
        public static Material CUSHION = (new Material.Builder(MaterialColor.COLOR_BLACK)).noCollider().notSolidBlocking().nonSolid().replaceable().destroyOnPush().build();
    }
}

package gigaherz.elementsofpower;

import gigaherz.elementsofpower.analyzer.AnalyzerItem;
import gigaherz.elementsofpower.gemstones.GemstoneItem;
import gigaherz.elementsofpower.items.BaubleItem;
import gigaherz.elementsofpower.items.MagicOrbItem;
import gigaherz.elementsofpower.items.StaffItem;
import gigaherz.elementsofpower.items.WandItem;
import gigaherz.elementsofpower.spelldust.SpelldustItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder("elementsofpower")
public class ElementsOfPowerItems
{
    @SuppressWarnings("ConstantConditions")
    @Nonnull
    private static <T extends Item> T toBeInitialized() {
        return null;
    }

    public static final MagicOrbItem FIRE_ORB = toBeInitialized();
    public static final MagicOrbItem WATER_ORB = toBeInitialized();
    public static final MagicOrbItem AIR_ORB = toBeInitialized();
    public static final MagicOrbItem EARTH_ORB = toBeInitialized();
    public static final MagicOrbItem LIGHT_ORB = toBeInitialized();
    public static final MagicOrbItem DARKNESS_ORB = toBeInitialized();
    public static final MagicOrbItem LIFE_ORB = toBeInitialized();
    public static final MagicOrbItem DEATH_ORB = toBeInitialized();

    public static final AnalyzerItem ANALYZER = toBeInitialized();
    public static final BlockItem ESSENTIALIZER = toBeInitialized();

    public static final WandItem WAND = toBeInitialized();
    public static final StaffItem STAFF = toBeInitialized();
    public static final BaubleItem RING = toBeInitialized();
    public static final BaubleItem HEADBAND = toBeInitialized();
    public static final BaubleItem NECKLACE = toBeInitialized();

    public static final GemstoneItem RUBY = toBeInitialized();
    public static final GemstoneItem SAPPHIRE = toBeInitialized();
    public static final GemstoneItem CITRINE = toBeInitialized();
    public static final GemstoneItem AGATE = toBeInitialized();
    public static final GemstoneItem QUARTZ = toBeInitialized();
    public static final GemstoneItem SERENDIBITE = toBeInitialized();
    public static final GemstoneItem EMERALD = toBeInitialized();
    public static final GemstoneItem AMETHYST = toBeInitialized();
    public static final GemstoneItem DIAMOND = toBeInitialized();
    public static final GemstoneItem CREATIVITE = toBeInitialized();

    public static final SpelldustItem RUBY_SPELLDUST = toBeInitialized();
    public static final SpelldustItem SAPPHIRE_SPELLDUST = toBeInitialized();
    public static final SpelldustItem CITRINE_SPELLDUST = toBeInitialized();
    public static final SpelldustItem AGATE_SPELLDUST = toBeInitialized();
    public static final SpelldustItem QUARTZ_SPELLDUST = toBeInitialized();
    public static final SpelldustItem SERENDIBITE_SPELLDUST = toBeInitialized();
    public static final SpelldustItem EMERALD_SPELLDUST = toBeInitialized();
    public static final SpelldustItem AMETHYST_SPELLDUST = toBeInitialized();
    public static final SpelldustItem DIAMOND_SPELLDUST = toBeInitialized();

    public static final BlockItem FIRE_COCOON = toBeInitialized();
    public static final BlockItem WATER_COCOON = toBeInitialized();
    public static final BlockItem AIR_COCOON = toBeInitialized();
    public static final BlockItem EARTH_COCOON = toBeInitialized();
    public static final BlockItem LIGHT_COCOON = toBeInitialized();
    public static final BlockItem DARKNESS_COCOON = toBeInitialized();
    public static final BlockItem LIFE_COCOON = toBeInitialized();
    public static final BlockItem DEATH_COCOON = toBeInitialized();
}

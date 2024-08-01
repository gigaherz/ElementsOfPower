package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.ElementsOfPowerBlocks;
import dev.gigaherz.elementsofpower.ElementsOfPowerItems;
import dev.gigaherz.elementsofpower.cocoons.CocoonBlock;
import dev.gigaherz.elementsofpower.items.MagicOrbItem;
import dev.gigaherz.elementsofpower.network.UpdateSpellSequence;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum Element implements StringRepresentable
{
    FIRE("fire", 1, ShapeType.CONE, EffectType.FLAME, () -> ElementsOfPowerBlocks.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_COCOON, () -> ElementsOfPowerItems.FIRE_ORB, 0xFFff3e00),
    WATER("water", 0, ShapeType.WALL, EffectType.WATER, () -> ElementsOfPowerBlocks.WATER_COCOON, () -> ElementsOfPowerItems.WATER_COCOON, () -> ElementsOfPowerItems.WATER_ORB, 0xFF005dff),
    AIR("air", 3, ShapeType.SPHERE, EffectType.PUSH, () -> ElementsOfPowerBlocks.AIR_COCOON, () -> ElementsOfPowerItems.AIR_COCOON, () -> ElementsOfPowerItems.AIR_ORB, 0xFFffed96),
    EARTH("earth", 2, ShapeType.BALL, EffectType.DUST, () -> ElementsOfPowerBlocks.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_COCOON, () -> ElementsOfPowerItems.EARTH_ORB, 0xFF7f3300),
    LIGHT("light", 5, ShapeType.BEAM, EffectType.LIGHT, () -> ElementsOfPowerBlocks.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_COCOON, () -> ElementsOfPowerItems.LIGHT_ORB, 0xFFffffff),
    TIME("time", 4, ShapeType.BEAM, EffectType.SLOWNESS, () -> ElementsOfPowerBlocks.TIME_COCOON, () -> ElementsOfPowerItems.TIME_COCOON, () -> ElementsOfPowerItems.TIME_ORB, 0xFF242424),
    LIFE("life", 7, ShapeType.SELF, EffectType.HEALING, () -> ElementsOfPowerBlocks.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_COCOON, () -> ElementsOfPowerItems.LIFE_ORB, 0xFF1acd7f),
    CHAOS("chaos", 6, ShapeType.SINGLE, EffectType.BREAKING, () -> ElementsOfPowerBlocks.CHAOS_COCOON, () -> ElementsOfPowerItems.CHAOS_COCOON, () -> ElementsOfPowerItems.CHAOS_ORB, 0xFF8c1acd),
    BALANCE("balance", 8, ShapeType.NONE, EffectType.NONE, null, null, null, 0xFF7F7F7F);

    public static final StreamCodec<FriendlyByteBuf, Element> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Element.class);

    private final int opposite;
    private final ShapeType shapeType;
    private final String name;
    private final EffectType initialEffect;
    @Nullable
    private final Supplier<CocoonBlock> blockSupplier;
    @Nullable
    private final Supplier<Item> itemSupplier;
    @Nullable
    private final Supplier<MagicOrbItem> orbSupplier;
    private final int color;

    @Nullable
    public static Element byName(String name)
    {
        for (Element e : values())
        {
            if (e.getName().equals(name))
                return e;
        }
        return null;
    }

    public Element getOpposite()
    {
        return values[opposite];
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    Element(String name, int opposite, ShapeType shapeType, EffectType initialEffect,
            @Nullable Supplier<DeferredHolder<Block, CocoonBlock>> blockSupplier,
            @Nullable Supplier<DeferredHolder<Item, ? extends Item>> itemSupplier,
            @Nullable Supplier<DeferredHolder<Item, MagicOrbItem>> orbSupplier, int color)
    {
        this.opposite = opposite;
        this.shapeType = shapeType;
        this.name = name;
        this.initialEffect = initialEffect;
        this.blockSupplier = blockSupplier != null ? () -> blockSupplier.get().value() : null;
        this.itemSupplier = itemSupplier != null ? () -> itemSupplier.get().value() : null;;
        this.orbSupplier = orbSupplier != null ? () -> orbSupplier.get().value() : null;;
        this.color = color;
    }

    public ShapeType getShape()
    {
        return shapeType;
    }

    public EffectType getInitialEffect()
    {
        return initialEffect;
    }

    @Nullable
    public CocoonBlock getCocoon()
    {
        return blockSupplier != null ? blockSupplier.get() : null;
    }

    @Nullable
    public Item getItem()
    {
        return itemSupplier != null ? itemSupplier.get() : null;
    }

    @Nullable
    public MagicOrbItem getOrb()
    {
        return orbSupplier != null ? orbSupplier.get() : null;
    }

    public int getColor()
    {
        return color;
    }

    public static final Element[] values = values();
    public static final Element[] values_without_balance = stream().filter(e -> e != BALANCE).toArray(Element[]::new);

    public static Stream<Element> stream()
    {
        return Arrays.stream(values);
    }

    public static Stream<Element> stream_without_balance()
    {
        return Arrays.stream(values_without_balance);
    }
}

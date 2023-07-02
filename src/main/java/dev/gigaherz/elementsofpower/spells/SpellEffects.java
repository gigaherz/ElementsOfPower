package dev.gigaherz.elementsofpower.spells;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.gigaherz.elementsofpower.spells.effects.*;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.world.effect.MobEffects;

public class SpellEffects
{
    private static final BiMap<String, SpellEffect> effectRegistry = HashBiMap.create();

    public static SpellEffect register(String name, SpellEffect effect)
    {
        effectRegistry.put(name, effect);
        return effect;
    }

    public static SpellEffect getEffect(String name)
    {
        return effectRegistry.get(name);
    }

    public static String getName(SpellEffect shape)
    {
        return effectRegistry.inverse().get(shape);
    }

    public static final SpellEffect BREAKING = register("breaking", new WitherEffect());
    public static final SpellEffect CUSHION = register("cushion", new CushionEffect());
    public static final SpellEffect DUST = register("dust", new DustEffect());
    public static final SpellEffect FLAME = register("flame", new FlameEffect());
    public static final SpellEffect FROST = register("frost", new FrostEffect());
    public static final SpellEffect HEALING = register("healing", new HealthEffect());
    public static final SpellEffect LAVA = register("lava", new LavaEffect(false));
    public static final SpellEffect LAVA_SOURCE = register("lava_source", new LavaEffect(true));
    public static final SpellEffect LIGHT = register("light", new LightEffect());
    public static final SpellEffect MINING = register("mining", new MiningEffect());
    public static final SpellEffect MIST = register("mist", new MistEffect());
    public static final SpellEffect RESURRECTION = register("resurrection", new ResurrectionEffect());
    public static final SpellEffect SLOWNESS = register("slowness", new ApplyPotionEffect(null, MobEffects.MOVEMENT_SLOWDOWN));
    public static final SpellEffect TELEPORT = register("teleport", new TeleportEffect());
    public static final SpellEffect WATER = register("water", new WaterEffect(false));
    public static final SpellEffect WATER_SOURCE = register("water_source", new WaterEffect(true));
    public static final SpellEffect WIND = register("wind", new WindEffect());
}

package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.spells.effects.*;
import net.minecraft.world.effect.MobEffects;

public class SpellEffects
{
    public static final SpellEffect BREAKING = new WitherEffect();
    public static final SpellEffect CUSHION = new CushionEffect();
    public static final SpellEffect DUST = new DustEffect();
    public static final SpellEffect FLAME = new FlameEffect();
    public static final SpellEffect FROST = new FrostEffect();
    public static final SpellEffect HEALING = new HealthEffect();
    public static final SpellEffect LAVA = new LavaEffect(false);
    public static final SpellEffect LAVA_SOURCE = new LavaEffect(true);
    public static final SpellEffect LIGHT = new LightEffect();
    public static final SpellEffect MINING = new MiningEffect();
    public static final SpellEffect MIST = new MistEffect();
    public static final SpellEffect RESURRECTION = new ResurrectionEffect();
    public static final SpellEffect SLOWNESS = new ApplyPotionEffect(null, MobEffects.MOVEMENT_SLOWDOWN);
    public static final SpellEffect TELEPORT = new TeleportEffect();
    public static final SpellEffect WATER = new WaterEffect(false);
    public static final SpellEffect WATER_SOURCE = new WaterEffect(true);
    public static final SpellEffect WIND = new WindEffect();
}

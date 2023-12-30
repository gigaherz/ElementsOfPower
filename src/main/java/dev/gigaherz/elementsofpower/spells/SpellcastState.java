package dev.gigaherz.elementsofpower.spells;


import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.misc.EntityInterceptor;
import dev.gigaherz.elementsofpower.network.SynchronizeSpellcastState;
import dev.gigaherz.elementsofpower.spells.effects.SpellEffect;
import dev.gigaherz.elementsofpower.spells.shapes.SpellShape;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpellcastState implements INBTSerializable<CompoundTag>
{
    private static final SpellcastState EMPTY = new SpellcastState();

    @Mod.EventBusSubscriber(modid=ElementsOfPowerMod.MODID, bus= Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents
    {
        @SubscribeEvent
        public static void playerJoinLevel(EntityJoinLevelEvent e)
        {
            if (e.getEntity() instanceof Player player)
                SpellcastState.get(player).setOwner(player);
        }

        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent e)
        {
            if (e.phase == TickEvent.Phase.END)
            {
                SpellcastState.get(e.player).updateSpell();
            }
        }

        @SubscribeEvent
        public static void playerTickEvent(LivingEvent.LivingJumpEvent e)
        {
            LivingEntity entity = e.getEntity();
            if (entity instanceof Player player)
            {
                SpellcastState.get(player).interrupt();
            }
        }
    }

    private Player player;

    @Nullable
    private Spellcast spellcast;

    public int remainingCastTime;
    public int remainingInterval;
    public int totalCastTime;


    // Rendering data;
    private Vec3 start;
    private Vec3 end;

    // Othjer data
    private RandomSource rand;

    private int currentSlot;

    private ItemStack currentItem = ItemStack.EMPTY;

    public static SpellcastState get(@Nullable Player p)
    {
        if (p == null)
            return SpellcastState.EMPTY;
        return p.getData(ElementsOfPowerMod.SPELLCAST_STATE);
    }

    public void setOwner(Player player)
    {
        this.player = player;
        this.rand = player.getRandom();
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        if (spellcast != null)
        {
            var cast = spellcast.serializeNBT();
            compound.put("currentSpell", cast);
            compound.putInt("remainingCastTime", remainingCastTime);
            compound.putInt("remainingInterval", remainingInterval);
            compound.putInt("totalCastTime", totalCastTime);
        }
        return compound;
    }

    public void deserializeNBT(CompoundTag compound)
    {
        if (compound.contains("currentSpell", Tag.TAG_COMPOUND))
        {
            CompoundTag cast = compound.getCompound("currentSpell");
            if (cast.contains("sequence", Tag.TAG_LIST))
            {
                spellcast = Spellcast.read(cast);
            }
        }
        remainingCastTime = compound.getInt("remainingCastTime");
        remainingInterval = compound.getInt("remainingInterval");
        totalCastTime = compound.getInt("totalCastTime");
    }

    public Player player()
    {
        return player;
    }

    public Level level()
    {
        return player.level();
    }

    public RandomSource getRandom()
    {
        return rand;
    }

    @Nullable
    public Spellcast spellcast()
    {
        return spellcast;
    }

    public boolean isCasting()
    {
        return spellcast != null;
    }

    public int damageForce()
    {
        if (spellcast == null)
            return 0;
        return Math.max(0, spellcast.power() - spellcast.effect().getForceModifier(this));
    }

    public int color()
    {
        if (spellcast == null)
            return 0xFFFFFFFF;
        return spellcast.effect().getColor(this);
    }

    public float scale()
    {
        return shape().getScale(this);
    }

    public SpellShape shape()
    {
        if (spellcast == null)
            return SpellShapes.SELF;
        return spellcast.shape();
    }

    public SpellEffect effect()
    {
        if (spellcast == null)
            return SpellEffects.NOOP;
        return spellcast.effect();
    }

    public int empowering()
    {
        if (spellcast == null)
            return 0;
        return spellcast.empowering();
    }

    public int radiating()
    {
        if (spellcast == null)
            return 0;
        return spellcast.radiating();
    }

    public void begin(Spellcast spellcast)
    {
        // If another spell was in progress, interrupt first
        interrupt();

        this.spellcast = spellcast;

        if (spellcast.shape().isInstant())
        {
            remainingCastTime = spellcast.shape().getInstantAnimationLength();
            remainingInterval = 0;
        }
        else
        {
            remainingCastTime = spellcast.effect().getDuration(this);
            remainingInterval = spellcast.effect().getInterval(this);
        }
        totalCastTime = remainingCastTime;

        sync(SynchronizeSpellcastState.ChangeMode.BEGIN);
    }

    public void end()
    {
        sync(SynchronizeSpellcastState.ChangeMode.END);

        spellcast = null;
    }

    public void interrupt()
    {
        sync(SynchronizeSpellcastState.ChangeMode.INTERRUPT);

        spellcast = null;
    }

    public void cancel()
    {
        sync(SynchronizeSpellcastState.ChangeMode.CANCEL);

        spellcast = null;
    }

    private void sync(SynchronizeSpellcastState.ChangeMode mode)
    {
        if (spellcast != null && !player.level().isClientSide)
        {
            ElementsOfPowerMod.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SynchronizeSpellcastState(mode, player, spellcast, remainingCastTime, remainingInterval, totalCastTime));
        }
    }

    public void updateSpell()
    {
        boolean shouldCancel = false;
        int newSlot = player.getInventory().selected;
        if (newSlot != currentSlot)
        {
            shouldCancel = true;
            currentSlot = newSlot;
        }
        else
        {
            ItemStack newItem = player.getInventory().getSelected();
            if (!ItemStack.isSameItem(currentItem, newItem))
            {
                shouldCancel = true;
            }
            currentItem = newItem;
        }

        if (spellcast != null)
        {
            if (shouldCancel)
            {
                cancel();
            }
            else
            {


                if (spellcast.shape().isInstant() && remainingCastTime == totalCastTime)
                {
                    if (!player.level().isClientSide)
                    {
                        spellcast.shape().spellTick(this);
                    }
                }

                remainingCastTime--;

                if (!spellcast.shape().isInstant())
                {
                    remainingInterval--;

                    if (remainingInterval <= 0)
                    {
                        remainingInterval = spellcast.effect().getInterval(this);

                        if (!player.level().isClientSide)
                        {
                            spellcast.shape().spellTick(this);
                        }
                    }
                }

                if (remainingCastTime <= 0)
                {
                    end();
                }
            }
        }
    }

    public void onSync(SynchronizeSpellcastState.ChangeMode changeMode, @Nullable Spellcast spellcast, int remainingCastTime, int remainingInterval, int totalCastTime)
    {
        this.remainingCastTime = remainingCastTime;
        this.remainingInterval = remainingInterval;
        this.totalCastTime = totalCastTime;
        switch (changeMode)
        {
            case BEGIN ->
                // TODO: Begin cast animation?
                    begin(Objects.requireNonNull(spellcast));
            case END ->
                // TODO: End particles?
                    end();
            case INTERRUPT ->
                // TODO: Interrupt particles?
                    interrupt();
            case CANCEL ->
                // TODO: Cancel particles?
                    cancel();
        }
    }

    public void onImpact(HitResult mop, RandomSource rand, Entity directEntity)
    {
        this.rand = rand;
        if (spellcast != null && !player.level().isClientSide)
        {
            spellcast.shape().onImpact(this, mop, directEntity);
        }
    }

    public float getRandomForParticle()
    {
        return (getRandom().nextFloat() - 0.5f) * spellcast.power() / 8.0f;
    }

    public void spawnRandomParticle(ParticleOptions type, double x, double y, double z)
    {
        player.level().addParticle(type, x, y, z, getRandomForParticle(), getRandomForParticle(), getRandomForParticle());
    }

    // Called by the client on render, and by the server as needed
    @Nullable
    public HitResult getHitPosition()
    {
        return getHitPosition(1);
    }

    @Nullable
    public HitResult getHitPosition(float partialTicks)
    {
        float maxDistance = 10;

        calculateStartPosition(partialTicks);

        Vec3 look = player.getViewVector(partialTicks);
        end = start.add(look.x * maxDistance, look.y * maxDistance, look.z * maxDistance);

        // FIXME
        BlockHitResult blockTrace = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        HitResult trace = EntityInterceptor.getEntityIntercept(player, player.level(), start, look, end, blockTrace);

        if (trace != null && trace.getType() != HitResult.Type.MISS)
        {
            end = trace.getLocation();
        }

        return trace;
    }

    public Vec3 calculateStartPosition(float partialTicks)
    {
        if (partialTicks < 1)
        {
            double sx = player.xo + (player.getX() - player.xo) * partialTicks;
            double sy = player.yo + (player.getY() - player.yo) * partialTicks + player.getEyeHeight();
            double sz = player.zo + (player.getZ() - player.zo) * partialTicks;
            start = new Vec3(sx, sy, sz);
        }
        else
        {
            start = player.getEyePosition(1.0f);
        }
        return start;
    }

    public Vec3 getStart()
    {
        return start;
    }

    public Vec3 getEnd()
    {
        return end;
    }
}

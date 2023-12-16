package dev.gigaherz.elementsofpower.spells;


import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.network.SynchronizeSpellcastState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class SpellcastEntityData implements INBTSerializable<CompoundTag>
{
    public static EntityCapability<SpellcastEntityData, Void> CAPABILITY = EntityCapability.createVoid(ElementsOfPowerMod.location("spellcast_data"), SpellcastEntityData.class);

    @Mod.EventBusSubscriber(modid=ElementsOfPowerMod.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents
    {
        @SubscribeEvent
        public static void registerCapabilities (RegisterCapabilitiesEvent event)
        {
            event.registerEntity(
                    CAPABILITY,
                    EntityType.PLAYER,
                    (entity, context) -> new SpellcastEntityData(entity)
            );
        }
    }

    @Mod.EventBusSubscriber(modid=ElementsOfPowerMod.MODID, bus= Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents
    {
        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent e)
        {
            if (e.phase == TickEvent.Phase.END)
            {
                var spellcast = SpellcastEntityData.get(e.player);
                if (spellcast != null)
                    spellcast.updateSpell();
            }
        }

        @SubscribeEvent
        public static void playerTickEvent(LivingEvent.LivingJumpEvent e)
        {
            LivingEntity entity = e.getEntity();
            if (entity instanceof Player)
            {
                var spellcast = SpellcastEntityData.get((Player) entity);
                if (spellcast != null)
                    spellcast.interrupt();
            }
        }
    }

    private final Player player;
    private InitializedSpellcast currentCasting;

    @Nullable
    public static SpellcastEntityData get(Player p)
    {
        return p.getCapability(CAPABILITY);
    }

    public SpellcastEntityData(Entity entity)
    {
        this.player = (Player) entity;
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag compound = new CompoundTag();
        if (currentCasting != null)
        {
            CompoundTag cast = new CompoundTag();
            currentCasting.write(cast);
            cast.put("sequence", currentCasting.getSequenceNBT());
            compound.put("currentSpell", cast);
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
                ListTag seq = cast.getList("sequence", Tag.TAG_STRING);
                Spellcast ccast = SpellManager.makeSpell(seq);
                if (ccast != null)
                {
                    currentCasting = ccast.init(player.level(), player);
                    currentCasting.read(cast);
                }
            }
        }
    }

    public void begin(InitializedSpellcast spell)
    {
        // If another spell was in progress, interrupt first
        interrupt();

        currentCasting = spell;
        currentCasting.init(player.level(), player);

        sync(SynchronizeSpellcastState.ChangeMode.BEGIN);
    }

    public void end()
    {
        sync(SynchronizeSpellcastState.ChangeMode.END);

        currentCasting = null;
    }

    public void interrupt()
    {
        sync(SynchronizeSpellcastState.ChangeMode.INTERRUPT);

        currentCasting = null;
    }

    public void cancel()
    {
        sync(SynchronizeSpellcastState.ChangeMode.CANCEL);

        currentCasting = null;
    }

    private void sync(SynchronizeSpellcastState.ChangeMode mode)
    {
        if (currentCasting != null && !player.level().isClientSide)
        {
            ElementsOfPowerMod.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SynchronizeSpellcastState(mode, currentCasting));
        }
    }

    private int currentSlot;

    private ItemStack currentItem = ItemStack.EMPTY;

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

        if (currentCasting != null)
        {
            if (shouldCancel)
                cancel();
            else
                currentCasting.update();
        }
    }

    public void onSync(SynchronizeSpellcastState.ChangeMode changeMode, InitializedSpellcast cast)
    {
        switch (changeMode)
        {
            case BEGIN:
                // TODO: Begin cast animation?
                begin(cast);
                break;

            case END:
                // TODO: End particles?
                end();
                break;

            case INTERRUPT:
                // TODO: Interrupt particles?
                interrupt();
                break;

            case CANCEL:
                // TODO: Cancel particles?
                cancel();
                break;
        }
    }

    @Nullable
    public InitializedSpellcast getCurrentCasting()
    {
        return currentCasting;
    }

}

package dev.gigaherz.elementsofpower.spells;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.network.SynchronizeSpellcastState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

public class SpellcastEntityData implements INBTSerializable<CompoundTag>
{
    private static final ResourceLocation PROP_KEY = ElementsOfPowerMod.location("spellcast_data");

    private final Player player;
    private InitializedSpellcast currentCasting;

    public static LazyOptional<SpellcastEntityData> get(Player p)
    {
        return p.getCapability(Handler.SPELLCAST, Direction.UP);
    }

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
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
            currentCasting.writeToNBT(cast);
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
                    currentCasting = ccast.init(player.level, player);
                    currentCasting.readFromNBT(cast);
                }
            }
        }
    }

    public void begin(InitializedSpellcast spell)
    {
        // If another spell was in progress, interrupt first
        interrupt();

        currentCasting = spell;
        currentCasting.init(player.level, player);

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
        if (currentCasting != null && !player.level.isClientSide)
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
            if (!ItemStack.isSame(currentItem, newItem))
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

    public static class Handler
    {
        public static Capability<SpellcastEntityData> SPELLCAST = CapabilityManager.get(new CapabilityToken<>() {});

        // FIXME
        public void registerCapability(RegisterCapabilitiesEvent event)
        {
            event.register(SpellcastEntityData.class);
        }

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> e)
        {
            final Entity entity = e.getObject();

            if (entity instanceof Player)
            {
                e.addCapability(PROP_KEY, new ICapabilitySerializable<CompoundTag>()
                {
                    final SpellcastEntityData cap = new SpellcastEntityData(entity);
                    final LazyOptional<SpellcastEntityData> capGetter = LazyOptional.of(() -> cap);

                    @Override
                    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
                    {
                        if (capability == SPELLCAST)
                            return capGetter.cast();
                        return LazyOptional.empty();
                    }

                    @Override
                    public CompoundTag serializeNBT()
                    {
                        return cap.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt)
                    {
                        cap.deserializeNBT(nbt);
                    }
                });
            }
        }

        @SubscribeEvent
        public void playerTickEvent(TickEvent.PlayerTickEvent e)
        {
            if (e.phase == TickEvent.Phase.END)
            {
                SpellcastEntityData.get(e.player).ifPresent(SpellcastEntityData::updateSpell);
            }
        }

        @SubscribeEvent
        public void playerTickEvent(LivingEvent.LivingJumpEvent e)
        {
            LivingEntity entity = e.getEntity();
            if (entity instanceof Player)
            {
                SpellcastEntityData.get((Player) entity).ifPresent(SpellcastEntityData::interrupt);
            }
        }
    }
}

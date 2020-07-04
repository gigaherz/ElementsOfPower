package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.network.SynchronizeSpellcastState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public class SpellcastEntityData implements INBTSerializable<CompoundNBT>
{
    private static final ResourceLocation PROP_KEY = ElementsOfPowerMod.location("spellcast_data");

    private final PlayerEntity player;
    private InitializedSpellcast currentCasting;

    public static LazyOptional<SpellcastEntityData> get(PlayerEntity p)
    {
        return p.getCapability(Handler.SPELLCAST, Direction.UP);
    }

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    public SpellcastEntityData(Entity entity)
    {
        this.player = (PlayerEntity) entity;
    }

    public CompoundNBT serializeNBT()
    {
        CompoundNBT compound = new CompoundNBT();
        if (currentCasting != null)
        {
            CompoundNBT cast = new CompoundNBT();
            currentCasting.writeToNBT(cast);
            cast.put("sequence", currentCasting.getSequenceNBT());
            compound.put("currentSpell", cast);
        }
        return compound;
    }

    public void deserializeNBT(CompoundNBT compound)
    {
        if (compound.contains("currentSpell", Constants.NBT.TAG_COMPOUND))
        {
            CompoundNBT cast = compound.getCompound("currentSpell");
            if (cast.contains("sequence", Constants.NBT.TAG_LIST))
            {
                ListNBT seq = cast.getList("sequence", Constants.NBT.TAG_STRING);
                Spellcast ccast = SpellManager.makeSpell(seq);
                if (ccast != null)
                {
                    currentCasting = ccast.init(player.world, player);
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
        currentCasting.init(player.world, player);

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
        if (currentCasting != null && !player.world.isRemote)
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
        int newSlot = player.inventory.currentItem;
        if (newSlot != currentSlot)
        {
            shouldCancel = true;
            currentSlot = newSlot;
        }
        else
        {
            ItemStack newItem = player.inventory.getCurrentItem();
            if (!ItemStack.areItemsEqual(currentItem, newItem))
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
        @CapabilityInject(SpellcastEntityData.class)
        public static Capability<SpellcastEntityData> SPELLCAST;

        public Handler()
        {
            CapabilityManager.INSTANCE.register(SpellcastEntityData.class, new Capability.IStorage<SpellcastEntityData>()
            {
                @Override
                public INBT writeNBT(Capability<SpellcastEntityData> capability, SpellcastEntityData instance, @Nullable Direction side)
                {
                    return instance.serializeNBT();
                }

                @Override
                public void readNBT(Capability<SpellcastEntityData> capability, SpellcastEntityData instance, @Nullable Direction side, INBT nbt)
                {
                    if (nbt instanceof CompoundNBT)
                        instance.deserializeNBT((CompoundNBT) nbt);
                }
            }, () -> null);
        }

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> e)
        {
            final Entity entity = e.getObject();

            if (entity instanceof PlayerEntity)
            {
                e.addCapability(PROP_KEY, new ICapabilitySerializable<CompoundNBT>()
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
                    public CompoundNBT serializeNBT()
                    {
                        return cap.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundNBT nbt)
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
            LivingEntity entity = e.getEntityLiving();
            if (entity instanceof PlayerEntity)
            {
                SpellcastEntityData.get((PlayerEntity) entity).ifPresent(SpellcastEntityData::interrupt);
            }
        }
    }
}

package gigaherz.elementsofpower.spells;

import com.google.common.collect.Lists;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.network.SpellcastSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;

public class SpellcastEntityData implements INBTSerializable<NBTTagCompound>
{
    private static final ResourceLocation PROP_KEY = ElementsOfPower.location("SpellcastData");

    private final EntityPlayer player;
    private Spellcast currentCasting;

    public static SpellcastEntityData get(EntityPlayer p)
    {
        return p.getCapability(Handler.SPELLCAST, EnumFacing.UP);
    }

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    public SpellcastEntityData(Entity entity)
    {
        this.player = (EntityPlayer) entity;
    }

    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        if (currentCasting != null)
        {
            NBTTagCompound cast = new NBTTagCompound();
            currentCasting.writeToNBT(cast);
            cast.setString("sequence", currentCasting.getSequence());
            compound.setTag("currentSpell", cast);
        }
        return compound;
    }

    public void deserializeNBT(NBTTagCompound compound)
    {
        if (compound.hasKey("currentSpell", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound cast = (NBTTagCompound) compound.getTag("currentSpell");
            String sequence = cast.getString("sequence");

            currentCasting = SpellManager.makeSpell(sequence);
            if (currentCasting != null)
            {
                currentCasting.init(player.world, player);
                currentCasting.readFromNBT(cast);
            }
        }
    }

    public void begin(Spellcast spell)
    {
        // If another spell was in progress, interrupt first
        interrupt();

        currentCasting = spell;
        currentCasting.init(player.world, player);

        if (!player.world.isRemote)
            ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.BEGIN, spell),
                    new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));
    }

    public void end()
    {
        if (currentCasting != null)
        {
            if (!player.world.isRemote)
                ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.END, currentCasting),
                        new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));

            currentCasting = null;
        }
    }

    public void interrupt()
    {
        if (currentCasting != null)
        {
            if (!player.world.isRemote)
                ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.INTERRUPT, currentCasting),
                        new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));

            currentCasting = null;
        }
    }

    public void cancel()
    {
        if (currentCasting != null)
        {
            if (!player.world.isRemote)
                ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.CANCEL, currentCasting),
                        new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));

            currentCasting = null;
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

    public void sync(SpellcastSync.ChangeMode changeMode, Spellcast cast)
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
    public Spellcast getCurrentCasting()
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
                public NBTBase writeNBT(Capability<SpellcastEntityData> capability, SpellcastEntityData instance, @Nullable EnumFacing side)
                {
                    return null;
                }

                @Override
                public void readNBT(Capability<SpellcastEntityData> capability, SpellcastEntityData instance, @Nullable EnumFacing side, NBTBase nbt)
                {

                }
            }, () -> null);
        }

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> e)
        {
            final Entity entity = e.getObject();

            if (entity instanceof EntityPlayer)
            {
                e.addCapability(PROP_KEY, new ICapabilitySerializable<NBTTagCompound>()
                {
                    final SpellcastEntityData cap = new SpellcastEntityData(entity);

                    @Override
                    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
                    {
                        return capability == SPELLCAST;
                    }

                    @Override
                    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
                    {
                        if (capability == SPELLCAST)
                            return SPELLCAST.cast(cap);
                        return null;
                    }

                    @Override
                    public NBTTagCompound serializeNBT()
                    {
                        return cap.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(NBTTagCompound nbt)
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
                SpellcastEntityData data = SpellcastEntityData.get(e.player);
                data.updateSpell();
            }
        }
    }
}

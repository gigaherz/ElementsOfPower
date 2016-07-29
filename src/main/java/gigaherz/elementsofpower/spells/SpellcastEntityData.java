package gigaherz.elementsofpower.spells;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.network.SpellcastSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class SpellcastEntityData
{
    public static final ResourceLocation PROP_KEY = ElementsOfPower.location("SpellcastData");

    EntityPlayer player;
    World world;
    Spellcast currentCasting;

    public static SpellcastEntityData get(EntityPlayer p)
    {
        return p.getCapability(Handler.SPELLCAST, EnumFacing.UP);
    }

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    public void saveNBTData(NBTTagCompound compound)
    {
        if (currentCasting != null)
        {
            NBTTagCompound cast = new NBTTagCompound();
            currentCasting.writeToNBT(cast);
            cast.setString("sequence", currentCasting.getSequence());
            compound.setTag("currentSpell", cast);
        }
    }

    public void loadNBTData(NBTTagCompound compound)
    {
        if (compound.hasKey("currentSpell", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound cast = (NBTTagCompound) compound.getTag("currentSpell");
            String sequence = cast.getString("sequence");

            currentCasting = SpellManager.makeSpell(sequence);
            currentCasting.init(world, player);
            currentCasting.readFromNBT(cast);
        }
    }

    public void init(Entity entity, World world)
    {
        this.player = (EntityPlayer) entity;
        this.world = world;
    }

    public void begin(Spellcast spell)
    {
        // If another spell was in progress, interrupt first
        interrupt();

        currentCasting = spell;
        currentCasting.init(world, player);

        if (!world.isRemote)
            ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.BEGIN, spell),
                    new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));
    }

    public void end()
    {
        if (currentCasting != null)
        {
            if (!world.isRemote)
                ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.END, currentCasting),
                        new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));

            currentCasting = null;
        }
    }

    public void interrupt()
    {
        if (currentCasting != null)
        {
            if (!world.isRemote)
                ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.INTERRUPT, currentCasting),
                        new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));

            currentCasting = null;
        }
    }

    public void cancel()
    {
        if (currentCasting != null)
        {
            if (!world.isRemote)
                ElementsOfPower.channel.sendToAllAround(new SpellcastSync(SpellcastSync.ChangeMode.CANCEL, currentCasting),
                        new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 128));

            currentCasting = null;
        }
    }

    int currentSlot;
    ItemStack currentItem;

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
            if (!ItemStack.areItemsEqual(currentItem, newItem) || currentItem == null)
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
                public NBTBase writeNBT(Capability<SpellcastEntityData> capability, SpellcastEntityData instance, EnumFacing side)
                {
                    return null;
                }

                @Override
                public void readNBT(Capability<SpellcastEntityData> capability, SpellcastEntityData instance, EnumFacing side, NBTBase nbt)
                {

                }
            }, () -> null);
        }

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent.Entity e)
        {
            final Entity entity = e.getEntity();

            if (entity instanceof EntityPlayer)
            {
                e.addCapability(PROP_KEY, new ICapabilitySerializable<NBTTagCompound>()
                {
                    SpellcastEntityData cap = new SpellcastEntityData();

                    {
                        cap.init(entity, entity.worldObj);
                    }

                    @Override
                    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                    {
                        return capability == SPELLCAST;
                    }

                    @Override
                    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                    {
                        if (capability == SPELLCAST)
                            return SPELLCAST.cast(cap);
                        return null;
                    }

                    @Override
                    public NBTTagCompound serializeNBT()
                    {
                        NBTTagCompound tag = new NBTTagCompound();
                        cap.saveNBTData(tag);
                        return tag;
                    }

                    @Override
                    public void deserializeNBT(NBTTagCompound nbt)
                    {
                        cap.loadNBTData(nbt);
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

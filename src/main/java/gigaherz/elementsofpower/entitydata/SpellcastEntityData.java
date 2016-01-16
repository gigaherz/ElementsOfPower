package gigaherz.elementsofpower.entitydata;

import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.SpellManager;
import gigaherz.elementsofpower.network.SpellcastSync;
import gigaherz.elementsofpower.spells.Spell;
import gigaherz.elementsofpower.spells.cast.Spellcast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class SpellcastEntityData implements IExtendedEntityProperties
{
    public static final String PROP_NAME = ElementsOfPower.MODID + "_SpellcastData";

    EntityPlayer player;
    World world;
    Spellcast currentCasting;

    public static SpellcastEntityData get(EntityPlayer p)
    {
        return (SpellcastEntityData) p.getExtendedProperties(PROP_NAME);
    }

    public static void register()
    {
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        if (currentCasting != null)
        {
            NBTTagCompound cast = new NBTTagCompound();
            currentCasting.writeToNBT(cast);
            cast.setString("sequence", currentCasting.getEffect().getSequence());
            compound.setTag("currentSpell", cast);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        if (compound.hasKey("currentSpell", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound cast = (NBTTagCompound) compound.getTag("currentSpell");
            String sequence = cast.getString("sequence");

            Spell ef = SpellManager.findSpell(sequence);

            currentCasting = ef.getNewCast();
            currentCasting.init(world, player);
            currentCasting.readFromNBT(cast);
        }
    }

    @Override
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
        if(newSlot != currentSlot)
        {
            shouldCancel = true;
            currentSlot = newSlot;
        }
        else
        {
            if(!ItemStack.areItemsEqual(currentItem, player.inventory.getCurrentItem()) || currentItem == null)
            {
                shouldCancel = true;
            }
        }

        if (currentCasting != null)
        {
            if(shouldCancel)
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
        @SubscribeEvent
        public void entityConstruct(EntityEvent.EntityConstructing e)
        {
            if (e.entity instanceof EntityPlayer)
            {
                if (e.entity.getExtendedProperties(PROP_NAME) == null)
                    e.entity.registerExtendedProperties(PROP_NAME, new SpellcastEntityData());
            }
        }

        @SubscribeEvent
        public void playerTickEvent(TickEvent.PlayerTickEvent e)
        {
            if (e.phase == TickEvent.Phase.END)
            {
                SpellcastEntityData data = SpellcastEntityData.get(e.player);
                if (data != null)
                    data.updateSpell();
            }
        }
    }
}

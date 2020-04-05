package gigaherz.elementsofpower.items;

import gigaherz.elementsofpower.capabilities.IMagicContainer;
import gigaherz.elementsofpower.capabilities.MagicContainerCapability;
import gigaherz.elementsofpower.client.WandUseManager;
import gigaherz.elementsofpower.database.MagicAmounts;
import gigaherz.elementsofpower.integration.Curios;
import gigaherz.elementsofpower.network.UpdateSpellSequence;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.spells.SpellManager;
import gigaherz.elementsofpower.spells.Spellcast;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class WandItem extends GemContainerItem
{
    public static final String SPELL_SEQUENCE_TAG = "SpellSequence";

    public WandItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            if (playerIn.isShiftKeyDown())
            {
                CompoundNBT tag = itemStackIn.getTag();
                if (tag != null)
                    tag.remove(WandItem.SPELL_SEQUENCE_TAG);
            }
        }

        if (hand == Hand.MAIN_HAND)
        {
            if (worldIn.isRemote)
            {
                beginTracking(playerIn, hand);
            }

            playerIn.setActiveHand(hand);
            return ActionResult.func_226248_a_(itemStackIn);
        }

        return ActionResult.func_226251_d_(itemStackIn);
    }

    public static void beginTracking(PlayerEntity playerIn, Hand hand)
    {
        WandUseManager.instance.handInUse = hand;
    }

    public boolean onSpellCommit(ItemStack stack, PlayerEntity player, @Nullable List<Element> sequence)
    {
        final boolean updateSequenceOnWand;
        if (sequence == null)
        {
            updateSequenceOnWand = false;
            CompoundNBT tag = stack.getTag();
            if (tag != null)
            {
                ListNBT seq = tag.getList(WandItem.SPELL_SEQUENCE_TAG, Constants.NBT.TAG_STRING);
                sequence = SpellManager.sequenceFromList(seq);
            }
        }
        else
        {
            updateSequenceOnWand = true;
        }

        if (sequence == null || sequence.size() == 0)
            return false;

        final Spellcast cast = SpellManager.makeSpell(sequence);
        if (cast == null)
        {
            player.sendStatusMessage(new TranslationTextComponent("text.elementsofpower.spell.invalid_sequence"), true);
            return false;
        }

        return MagicContainerCapability.getContainer(stack).map(magic -> {

            MagicAmounts amounts = magic.getContainedMagic().add(getTotalPlayerReservoir(player));
            MagicAmounts cost = cast.getSpellCost();

            if (!magic.isInfinite() && !amounts.hasEnough(cost))
            {
                player.sendStatusMessage(new TranslationTextComponent("text.elementsofpower.spell.cost_too_high"), true);
                return updateSequenceOnWand;
            }

            Spellcast cast2 = cast.getShape().castSpell(stack, player, cast);
            if (cast2 != null)
            {
                SpellcastEntityData.get(player).ifPresent(data -> data.begin(cast2));
            }

            // TODO: Subtract from reservoir if needed
            if (!magic.isInfinite())
            {
                cost = subtractFromReservoir(player, cost);
                if (!cost.isEmpty())
                {
                    amounts = amounts.subtract(cost);
                    magic.setContainedMagic(amounts);
                }
            }

            //DiscoveryHandler.instance.onSpellcast(player, cast);
            return updateSequenceOnWand;
        }).orElse(false);
    }

    public static MagicAmounts getTotalPlayerReservoir(PlayerEntity player)
    {
        MagicAmounts.Accumulator accumulator = MagicAmounts.builder();

        for (int i = 0; i < player.inventory.getSizeInventory(); i++)
        {
            accumulateReservoir(accumulator, player.inventory.getStackInSlot(i));
        }

        Curios.getCurios(player).forEach(value -> {
            for (int i = 0; i < value.getSlots(); i++)
            {
                accumulateReservoir(accumulator, value.getStackInSlot(i));
            }
        });

        return accumulator.toAmounts();
    }

    private static void accumulateReservoir(MagicAmounts.Accumulator accumulator, ItemStack stack)
    {
        if (stack.getCount() > 0)
        {
            Item item = stack.getItem();

            if (item instanceof BaubleItem && ((BaubleItem) item).getTransferMode(stack) == BaubleItem.TransferMode.PASSIVE)
            {
                MagicAmounts contained = MagicContainerCapability.getContainer(stack).map(IMagicContainer::getContainedMagic).orElse(MagicAmounts.EMPTY);
                accumulator.add(contained);
            }
        }
    }

    public static MagicAmounts subtractFromReservoir(PlayerEntity player, MagicAmounts cost)
    {
        MagicAmounts.Accumulator accumulator = MagicAmounts.builder();

        accumulator.add(cost);

        for (int i = 0; i < player.inventory.getSizeInventory(); i++)
        {
            subtractFromReservoir(accumulator, player.inventory.getStackInSlot(i));
        }

        Curios.getCurios(player).forEach(value -> {
            for (int i = 0; i < value.getSlots(); i++)
            {
                subtractFromReservoir(accumulator, value.getStackInSlot(i));
            }
        });

        return accumulator.toAmounts();
    }

    private static void subtractFromReservoir(MagicAmounts.Accumulator accumulator, ItemStack stack)
    {
        if (stack.getCount() > 0)
        {
            Item item = stack.getItem();

            if (item instanceof BaubleItem && ((BaubleItem) item).getTransferMode(stack) == BaubleItem.TransferMode.PASSIVE)
            {
                MagicContainerCapability.getContainer(stack).ifPresent(magic -> {

                    MagicAmounts contained = magic.getContainedMagic();
                    MagicAmounts required = accumulator.toAmounts();
                    MagicAmounts toSubtract = MagicAmounts.min(contained, required);
                    if (!toSubtract.isEmpty())
                    {
                        MagicAmounts remainder = contained.subtract(toSubtract);
                        magic.setContainedMagic(remainder);
                        accumulator.subtract(toSubtract);
                    }
                });
            }
        }
    }

    public void processSequenceUpdate(UpdateSpellSequence message, ItemStack stack, PlayerEntity player)
    {
        if (message.changeMode == UpdateSpellSequence.ChangeMode.COMMIT)
        {
            CompoundNBT nbt = stack.getOrCreateTag();

            if (onSpellCommit(stack, player, message.sequence))
            {
                nbt.put(WandItem.SPELL_SEQUENCE_TAG, SpellManager.sequenceToList(message.sequence));
            }
        }
    }
}
package dev.gigaherz.elementsofpower.analyzer;

import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerContainer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class AnalyzerItem extends Item
{
    public AnalyzerItem(Properties properties)
    {
        super(properties);
    }

    // Grumbles at getSlotFor being client-only
    public static int getSlotIndex(Inventory inv, ItemStack stack)
    {
        for (int i = 0; i < inv.items.size(); ++i)
        {
            ItemStack stack2 = inv.items.get(i);
            if (!inv.items.get(i).isEmpty()
                    && stack.getItem() == stack2.getItem()
                    && ItemStack.tagMatches(stack, stack2))
            {
                return i;
            }
        }

        return -1;
    }

    private void openGui(ServerPlayer playerIn, ItemStack heldItem)
    {
        int slot = getSlotIndex(playerIn.getInventory(), heldItem);
        NetworkHooks.openGui(playerIn, new SimpleMenuProvider((id, playerInventory, player) -> new AnalyzerContainer(id, playerInventory, slot),
                        new TranslatableComponent("container.elementsofpower.analyzer")),
                (packetBuffer) -> packetBuffer.writeInt(slot));
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (context.getPlayer() == null)
            return InteractionResult.PASS;

        if (!context.getLevel().isClientSide)
            openGui((ServerPlayer) context.getPlayer(), context.getItemInHand());

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand)
    {
        if (!worldIn.isClientSide)
            openGui((ServerPlayer) playerIn, playerIn.getItemInHand(hand));

        return InteractionResultHolder.success(playerIn.getItemInHand(hand));
    }
}

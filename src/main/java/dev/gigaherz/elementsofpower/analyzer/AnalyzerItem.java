package dev.gigaherz.elementsofpower.analyzer;

import dev.gigaherz.elementsofpower.analyzer.menu.AnalyzerMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public class AnalyzerItem extends BlockItem
{
    public AnalyzerItem(Block block, Properties properties)
    {
        super(block, properties);
    }

    // Grumbles at getSlotFor being client-only
    public static int getSlotIndex(Inventory inv, ItemStack stack)
    {
        NonNullList<ItemStack> items = inv.items;
        for (int i = 0; i < items.size(); ++i)
        {
            ItemStack stack2 = items.get(i);
            if (stack2 == stack)
                return i;
        }
        return -1;
    }

    private void openGui(ServerPlayer player, ItemStack heldItem)
    {
        int slot = getSlotIndex(player.getInventory(), heldItem);
        AnalyzerMenu.openAnalyzer(player, slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        var stack = player.getItemInHand(hand);

        if (level.isClientSide)
            return InteractionResultHolder.success(stack);

        if (player instanceof FakePlayer)
            return InteractionResultHolder.fail(stack);

        if (!(player instanceof ServerPlayer sp))
            return InteractionResultHolder.fail(stack);

        openGui(sp, stack);

        return InteractionResultHolder.success(stack);
    }
}

package gigaherz.elementsofpower.analyzer;

import gigaherz.elementsofpower.analyzer.gui.ContainerAnalyzer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemAnalyzer extends Item
{
    public ItemAnalyzer(Properties properties)
    {
        super(properties);
    }

    private void openGui(ServerPlayerEntity playerIn, ItemStack heldItem)
    {
        int slot = playerIn.inventory.getSlotFor(heldItem);
        NetworkHooks.openGui(playerIn, new SimpleNamedContainerProvider((id, playerInventory, player) -> new ContainerAnalyzer(id, playerInventory, slot),
                        new TranslationTextComponent("container.elementsofpower.analyzer")),
                (packetBuffer) -> packetBuffer.writeInt(slot));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        if (context.getPlayer() == null)
            return ActionResultType.PASS;

        if (!context.getWorld().isRemote)
            openGui((ServerPlayerEntity)context.getPlayer(), context.getItem());

        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
    {
        if (!worldIn.isRemote)
            openGui((ServerPlayerEntity)playerIn, playerIn.getHeldItem(hand));

        return ActionResult.func_226248_a_(playerIn.getHeldItem(hand));
    }
}

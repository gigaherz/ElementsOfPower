package dev.gigaherz.elementsofpower.progression;

/*
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.essentializer.BlockEssentializer;
import gigaherz.elementsofpower.items.ItemRing;
import gigaherz.elementsofpower.items.ItemStaff;
import gigaherz.elementsofpower.items.ItemWand;
import Spellcast;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class DiscoveryHandler
{
    public static final DiscoveryHandler instance = new DiscoveryHandler();

    static AchievementPage achievementPage;
    static Achievement discoverGems;
    static Achievement acquireRing;
    static Achievement acquireWand;
    static Achievement acquireStaff;
    static Achievement essentializing;
    static Achievement firstSpell;
    static Achievement advancedSpell;
    static Achievement masterSpell;

    final static NonNullList<ItemStack> gemTypes = NonNullList.create();

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(instance);

        // Prepare item comparisons
        gemTypes.add(new ItemStack(Items.EMERALD));
        gemTypes.add(new ItemStack(Items.DIAMOND));
        ElementsOfPower.gemstone.getUnexamined(gemTypes);
        ElementsOfPower.gemstone.getSubItems(ElementsOfPower.gemstone, CreativeTabs.SEARCH, gemTypes);

        // Initialize achievements
        discoverGems = newAchievement(".discoverGems", -3, 0, Items.DIAMOND, null);
        discoverGems.initIndependentStat().registerStat();

        acquireWand = newAchievement(".acquireWand", 0, 0, new ItemStack(ElementsOfPower.wand), discoverGems);
        acquireWand.registerStat();

        acquireRing = newAchievement(".acquireRing", 1, -2, new ItemStack(ElementsOfPower.ring), acquireWand);
        acquireRing.registerStat();

        acquireStaff = newAchievement(".acquireStaff", 2, 0, new ItemStack(ElementsOfPower.staff), acquireWand);
        acquireStaff.registerStat();

        essentializing = newAchievement(".essentializing", 4, 0, ElementsOfPower.essentializer, acquireStaff);
        essentializing.setSpecial().registerStat();

        firstSpell = newAchievement(".firstSpell", 0, 2, new ItemStack(ElementsOfPower.orb, 1, 2), acquireWand);
        firstSpell.registerStat();

        advancedSpell = newAchievement(".advancedSpell", 1, 3, new ItemStack(ElementsOfPower.orb, 1, 1), firstSpell);
        advancedSpell.registerStat();

        masterSpell = newAchievement(".masterSpell", 3, 3, new ItemStack(ElementsOfPower.orb, 1, 0), advancedSpell);
        masterSpell.setSpecial().registerStat();

        achievementPage = new AchievementPage("Elements of Power",
                discoverGems, acquireRing, acquireWand, acquireStaff, essentializing,
                firstSpell, advancedSpell, masterSpell);
        AchievementPage.registerAchievementPage(achievementPage);
    }

    public static Achievement newAchievement(String baseName, int column, int row, Item item, Achievement parent)
    {
        String achvName = ElementsOfPower.MODID + baseName;
        String achvId = "achievement." + achvName;
        return new Achievement(achvId, achvName, column, row, item, parent);
    }

    public static Achievement newAchievement(String baseName, int column, int row, Block block, Achievement parent)
    {
        String achvName = ElementsOfPower.MODID + baseName;
        String achvId = "achievement." + achvName;
        return new Achievement(achvId, achvName, column, row, block, parent);
    }

    public static Achievement newAchievement(String baseName, int column, int row, ItemStack stack, Achievement parent)
    {
        String achvName = ElementsOfPower.MODID + baseName;
        String achvId = "achievement." + achvName;
        return new Achievement(achvId, achvName, column, row, stack, parent);
    }

    @SubscribeEvent
    public void onItemPickUp(EntityItemPickupEvent event)
    {
        if (event.getEntityPlayer().world.isRemote)
            return;

        checkItem(event.getEntityPlayer(), event.getItem().getEntityItem());
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        if (event.player.world.isRemote)
            return;

        checkItem(event.player, event.crafting);
    }

    public void onSpellcast(EntityPlayer player, Spellcast spell)
    {
        player.addStat(firstSpell, 1);
    }

    private void checkItem(EntityPlayer player, ItemStack stack)
    {
        for (ItemStack gem : gemTypes)
        {
            if (ItemStack.areItemsEqual(stack, gem))
            {
                player.addStat(discoverGems, 1);
                break;
            }
        }

        Item item = stack.getItem();

        if (item instanceof ItemRing)
        {
            player.addStat(acquireRing, 1);
        }

        if (item instanceof ItemWand)
        {
            player.addStat(acquireWand, 1);
        }

        if (item instanceof ItemStaff)
        {
            player.addStat(acquireStaff, 1);
        }

        if (item instanceof ItemBlock)
        {
            Block block = ((ItemBlock) item).getBlock();

            if (block instanceof BlockEssentializer)
            {
                player.addStat(essentializing, 1);
            }
        }
    }
}
*/
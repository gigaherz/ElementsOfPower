package gigaherz.elementsofpower;

import gigaherz.elementsofpower.client.GuiOverlayMagicContainer;
import gigaherz.elementsofpower.network.SpellSequenceUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.swing.text.JTextComponent;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class ItemWand extends ItemMagicContainer {
    private static final String[] subNames = {
            "lapisWand", "emeraldWand", "diamondWand", "creativeWand",
            "lapisStaff", "emeraldStaff", "diamondStaff", "creativeStaff"
    };

    private static final EnumRarity rarities[] = {
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON,
            EnumRarity.UNCOMMON, EnumRarity.RARE, EnumRarity.EPIC, EnumRarity.COMMON
    };

    private final static Hashtable<ItemStack, byte[]> spellBackup = new Hashtable<ItemStack, byte[]>();

    public ItemWand() {
        setMaxStackSize(1);
        setHasSubtypes(true);
        setUnlocalizedName("magicWand");
        setCreativeTab(ElementsOfPower.tabMagic);
    }

    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return rarities[stack.getItemDamage()];
    }

    @Override
    public int getMetadata(int damageValue) {
        return damageValue;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int sub = stack.getItemDamage();

        if (sub >= subNames.length) {
            sub = 0;
        }

        return getUnlocalizedName() + "." + subNames[sub];
    }

    public ItemStack getStack(int count, int damageValue) {
        return new ItemStack(this, count, damageValue);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
        for (int meta = 0; meta < subNames.length; meta++) {
            subItems.add(new ItemStack(itemIn, 1, meta));
        }
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        if(world.isRemote) {
            int slot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
            GuiOverlayMagicContainer.instance.beginHoldingRightButton(slot, stack);
        }
        return stack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
        if (!world.isRemote) {
            onMagicItemReleased(stack, world, player, remaining);
        } else {
            GuiOverlayMagicContainer.instance.endHoldingRightButton(false);
        }
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return 72000;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.BOW;
    }

    @Override
    public void onMagicItemReleased(ItemStack stack, World world,
                                    EntityPlayer player, int remaining) {

        Vec3 lookAt = player.getLook(1.0F);

        byte[] sequence = stack.getTagCompound().getByteArray("SpellSequence");
        if(sequence.length == 0)
            return;

        switch(getSequenceItem(sequence, 0))
        {
            case -1:
                return;
            case 0:
                // fire1
                switch(getSequenceItem(sequence, 1))
                {
                    case -1:
                        doLittleFireball(world, player, lookAt);
                        return;
                    case 0:
                        // fire2
                        switch(getSequenceItem(sequence, 2))
                        {
                            case -1:
                                doExplodingFireball(world, player, 1, lookAt);
                                return;
                            case 0:
                                // fire3
                                switch(getSequenceItem(sequence, 3))
                                {
                                    case -1:
                                        doExplodingFireball(world, player, 2, lookAt);
                                        return;
                                    default:
                                        // fire2
                                        return;
                                    // TODO: more
                                }

                            // TODO: more
                        }

                        break;
                    // TODO: more
                }

                break;
            // TODO: more
        }

    }

    private int getSequenceItem(byte[] sequence, int i) {
        if(sequence.length <= i)
            return -1;
        return sequence[i];
    }

    private void doExplodingFireball(World world, EntityPlayer player, int power, Vec3 lookAt) {
        EntityLargeFireball var17 = new EntityLargeFireball(world, player, lookAt.xCoord * 10, lookAt.yCoord * 10, lookAt.zCoord * 10);

        var17.explosionPower = power;

        var17.posX = player.posX + lookAt.xCoord * player.width * 0.75f;
        var17.posY = player.posY + 1.0f;
        var17.posZ = player.posZ + lookAt.zCoord * player.width * 0.75f;

        world.spawnEntityInWorld(var17);
    }

    private void doLittleFireball(World world, EntityPlayer player, Vec3 lookAt) {
        EntitySmallFireball var17 = new EntitySmallFireball(world, player, lookAt.xCoord * 10, lookAt.yCoord * 10, lookAt.zCoord * 10);

        var17.posX = player.posX + lookAt.xCoord * 2;
        var17.posY = player.posY + 1.0f;
        var17.posZ = player.posZ + lookAt.zCoord * 2;

        world.spawnEntityInWorld(var17);
    }

    public void processSequenceUpdate(SpellSequenceUpdate message, ItemStack stack) {
        byte[] sequence;

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
            nbt = new NBTTagCompound();

        if(message.changeMode == SpellSequenceUpdate.ChangeMode.BEGIN) {
            sequence = nbt.getByteArray("SpellSequence");
            spellBackup.put(stack, sequence);
        } else if(message.changeMode == SpellSequenceUpdate.ChangeMode.CANCEL) {
            sequence = spellBackup.get(stack);
            nbt.setByteArray("SpellSequence", sequence);
            stack.setTagCompound(nbt);
        } else if(message.changeMode == SpellSequenceUpdate.ChangeMode.COMMIT) {
            spellBackup.remove(stack);
        } else {
            sequence = new byte[message.sequence.size()];
            for(int i=0;i<sequence.length;i++)
                sequence[i] = message.sequence.get(i);
            nbt.setByteArray("SpellSequence", sequence);
            stack.setTagCompound(nbt);
            System.out.println("Sequence stored: length=" + sequence.length);
        }
    }
}

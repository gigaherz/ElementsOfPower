package gigaherz.elementsofpower.client;

import com.google.common.collect.ImmutableMap;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.renderers.*;
import gigaherz.elementsofpower.common.ISideProxy;
import gigaherz.elementsofpower.common.Used;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.spells.SpellcastEntityData;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.essentializer.gui.ContainerEssentializer;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneBlockType;
import gigaherz.elementsofpower.guidebook.GuiGuidebook;
import gigaherz.elementsofpower.items.ItemGemContainer;
import gigaherz.elementsofpower.network.AddVelocityPlayer;
import gigaherz.elementsofpower.network.EssentializerAmountsUpdate;
import gigaherz.elementsofpower.network.EssentializerTileUpdate;
import gigaherz.elementsofpower.network.SpellcastSync;
import gigaherz.elementsofpower.spelldust.BlockSpelldust;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.animation.ITimeValue;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.InvalidParameterException;

@Used
public class ClientProxy implements ISideProxy
{
    public void preInit()
    {
        OBJLoader.INSTANCE.addDomain(ElementsOfPower.MODID);

        ModelHandle.init();

        registerClientEvents();
        registerModels();
        registerEntityRenderers();
    }

    public void init()
    {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(
                (state, world, pos, tintIndex) -> {
                    Gemstone gem = state.getValue(BlockSpelldust.VARIANT);
                    return gem.getTintColor();
                }, ElementsOfPower.spell_wire);

        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                (stack, tintIndex) -> {
                    if (tintIndex != 0)
                        return 0xFFFFFFFF;

                    int index = stack.getItemDamage();

                    if (index >= Gemstone.values.length)

                        return 0xFFFFFFFF;

                    return Gemstone.values[index].getTintColor();
                }, ElementsOfPower.spelldust);
    }

    public void registerClientEvents()
    {
        MinecraftForge.EVENT_BUS.register(new MagicContainerOverlay());
        MinecraftForge.EVENT_BUS.register(new MagicTooltips());
        MinecraftForge.EVENT_BUS.register(new SpellRenderOverlay());
        MinecraftForge.EVENT_BUS.register(new TickEventWandControl());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.getMap().registerSprite(ElementsOfPower.location("blocks/cone"));
    }

    @Override
    public void handleSpellcastSync(SpellcastSync message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            World world = Minecraft.getMinecraft().theWorld;
            EntityPlayer player = (EntityPlayer) world.getEntityByID(message.casterID);
            SpellcastEntityData data = SpellcastEntityData.get(player);

            data.sync(message.changeMode, message.spellcast);
        });
    }

    @Override
    public void handleRemainingAmountsUpdate(EssentializerAmountsUpdate message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;

            if (message.windowId != -1)
            {
                if (message.windowId == player.openContainer.windowId)
                {
                    if ((player.openContainer instanceof ContainerEssentializer))
                    {
                        ((ContainerEssentializer) player.openContainer).updateAmounts(message.contained, message.remaining);
                    }
                }
            }
        });
    }

    @Override
    public void handleEssentializerTileUpdate(EssentializerTileUpdate message)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
            if (te instanceof TileEssentializer)
            {
                TileEssentializer essentializer = (TileEssentializer) te;
                essentializer.setInventorySlotContents(0, message.activeItem);
                essentializer.remainingToConvert = message.remaining;
            }
        });
    }

    @Override
    public void handleAddVelocity(AddVelocityPlayer message)
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
                Minecraft.getMinecraft().thePlayer.addVelocity(message.vx, message.vy, message.vz));
    }

    @Override
    public void displayBook()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiGuidebook());
    }

    @Override
    public void beginTracking(EntityPlayer playerIn, EnumHand hand)
    {
        TickEventWandControl.instance.handInUse = hand;
        playerIn.setActiveHand(hand);
    }

    @Override
    public IAnimationStateMachine load(ResourceLocation location, ImmutableMap<String, ITimeValue> parameters)
    {
        return ModelLoaderRegistry.loadASM(location, parameters);
    }

    // ----------------------------------------------------------- Item/Block Models
    public void registerModels()
    {
        registerBlockModelAsItem(ElementsOfPower.essentializer);
        registerBlockModelAsItem(ElementsOfPower.cocoon, "color=8,facing=north");

        registerItemModel(ElementsOfPower.analyzer);

        registerItemModel(ElementsOfPower.guidebook);

        registerItemModel(ElementsOfPower.fire, "element=fire");
        registerItemModel(ElementsOfPower.water, "element=water");
        registerItemModel(ElementsOfPower.air, "element=air");
        registerItemModel(ElementsOfPower.earth, "element=earth");
        registerItemModel(ElementsOfPower.light, "element=light");
        registerItemModel(ElementsOfPower.darkness, "element=dark");
        registerItemModel(ElementsOfPower.life, "element=life");
        registerItemModel(ElementsOfPower.death, "element=death");

        for (Gemstone g : Gemstone.values)
        {
            registerItemModel(ElementsOfPower.gemstone, g.ordinal(), "gem=" + g.getName());

            if (g != Gemstone.Creativite)
                registerItemModel(ElementsOfPower.spelldust, g.ordinal(), "gem=" + g.getName());
        }

        for (GemstoneBlockType b : GemstoneBlockType.values)
        {
            registerBlockModelAsItem(ElementsOfPower.gemstoneBlock, b.ordinal(), "type=" + b.getName());
            registerBlockModelAsItem(ElementsOfPower.gemstoneOre, b.ordinal(), "type=" + b.getName());
        }

        registerGemMeshDefinition(ElementsOfPower.magicRing, "magicRing");
        registerGemMeshDefinition(ElementsOfPower.magicWand, "magicWand");
        registerGemMeshDefinition(ElementsOfPower.magicStaff, "magicStaff");
    }

    private void registerGemMeshDefinition(Item item, String itemName)
    {
        ModelLoader.setCustomMeshDefinition(item, new GemContainerMeshDefinition(item, itemName));
    }

    public void registerBlockModelAsItem(final Block block)
    {
        Item item = Item.getItemFromBlock(block);
        assert item != null;
        registerItemModel(item);
    }

    public void registerItemModel(final Item item)
    {
        registerItemModel(item, "inventory");
    }

    public void registerBlockModelAsItem(final Block block, final String variant)
    {
        Item item = Item.getItemFromBlock(block);
        assert item != null;
        registerItemModel(item, variant);
    }

    public void registerItemModel(final ItemStack stack, final String variant)
    {
        registerItemModel(stack.getItem(), stack.getMetadata(), variant);
    }

    public void registerItemModel(final Item item, final String variant)
    {
        registerItemModel(item, 0, variant);
    }

    public void registerBlockModelAsItem(final Block block, final int meta, final String variant)
    {
        Item item = Item.getItemFromBlock(block);
        assert item != null;
        registerItemModel(item, meta, variant);
    }

    public void registerItemModel(final Item item, final int meta, final String variant)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), variant));
    }

    // ----------------------------------------------------------- Entity Renderers
    public void registerEntityRenderers()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEssentializer.class, new RenderEssentializer());

        RenderingRegistry.registerEntityRenderingHandler(EntityBall.class, RenderBall::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEssence.class, RenderEssence::new);
    }

    private class GemContainerMeshDefinition implements ItemMeshDefinition
    {
        final String itemName;

        private GemContainerMeshDefinition(Item item, String itemName)
        {
            this.itemName = itemName;

            ResourceLocation[] resLocs = new ResourceLocation[Gemstone.values.length + 1];
            for (int i = 0; i < Gemstone.values.length; i++)
            {
                Gemstone g = Gemstone.values[i];
                resLocs[i] = getModelResourceLocation(g);
            }
            resLocs[Gemstone.values.length] = getModelResourceLocation(null);
            ModelBakery.registerItemVariants(item, resLocs);
        }

        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            Item item = stack.getItem();
            if (!(item instanceof ItemGemContainer))
                throw new InvalidParameterException("stack is not a gem container");

            ItemGemContainer c = (ItemGemContainer) item;

            Gemstone g = c.getGemstone(stack);

            return getModelResourceLocation(g);
        }

        private ModelResourceLocation getModelResourceLocation(@Nullable Gemstone g)
        {
            String variantName = "gem=" + (g != null ? g.getName() : "unbound");

            return new ModelResourceLocation(ElementsOfPower.MODID + ":" + itemName, variantName);
        }
    }
}

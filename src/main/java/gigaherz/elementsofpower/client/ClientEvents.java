package gigaherz.elementsofpower.client;

import gigaherz.common.client.ModelHandle;
import gigaherz.common.state.client.ItemStateMapper;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.client.renderers.RenderBall;
import gigaherz.elementsofpower.client.renderers.RenderEssence;
import gigaherz.elementsofpower.client.renderers.RenderEssentializer;
import gigaherz.elementsofpower.entities.EntityBall;
import gigaherz.elementsofpower.entities.EntityEssence;
import gigaherz.elementsofpower.essentializer.TileEssentializer;
import gigaherz.elementsofpower.spells.Element;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.gemstones.GemstoneBlockType;
import gigaherz.elementsofpower.items.ItemGemContainer;
import gigaherz.guidebook.client.BookRegistryEvent;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.security.InvalidParameterException;

import static gigaherz.common.client.ModelHelpers.registerBlockModelAsItem;
import static gigaherz.common.client.ModelHelpers.registerItemModel;

@Mod.EventBusSubscriber(value=Side.CLIENT, modid=ElementsOfPower.MODID)
public class ClientEvents
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        OBJLoader.INSTANCE.addDomain(ElementsOfPower.MODID);

        ModelHandle.init();

        registerBlockModelAsItem(ElementsOfPower.essentializer);
        registerBlockModelAsItem(ElementsOfPower.cocoon, 0, "color=8,facing=north");

        registerItemModel(ElementsOfPower.analyzer);

        registerItemModel(ElementsOfPower.orb.getStack(Element.Fire), "element=fire");
        registerItemModel(ElementsOfPower.orb.getStack(Element.Water), "element=water");
        registerItemModel(ElementsOfPower.orb.getStack(Element.Air), "element=air");
        registerItemModel(ElementsOfPower.orb.getStack(Element.Earth), "element=earth");
        registerItemModel(ElementsOfPower.orb.getStack(Element.Light), "element=light");
        registerItemModel(ElementsOfPower.orb.getStack(Element.Darkness), "element=dark");
        registerItemModel(ElementsOfPower.orb.getStack(Element.Life), "element=life");
        registerItemModel(ElementsOfPower.orb.getStack(Element.Death), "element=death");

        new ItemStateMapper(ElementsOfPower.gemstone).registerAllModelsExplicitly();
        new ItemStateMapper(ElementsOfPower.spelldust).registerAllModelsExplicitly();

        for (GemstoneBlockType b : GemstoneBlockType.values)
        {
            registerBlockModelAsItem(ElementsOfPower.gemstoneBlock, b.ordinal(), "type=" + b.getName());
            registerBlockModelAsItem(ElementsOfPower.gemstoneOre, b.ordinal(), "type=" + b.getName());
        }

        registerGemMeshDefinition(ElementsOfPower.wand);
        registerGemMeshDefinition(ElementsOfPower.staff);
        registerGemMeshDefinition(ElementsOfPower.ring);
        registerGemMeshDefinition(ElementsOfPower.headband);
        registerGemMeshDefinition(ElementsOfPower.necklace);

        ClientRegistry.bindTileEntitySpecialRenderer(TileEssentializer.class, new RenderEssentializer());

        RenderingRegistry.registerEntityRenderingHandler(EntityBall.class, RenderBall::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEssence.class, RenderEssence::new);
    }

    @Optional.Method(modid = "gbook")
    @SubscribeEvent
    public static void registerBook(BookRegistryEvent event)
    {
        event.register(ElementsOfPower.location("xml/guidebook.xml"));
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.getMap().registerSprite(ElementsOfPower.location("blocks/cone"));
    }

    @SubscribeEvent
    public static void colorHandlers(ColorHandlerEvent.Item event)
    {
        event.getItemColors().registerItemColorHandler(
                (stack, tintIndex) ->
                {
                    if (tintIndex != 0)
                        return 0xFFFFFFFF;

                    int index = stack.getItemDamage();

                    if (index >= Gemstone.values.size())

                        return 0xFFFFFFFF;

                    return Gemstone.values.get(index).getTintColor();
                }, ElementsOfPower.spelldust);
    }

    private static void registerGemMeshDefinition(Item item)
    {
        ModelLoader.setCustomMeshDefinition(item, new GemContainerMeshDefinition(item));
    }

    private static class GemContainerMeshDefinition implements ItemMeshDefinition
    {
        final Item item;

        private GemContainerMeshDefinition(Item item)
        {
            this.item = item;

            ResourceLocation[] resLocs = new ResourceLocation[Gemstone.values.size() + 1];
            for (int i = 0; i < Gemstone.values.size(); i++)
            {
                Gemstone g = Gemstone.values.get(i);
                resLocs[i] = getModelResourceLocation(g);
            }
            resLocs[Gemstone.values.size()] = getModelResourceLocation(null);
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

            return new ModelResourceLocation(item.getRegistryName(), variantName);
        }
    }
}

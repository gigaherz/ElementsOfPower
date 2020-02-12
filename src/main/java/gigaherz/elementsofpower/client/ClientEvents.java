package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.items.ItemGemContainer;
import gigaherz.elementsofpower.spelldust.ItemSpelldust;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.security.InvalidParameterException;

@Mod.EventBusSubscriber(value= Dist.CLIENT, modid= ElementsOfPowerMod.MODID)
public class ClientEvents
{
    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.addSprite(ElementsOfPowerMod.location("blocks/cone"));
    }

    @SubscribeEvent
    public static void colorHandlers(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register(
                (stack, tintIndex) ->
                {
                    if (tintIndex != 0)
                        return 0xFFFFFFFF;

                    if (stack.getItem() instanceof ItemSpelldust)
                        return ((ItemSpelldust)stack.getItem()).getType().getTintColor();

                    return 0xFFFFFFFF;
                },
                ElementsOfPowerMod.ruby_spelldust,
                ElementsOfPowerMod.sapphire_spelldust,
                ElementsOfPowerMod.citrine_spelldust,
                ElementsOfPowerMod.agate_spelldust,
                ElementsOfPowerMod.quartz_spelldust,
                ElementsOfPowerMod.serendibite_spelldust,
                ElementsOfPowerMod.emerald_spelldust,
                ElementsOfPowerMod.amethyst_spelldust,
                ElementsOfPowerMod.diamond_spelldust
        );
    }
}

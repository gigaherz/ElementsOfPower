package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerItems;
import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.spelldust.SpelldustItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value= Dist.CLIENT, modid= ElementsOfPowerMod.MODID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.addSprite(ElementsOfPowerMod.location("block/cone"));
    }

    @SubscribeEvent
    public static void colorHandlers(ColorHandlerEvent.Item event)
    {
        event.getItemColors().register(
                (stack, tintIndex) ->
                {
                    if (tintIndex != 0)
                        return 0xFFFFFFFF;

                    if (stack.getItem() instanceof SpelldustItem)
                        return ((SpelldustItem)stack.getItem()).getType().getTintColor();

                    return 0xFFFFFFFF;
                },
                ElementsOfPowerItems.RUBY_SPELLDUST,
                ElementsOfPowerItems.SAPPHIRE_SPELLDUST,
                ElementsOfPowerItems.CITRINE_SPELLDUST,
                ElementsOfPowerItems.AGATE_SPELLDUST,
                ElementsOfPowerItems.QUARTZ_SPELLDUST,
                ElementsOfPowerItems.SERENDIBITE_SPELLDUST,
                ElementsOfPowerItems.EMERALD_SPELLDUST,
                ElementsOfPowerItems.AMETHYST_SPELLDUST,
                ElementsOfPowerItems.DIAMOND_SPELLDUST
        );
    }
}

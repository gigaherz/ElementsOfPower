package dev.gigaherz.elementsofpower.client;

import dev.gigaherz.elementsofpower.ElementsOfPowerItems;
import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.spelldust.SpelldustItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ElementsOfPowerMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.addSprite(ElementsOfPowerMod.location("block/cone"));
        event.addSprite(ElementsOfPowerMod.location("gui/ring_slot_background"));
        event.addSprite(ElementsOfPowerMod.location("gui/necklace_slot_background"));
        event.addSprite(ElementsOfPowerMod.location("gui/headband_slot_background"));
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
                        return ((SpelldustItem) stack.getItem()).getType().getTintColor();

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

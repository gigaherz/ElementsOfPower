package dev.gigaherz.elementsofpower.client;

import dev.gigaherz.elementsofpower.ElementsOfPowerMod;
import dev.gigaherz.elementsofpower.gemstones.Gemstone;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ElementsOfPowerMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents
{
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event)
    {
        Gemstone.values.forEach(gem -> {
            if (gem.generateCustomOre())
            {
                for (var ore : gem.getOres())
                {
                    ItemBlockRenderTypes.setRenderLayer(ore, RenderType.translucent());
                }
            }
        });
    }

    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event)
    {
        event.addSprite(ElementsOfPowerMod.location("block/cone"));
        event.addSprite(ElementsOfPowerMod.location("gui/ring_slot_background"));
        event.addSprite(ElementsOfPowerMod.location("gui/necklace_slot_background"));
        event.addSprite(ElementsOfPowerMod.location("gui/headband_slot_background"));
    }
}

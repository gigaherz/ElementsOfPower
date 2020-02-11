package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.ElementsOfPowerMod;
import gigaherz.elementsofpower.gemstones.Gemstone;
import gigaherz.elementsofpower.items.ItemGemContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
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

                    int index = stack.getItemDamage();

                    if (index >= Gemstone.values.size())

                        return 0xFFFFFFFF;

                    return Gemstone.values.get(index).getTintColor();
                }, ElementsOfPowerMod.spelldust);
    }
}

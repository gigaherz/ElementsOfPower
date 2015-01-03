package gigaherz.elementsofpower;

import gigaherz.elementsofpower.models.IModelRegistrationHelper;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderHookHandler {

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        final ModelManager modelManager = event.modelManager;
        final IRegistry modelRegistry = event.modelRegistry;
        final ModelBakery modelBakery = event.modelBakery;

        IModelRegistrationHelper helper = new IModelRegistrationHelper() {
            @Override
            public void registerCustomModel(ResourceLocation location, IBakedModel bakedModel) {
                modelRegistry.putObject(location, bakedModel);
            }

            public ModelManager getModelManager() { return modelManager; }
            public IRegistry getModelRegistry() { return modelRegistry; }
            public ModelBakery getModelBakery() { return modelBakery; }
        };

        ElementsOfPower.instance.proxy.registerCustomBakedModels(helper);
    }
}

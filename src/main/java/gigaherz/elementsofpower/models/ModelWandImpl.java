package gigaherz.elementsofpower.models;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class ModelWandImpl extends ModelWand {

    int subtype;
    float angle = 0;

    public ModelWandImpl(int dmg) {
        subtype = dmg;
    }

    @Override
    protected boolean beforeRender(TextureManager engine, String partName, int partNumber, String texture, long elapsed) {
        if (subtype == 0 && texture.equals("diamond1.png"))
            texture = "lapis1.png";

        if (subtype == 1 && texture.equals("diamond1.png"))
            texture = "emerald1.png";

        engine.bindTexture(new ResourceLocation("/assets/elementsofpower/models/" + texture));
        return true;
    }

    @Override
    public void render(TextureManager engine, long elapsed) {
        {
            angle += (0.1f * elapsed);
            if (angle > 360)
                angle -= 360;
        }

        super.render(engine, elapsed);
    }
}

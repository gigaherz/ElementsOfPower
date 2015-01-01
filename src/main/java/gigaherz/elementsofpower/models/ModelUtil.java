package gigaherz.elementsofpower.models;

import net.minecraft.client.renderer.texture.TextureManager;

public class ModelUtil {
    protected boolean beforeRender(TextureManager engine, String partName, int partNumber, String texture, long elapsed) {
        return true;
    }

    protected void afterRender(TextureManager engine, String partName, int partNumber, String texture, long elapsed) {
    }
}

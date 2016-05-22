package gigaherz.elementsofpower.client.renderers;

import gigaherz.elementsofpower.essentializer.TileEssentializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class RenderEssentializer extends TileEntitySpecialRenderer<TileEssentializer>
{
    ModelHandle handle = ModelHandle.of("elementsofpower:block/essentializer_2.obj");

    @Override
    public void renderTileEntityAt(TileEssentializer te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        IBakedModel model = handle.get();

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 1, z + 0.5);

        float timeRandom = MathHelper.getPositionRandom(te.getPos()) % 360;

        float time = (getWorld().getTotalWorldTime() + timeRandom + partialTicks) * 1.5f;

        float angle1 = time * 2.5f + 120 * (1 + (float) Math.sin(time * 0.05f));
        float angle2 = time * 0.9f;
        float bob = (float) Math.sin(time * (Math.PI / 180) * 2.91) * 0.06f;

        GlStateManager.translate(0, bob, 0);

        for (int i = 0; i < 4; i++)
        {
            GlStateManager.pushMatrix();

            float angle3 = angle2 + 90 * i;
            GlStateManager.rotate(angle3, 0, 1, 0);
            GlStateManager.translate(0.6, 0, 0);
            GlStateManager.rotate(-45, 0, 0, 1);

            GlStateManager.rotate(angle1, 0, 1, 0);

            ModelHandle.renderModel(model, 0xFFFFFFFF);

            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();

        ItemStack stack = te.getStackInSlot(0);
        if (stack != null)
        {
            float angle3 = time * 1.5f;
            float bob2 = (float) (1 + Math.sin(time * (Math.PI / 180) * 0.91)) * 0.03f;

            GlStateManager.pushMatrix();

            GlStateManager.translate(x + 0.5, y + 0.55 + bob2, z + 0.5);

            GlStateManager.rotate(angle3, 0, 1, 0);
            GlStateManager.rotate(90, 1, 0, 0);

            GlStateManager.color(1f, 1f, 1f, 1f);
            GlStateManager.scale(0.35, 0.35, 0.35);

            Minecraft mc = Minecraft.getMinecraft();
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            mc.getRenderItem().renderItem(stack, TransformType.GROUND);

            GlStateManager.popMatrix();
        }

        GlStateManager.enableLighting();
    }
}

package gigaherz.elementsofpower.renders;

import gigaherz.elementsofpower.essentializer.TileEssentializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class RenderEssentializer extends TileEntitySpecialRenderer<TileEssentializer>
{
    @Override
    public void renderTileEntityAt(TileEssentializer te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        IFlexibleBakedModel model = RenderingStuffs.loadModel("elementsofpower:block/essentializer_2.obj");

        bindTexture(TextureMap.locationBlocksTexture);

        GlStateManager.disableLighting();

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1, z + 0.5);

        float timeRandom = MathHelper.getPositionRandom(te.getPos()) % 360;

        float time = (getWorld().getTotalWorldTime() + timeRandom + partialTicks) * 1.5f;
        float angle1 = time * 2.5f + 120 * (1 + (float)Math.sin(time * 0.05f));
        float angle2 = time * 0.9f;
        float bob = (float) Math.sin(time * (Math.PI / 180) * 2.91) * 0.06f;

        GL11.glTranslated(0, bob, 0);

        for(int i=0;i<4;i++)
        {
            GL11.glPushMatrix();

            float angle3 = angle2 + 90*i;
            GL11.glRotatef(angle3, 0, 1, 0);
            GL11.glTranslated(0.6,0,0);
            GL11.glRotatef(-45, 0, 0, 1);

            GL11.glRotatef(angle1, 0, 1, 0);

            RenderingStuffs.renderModel(model, 0xFFFFFFFF);

            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();

        GlStateManager.enableLighting();
    }
}

package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.essentializer.TileEssentializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class TESREssentializer extends TileEntitySpecialRenderer<TileEssentializer>
{
    IFlexibleBakedModel model;

    public TESREssentializer()
    {
    }

    @Override
    public void renderTileEntityAt(TileEssentializer te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if(model == null)
        {
            try
            {
                IModel mod = ModelLoaderRegistry.getModel(new ResourceLocation("elementsofpower:block/essentializer_2.obj"));
                model = mod.bake(mod.getDefaultState(), Attributes.DEFAULT_BAKED_FORMAT,
                        (location) -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
            }
            catch(IOException e)
            {
                throw new ReportedException(new CrashReport("Error loading model for TESR", e));
            }
        }

        bindTexture(TextureMap.locationBlocksTexture);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y + 1, z + 0.5);

        float time = (te.getWorld().getWorldTime() + partialTicks) * 1.5f;
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

            renderModel(model);

            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();
    }

    private void renderModel(IFlexibleBakedModel model)
    {
        renderModel(model, -1);
    }

    private void renderModel(IFlexibleBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            this.renderQuads(worldrenderer, model.getFaceQuads(enumfacing), color);
        }

        this.renderQuads(worldrenderer, model.getGeneralQuads(), color);
        tessellator.draw();
    }

    private void renderQuads(WorldRenderer renderer, List<BakedQuad> quads, int color)
    {
        for (BakedQuad bakedquad : quads)
            LightUtil.renderQuadColor(renderer, bakedquad, color);
    }
}

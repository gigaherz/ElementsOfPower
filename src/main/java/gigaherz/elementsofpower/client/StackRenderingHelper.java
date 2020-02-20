package gigaherz.elementsofpower.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class StackRenderingHelper
{
    public static void renderItemStack(ItemModelMesher mesher, TextureManager renderEngine, int xPos, int yPos, ItemStack stack, int color)
    {
        RenderHelper.disableStandardItemLighting();
        RenderSystem.enableRescaleNormal();
        RenderSystem.disableAlphaTest();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        renderEngine.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        renderEngine.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        RenderSystem.pushMatrix();

        MatrixStack m = new MatrixStack();
        m.push();

        IBakedModel model = mesher.getItemModel(stack);
        setupGuiTransform(xPos, yPos, model.isGui3d());
        model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(m, model, ItemCameraTransforms.TransformType.GUI, false);

        //RenderSystem.translatef(0.45F, 0, 0);

        renderItem(model, color, m);

        RenderSystem.popMatrix();

        renderEngine.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        renderEngine.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

        RenderHelper.disableStandardItemLighting();
    }

    private static void renderItem(IBakedModel model, int color, MatrixStack m)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuil = tessellator.getBuffer();
        bufferBuil.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

        Random random = new Random();
        random.setSeed(42);

        float a = ((color>>24)&0xFF)/255.0f;
        float r = ((color>>16)&0xFF)/255.0f;
        float g = ((color>>8)&0xFF)/255.0f;
        float b = ((color>>0)&0xFF)/255.0f;

        for (BakedQuad bakedquad : model.getQuads(null, null, random))
        {
            bufferBuil.addVertexData(m.getLast(), bakedquad, r, g, b, a, 0x00F000F0, OverlayTexture.DEFAULT_LIGHT, true);
        }

        tessellator.draw();
    }

    private static void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d)
    {
        RenderSystem.translatef(xPosition, yPosition, 150);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);

        if (isGui3d)
        {
            RenderSystem.enableLighting();
        }
        else
        {
            RenderSystem.disableLighting();
        }
    }
}

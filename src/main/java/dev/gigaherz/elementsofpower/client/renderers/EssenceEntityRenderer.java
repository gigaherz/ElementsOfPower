package dev.gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.client.renderers.spells.SpellRenderer;
import dev.gigaherz.elementsofpower.entities.EssenceEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullLazy;

public class EssenceEntityRenderer extends EntityRenderer<EssenceEntity>
{
    public EssenceEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    NonNullLazy<ModelHandle> handle = SpellRenderer.modelSphere;

    @Override
    public void render(EssenceEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
        float scale = entity.getScale();

        /*
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableRescaleNormal();
        */

        matrixStackIn.pushPose();
        matrixStackIn.scale(scale, scale, scale);

        float cycle = (entity.tickCount % 10 + partialTicks) / 11.0f;

        int ball_color = entity.getColor(entity.tickCount + partialTicks);
        for (int i = 0; i <= 8; i++)
        {
            float tt = (i + cycle) / 9.0f;
            float subScale = EssenceEntity.lerp(0.01f, 1.0f, tt) * 10;

            float rtt = (1 - tt);
            int alpha = (i == 0 ? 255 : (int) (rtt * 255));
            if (alpha >= 128) alpha = 255 - alpha;
            alpha /= 2;
            int color = (alpha << 24) | ball_color;

            matrixStackIn.pushPose();
            matrixStackIn.scale(subScale, subScale, subScale);

            handle.get().render(bufferIn, RenderType.entityTranslucent(getTextureLocation(entity)), matrixStackIn, 0x00F000F0, color);

            matrixStackIn.popPose();
        }

        matrixStackIn.popPose();

        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(EssenceEntity entity)
    {
        return new ResourceLocation("forge:textures/white.png");
    }
}
package dev.gigaherz.elementsofpower.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.client.renderers.ModelHandle;
import dev.gigaherz.elementsofpower.client.renderers.spells.SpellRenderer;
import dev.gigaherz.elementsofpower.entities.BallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.NonNullLazy;

public class BallEntityRenderer extends EntityRenderer<BallEntity>
{
    public BallEntityRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    NonNullLazy<ModelHandle> handle = SpellRenderer.modelSphere;

    @Override
    public void render(BallEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
        if (entity.tickCount < 2)
            return;

        float scale = entity.getScale() * 0.25f;

        matrixStackIn.pushPose();
        matrixStackIn.scale(scale, scale, scale);

        int ball_color = entity.getColor();
        for (int i = 0; i <= 4; i++)
        {
            float tt = (i + (entity.tickCount % 10 + partialTicks) / 11.0f) / 5.0f;
            float subScale = (1 + 0.5f * tt);

            int alpha = 255 - (i == 0 ? 0 : (int) (tt * 255));
            int color = (alpha << 24) | ball_color;

            matrixStackIn.pushPose();
            matrixStackIn.scale(subScale, subScale, subScale);

            handle.get().render(matrixStackIn, bufferIn, RenderType.entityTranslucent(getTextureLocation(entity)), 0x00F000F0, color);

            matrixStackIn.popPose();
        }

        matrixStackIn.popPose();

        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(BallEntity entity)
    {
        return SpellRenderer.getTexture(entity.getSpellcast());
    }
}
package dev.gigaherz.elementsofpower.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gigaherz.elementsofpower.entities.PillarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class PillarEntityRenderer extends EntityRenderer<PillarEntity>
{
    public PillarEntityRenderer(EntityRendererProvider.Context pContext)
    {
        super(pContext);
    }

    @Override
    public void render(PillarEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight)
    {
        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PillarEntity pEntity)
    {
        return null;
    }
}

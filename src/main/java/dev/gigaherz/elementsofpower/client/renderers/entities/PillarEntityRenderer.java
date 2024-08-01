package dev.gigaherz.elementsofpower.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.elementsofpower.client.models.PillarModel;
import dev.gigaherz.elementsofpower.entities.PillarEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class PillarEntityRenderer extends EntityRenderer<PillarEntity>
{
    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("elementsofpower","textures/entity/pillar.png");

    private final ModelPart pillarModel;

    public PillarEntityRenderer(EntityRendererProvider.Context pContext)
    {
        super(pContext);
        pillarModel = pContext.bakeLayer(PillarModel.LAYER_LOCATION);
    }

    @Override
    public void render(PillarEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pose, MultiBufferSource pBuffer, int pPackedLight)
    {
        super.render(pEntity, pEntityYaw, pPartialTick, pose, pBuffer, pPackedLight);

        pose.pushPose();

        pose.translate(0, Math.min((pEntity.tickCount-pEntity.delay()+pPartialTick-PillarEntity.RAISE_TICKS)*32/PillarEntity.RAISE_TICKS, -1)/16.0f, 0);

        pose.mulPose(Axis.YP.rotation((float)Math.toRadians(90-pEntity.getYRot())));

        pillarModel.render(pose, pBuffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(pEntity))), pPackedLight, OverlayTexture.NO_OVERLAY);

        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(PillarEntity pEntity)
    {
        return TEXTURE_LOCATION; // TODO: per-effect texture
    }
}

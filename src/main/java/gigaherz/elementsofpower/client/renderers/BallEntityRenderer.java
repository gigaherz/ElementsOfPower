package gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import gigaherz.elementsofpower.client.renderers.spells.SpellRenderer;
import gigaherz.elementsofpower.entities.BallEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.NonNullLazy;

public class BallEntityRenderer extends EntityRenderer<BallEntity>
{
    public BallEntityRenderer(EntityRendererManager renderManager)
    {
        super(renderManager);
    }

    NonNullLazy<ModelHandle> handle = SpellRenderer.modelSphere;

    @Override
    public void render(BallEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        float scale = entity.getScale() * 0.25f;

        matrixStackIn.push();
        matrixStackIn.scale(scale, scale, scale);

        int ball_color = entity.getColor();
        for (int i = 0; i <= 4; i++)
        {
            float tt = (i + (entity.ticksExisted % 10 + partialTicks) / 11.0f) / 5.0f;
            float subScale = (1 + 0.5f * tt);

            int alpha = 255 - (i == 0 ? 0 : (int) (tt * 255));
            int color = (alpha << 24) | ball_color;

            matrixStackIn.push();
            matrixStackIn.scale(subScale, subScale, subScale);

            handle.get().render(bufferIn, RenderType.entityTranslucent(getEntityTexture(entity)), matrixStackIn, 0x00F000F0, color);

            matrixStackIn.pop();
        }

        matrixStackIn.pop();

        super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(BallEntity entity)
    {
        return SpellRenderer.getTexture(entity.getSpellcast());
    }
}
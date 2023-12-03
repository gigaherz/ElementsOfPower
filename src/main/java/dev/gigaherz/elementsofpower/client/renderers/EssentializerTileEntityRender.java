package dev.gigaherz.elementsofpower.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.gigaherz.elementsofpower.essentializer.EssentializerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;

public class EssentializerTileEntityRender implements BlockEntityRenderer<EssentializerBlockEntity>
{
    private final Lazy<ModelHandle> corner_handle = Lazy.of(() -> ModelHandle.of("elementsofpower:models/block/essentializer_corner.obj"));

    private final ResourceLocation texture = new ResourceLocation("elementsofpower:textures/block/side_obsidian.png");

    public EssentializerTileEntityRender(BlockEntityRendererProvider.Context ctx)
    {
    }

    @Override
    public void render(EssentializerBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        //GlStateManager.disableLighting();

        matrixStack.pushPose();
        matrixStack.translate(0.5, 1, 0.5);

        float timeRandom = Mth.getSeed(te.getBlockPos()) % 360;

        float time = (te.animateTick + timeRandom + partialTicks) * 1.5f;

        float angle1 = time * 2.5f + 120 * (1 + (float) Math.sin(time * 0.05f));
        float angle2 = time * 0.9f;
        float bob = (float) Math.sin(time * (Math.PI / 180) * 2.91) * 0.06f;

        matrixStack.translate(0, bob, 0);

        for (int i = 0; i < 4; i++)
        {
            matrixStack.pushPose();

            float angle3 = angle2 + 90 * i;
            matrixStack.mulPose(Axis.YP.rotationDegrees(angle3));
            matrixStack.translate(0.6, 0, 0);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(-45));

            matrixStack.mulPose(Axis.YP.rotationDegrees(angle1));

            corner_handle.get().render(matrixStack, bufferIn, RenderType.entityTranslucent(texture), combinedLightIn, 0xFFFFFFFF);

            matrixStack.popPose();
        }
        matrixStack.popPose();

        /*
        matrixStack.push();
        for (int i = 0; i < 4; i++)
        {
            matrixStack.push();

            float angle3 = angle2 + 90 * i;
            matrixStack.rotate(Vector3f.YP.rotationDegrees(angle3));
            matrixStack.translate(0.6, 0, 0);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(-45));

            matrixStack.rotate(Vector3f.YP.rotationDegrees(angle1));

            side_handle.get().render(bufferIn, RenderType.getEntityTranslucent(texture), matrixStack, combinedLightIn, 0xFFFFFFFF);

            matrixStack.pop();
        }
        matrixStack.pop();
         */

        ItemStack stack = te.getInventory().getStackInSlot(0);
        if (stack.getCount() > 0)
        {
            float angle3 = time * 1.5f;
            float bob2 = (float) (1 + Math.sin(time * (Math.PI / 180) * 0.91)) * 0.03f;

            matrixStack.pushPose();

            matrixStack.translate(0.5, 0.45 + bob2, 0.5);

            matrixStack.mulPose(Axis.YP.rotationDegrees(angle3));

            float scale = 0.45f;
            matrixStack.scale(scale, scale, scale);

            Minecraft mc = Minecraft.getInstance();
            mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, te.getLevel(), 0);

            matrixStack.popPose();
        }
    }
}

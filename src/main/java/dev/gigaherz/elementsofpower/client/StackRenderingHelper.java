package dev.gigaherz.elementsofpower.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class StackRenderingHelper
{
    public static void renderItemStack(ItemRenderer itemRenderer, PoseStack matrixStack, ItemStack stack, float x, float y, float z, int color)
    {
        PoseStack viewModelPose = RenderSystem.getModelViewStack();
        viewModelPose.pushPose();
        viewModelPose.mulPoseMatrix(matrixStack.last().pose());
        viewModelPose.translate(x, y, z);
        RenderSystem.applyModelViewMatrix();
        renderAndDecorateItem(itemRenderer, stack, 0, 0, color);
        viewModelPose.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderAndDecorateItem(ItemRenderer itemRenderer, ItemStack stack, int x, int y, int color)
    {
        if (!stack.isEmpty())
        {
            BakedModel bakedmodel = itemRenderer.getModel(stack, null, Minecraft.getInstance().player, 0);
            itemRenderer.blitOffset += 50.0F;

            try
            {
                renderGuiItem(itemRenderer, stack, x, y, bakedmodel, color);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(stack.getItem().getRegistryName()));
                crashreportcategory.setDetail("Item Damage", () -> String.valueOf(stack.getDamageValue()));
                crashreportcategory.setDetail("Item NBT", () -> String.valueOf(stack.getTag()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }

            itemRenderer.blitOffset -= 50.0F;
        }
    }

    public static void renderGuiItem(ItemRenderer itemRenderer, ItemStack p_115128_, int x, int y, BakedModel model, int color)
    {
        Minecraft.getInstance().textureManager.getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double) x, (double) y, (double) (100.0F + itemRenderer.blitOffset));
        posestack.translate(8.0D, 8.0D, 0.0D);
        posestack.scale(1.0F, -1.0F, 1.0F);
        posestack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();

        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = ((color >> 0) & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        var colorMultiply = new ColorWrapper(r, g, b, a, multibuffersource$buffersource);

        boolean flag = !model.usesBlockLight();
        if (flag)
        {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(p_115128_, ItemTransforms.TransformType.GUI, false, posestack1, colorMultiply, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag)
        {
            Lighting.setupFor3DItems();
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static class ColorWrapper implements MultiBufferSource
    {
        public final float _r;
        public final float _g;
        public final float _b;
        public final float _a;
        private final MultiBufferSource inner;

        private ColorWrapper(float r, float g, float b, float a, MultiBufferSource inner)
        {
            this._r = r;
            this._g = g;
            this._b = b;
            this._a = a;
            this.inner = inner;
        }

        @Override
        public VertexConsumer getBuffer(RenderType rt)
        {
            return new ConsumerWrapper(inner.getBuffer(rt));
        }

        private class ConsumerWrapper implements VertexConsumer
        {
            private final VertexConsumer buffer;

            public ConsumerWrapper(VertexConsumer buffer)
            {
                this.buffer = buffer;
            }

            @Override
            public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z)
            {
                return buffer.vertex(matrix, x, y, z);
            }

            @Override
            public VertexConsumer vertex(double x, double y, double z)
            {
                return buffer.vertex(x, y, z);
            }

            @Override
            public VertexConsumer color(int r, int g, int b, int a)
            {
                return buffer.color((int) (r * _r), (int) (g * _g), (int) (b * _b), (int) (a * _a));
            }

            @Override
            public VertexConsumer color(float r, float g, float b, float a)
            {
                return buffer.color(r * _r, g * _g, b * _b, a * _a);
            }

            @Override
            public VertexConsumer uv(float u, float v)
            {
                return buffer.uv(u, v);
            }

            @Override
            public VertexConsumer overlayCoords(int u, int v)
            {
                return buffer.overlayCoords(u, v);
            }

            @Override
            public VertexConsumer uv2(int u, int v)
            {
                return buffer.uv2(u, v);
            }

            @Override
            public VertexConsumer normal(float x, float y, float z)
            {
                return buffer.normal(x, y, z);
            }

            @Override
            public VertexConsumer normal(Matrix3f matrix, float x, float y, float z)
            {
                return buffer.normal(matrix, x, y, z);
            }

            @Override
            public void endVertex()
            {
                buffer.endVertex();
            }

            @Override
            public void defaultColor(int r, int g, int b, int a)
            {
                buffer.defaultColor(r, g, b, a);
            }

            @Override
            public void unsetDefaultColor()
            {
                buffer.unsetDefaultColor();
            }
        }
    }
}

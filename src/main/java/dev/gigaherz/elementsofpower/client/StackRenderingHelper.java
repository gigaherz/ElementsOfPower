package dev.gigaherz.elementsofpower.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class StackRenderingHelper
{
    public static void renderItemStack(ItemRenderer itemRenderer, PoseStack matrixStack, ItemStack stack, float x, float y, float z, int color)
    {
        var viewModelPose = RenderSystem.getModelViewStack();
        viewModelPose.pushMatrix();
        viewModelPose.mul(matrixStack.last().pose());
        viewModelPose.translate(x, y, z);
        RenderSystem.applyModelViewMatrix();
        renderAndDecorateItem(itemRenderer, stack, 0, 0, color);
        viewModelPose.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderAndDecorateItem(ItemRenderer itemRenderer, ItemStack stack, int x, int y, int color)
    {
        if (!stack.isEmpty())
        {
            BakedModel bakedmodel = itemRenderer.getModel(stack, null, Minecraft.getInstance().player, 0);

            try
            {
                renderGuiItem(itemRenderer, stack, x, y, bakedmodel, color);

            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }
        }
    }

    public static void renderGuiItem(ItemRenderer itemRenderer, ItemStack p_115128_, int x, int y, BakedModel model, int color)
    {
        Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.translate(x, y, 100.0F);
        modelViewStack.translate(8.0F, 8.0F, 0.0F);
        modelViewStack.scale(1.0F, -1.0F, 1.0F);
        modelViewStack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = ((color >> 0) & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        var colorMultiply = new ColoringBufferSource(r, g, b, a, bufferSource);

        boolean flag = !model.usesBlockLight();
        if (flag)
        {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(p_115128_, ItemDisplayContext.GUI, false, posestack1, colorMultiply, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag)
        {
            Lighting.setupFor3DItems();
        }

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }
}

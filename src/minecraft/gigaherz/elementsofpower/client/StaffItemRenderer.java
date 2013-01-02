package gigaherz.elementsofpower.client;

import gigaherz.elementsofpower.CommonProxy;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

public class StaffItemRenderer implements IItemRenderer
{
	final StaffModel model = new StaffModel();
	
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        if (type == ItemRenderType.EQUIPPED)
        {
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
            ItemRendererHelper helper)
    {
    	if(type == ItemRenderType.EQUIPPED && helper == ItemRendererHelper.EQUIPPED_BLOCK)
    		return true;
    	if(type == ItemRenderType.EQUIPPED && helper == ItemRendererHelper.BLOCK_3D)
    		return true;
    	if(type == ItemRenderType.EQUIPPED && helper == ItemRendererHelper.ENTITY_BOBBING)
    		return true;
    	if(type == ItemRenderType.EQUIPPED && helper == ItemRendererHelper.ENTITY_ROTATION)
    		return true;
        return false;
    }
    
    protected void bindTextureByName(RenderEngine engine, String texturePath)
    {
        engine.bindTexture(engine.getTexture(texturePath));
    }
    
    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
    	//RenderBlocks rb = (RenderBlocks)data[0];
    	//EntityItem entity = (EntityItem)data[1];
    	
    	RenderEngine engine = FMLClientHandler.instance().getClient().renderEngine;
    	
    	GL11.glPushMatrix();

    	bindTextureByName(engine, CommonProxy.STAFF_PNG);
    	
    	model.render();
    	
    	GL11.glPopMatrix();
    }
}

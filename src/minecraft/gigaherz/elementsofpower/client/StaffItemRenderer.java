package gigaherz.elementsofpower.client;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import gigaherz.elementsofpower.CommonProxy;
import gigaherz.elementsofpower.models.ModelStaffImpl;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

public class StaffItemRenderer implements IItemRenderer
{
	Map<Integer, ModelStaffImpl> models = new HashMap<Integer, ModelStaffImpl>();
	
	long lDate = 0;
	
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
    	return true;
    }
    
    protected void bindTextureByName(RenderEngine engine, String texturePath)
    {
        engine.bindTexture(engine.getTexture(texturePath));
    }

	float cc = -20;

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        RenderBlocks render = (RenderBlocks)data[0];
        EntityLiving entity = (EntityLiving)data[1];
        
        boolean inUse = false;
        
        if(entity instanceof EntityPlayer)
        {
        	EntityPlayer player = (EntityPlayer)entity;
        	inUse = player.isUsingItem();
        }
    	
    	int dmg = item.getItemDamage();
    	ModelStaffImpl model;
    	
    	if(models.containsKey(dmg))
    	{
    		model = models.get(dmg);
    	}
    	else
    	{
    		model = new ModelStaffImpl(dmg);
    		models.put(dmg, model);
    	}
    	
    	RenderEngine engine = FMLClientHandler.instance().getClient().renderEngine;

    	long cDate = new Date().getTime();
    	long elapsed = cDate - lDate;
    	lDate = cDate;
    	if(elapsed > 50)
    		elapsed = 50;
    	
    	GL11.glDisable(GL11.GL_CULL_FACE);
    	
    	GL11.glPushMatrix();	

    	GL11.glTranslatef(-0.3f, 0.1f, 1.3f);	  
    	
    	if(inUse)
    	{
    		GL11.glRotatef(-60f, 1.0f, 0.0f, -0.4f);
    	}
    
    	GL11.glTranslatef(0.0f, -1.5f, 0.0f);	    
    	GL11.glRotatef(cc, 0.5f, 0, 1);
    	GL11.glScalef(0.2f, 0.2f, 0.2f);
    	GL11.glPushMatrix();
    	
    	model.render(engine, elapsed);

    	GL11.glPopMatrix();
    	GL11.glPopMatrix();
    }
}

package gigaherz.elementsofpower.client;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
	Map<Integer, StaffModel> models = new HashMap<Integer, StaffModel>();
	
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
    	//RenderBlocks rb = (RenderBlocks)data[0];
    	//EntityItem entity = (EntityItem)data[1];
    	
    	int dmg = item.getItemDamage();
    	StaffModel model;
    	
    	if(models.containsKey(dmg))
    	{
    		model = models.get(dmg);
    	}
    	else
    	{
    		model = new StaffModel(dmg);
    		models.put(dmg, model);
    	}
    	
    	RenderEngine engine = FMLClientHandler.instance().getClient().renderEngine;

    	bindTextureByName(engine, CommonProxy.STAFF_PNG);
    	
    	long cDate = new Date().getTime();
    	long elapsed = cDate - lDate;
    	lDate = cDate;
    	if(elapsed > 50)
    		elapsed = 50;
    	
    	GL11.glPushMatrix();
    	GL11.glTranslatef(0.2f, 0.1f, 0.9f);    	    
    	GL11.glRotatef(cc, 0.5f, 0, 1);
    	GL11.glPushMatrix();

    	model.render(elapsed);

    	GL11.glPopMatrix();
    	GL11.glPopMatrix();
    }
}

package gigaherz.elementsofpower.models;

import net.minecraft.client.renderer.RenderEngine;

import org.lwjgl.opengl.GL11;

public class ModelStaffImpl extends ModelStaff {

	int subtype;
	float angle = 0;

	public ModelStaffImpl(int dmg)
	{
		subtype = dmg;
	}

	@Override
	protected boolean beforeRender(RenderEngine engine, String partName, int partNumber, String texture, long elapsed) 
	{
		if (partName.equals("Side"))
		{
		GL11.glPushMatrix();
		GL11.glRotatef(angle, 0, 1, 0);
		}
		
		if(subtype == 0 && texture.equals("diamond1.png"))
			texture = "lapis1.png";
		
		if(subtype == 1 && texture.equals("diamond1.png"))
			texture = "emerald1.png";

		int x = engine.getTexture("/gigaherz/elementsofpower/models/" + texture);
		engine.bindTexture(x);
		return true;
	}

	@Override
	protected void afterRender(RenderEngine engine, String partName, int partNumber, String texture, long elapsed) 
	{
		if (partName.equals("Side"))
			GL11.glPopMatrix();
	}
	
	@Override
	public void render(RenderEngine engine, long elapsed)
	{		
		{
			angle += (0.1f * elapsed);
			if(angle > 360)
				angle -= 360;
		}
		
		super.render(engine, elapsed);
	}
}

package gigaherz.elementsofpower.models;

import net.minecraft.client.renderer.RenderEngine;

import org.lwjgl.opengl.GL11;

public class ModelWandImpl extends ModelWand {

	int subtype;
	float angle = 0;

	public ModelWandImpl(int dmg)
	{
		subtype = dmg;
	}

	@Override
	protected boolean beforeRender(RenderEngine engine, String partName, int partNumber, String texture, long elapsed) 
	{
		if(subtype == 0 && texture.equals("diamond1.png"))
			texture = "lapis1.png";
		
		if(subtype == 1 && texture.equals("diamond1.png"))
			texture = "emerald1.png";

		int x = engine.getTexture("/gigaherz/elementsofpower/models/" + texture);
		engine.bindTexture(x);
		return true;
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

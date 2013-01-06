package gigaherz.elementsofpower.models;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.RenderEngine;

public class ModelUtil
{	
	protected boolean beforeRender(RenderEngine engine, String partName, int partNumber, String texture, long elapsed) 
	{
		return true;
	}
	
	protected void afterRender(RenderEngine engine, String partName, int partNumber, String texture, long elapsed)
	{
	}
}

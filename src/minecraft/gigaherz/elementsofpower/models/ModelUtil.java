package gigaherz.elementsofpower.models;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.RenderEngine;

public class ModelUtil
{
	public Map<String, String> textures;
	public Map<String, Mesh> meshes;
	protected Mesh currentMesh;
	protected int xoff;
	protected int yoff;
	
	public ModelUtil()
	{
		meshes = new HashMap<String, Mesh>();
		textures = new HashMap<String, String>();	
		xoff=1;
		yoff=0;
	}

	protected void vertexPosition(double d, double e, double f)
	{
		xoff++;
		currentMesh.addPosition((float)d, (float)e, (float)f);		
	}

	protected void vertexNormal(double d, double e, double f)
	{
		currentMesh.addNormal((float)d, (float)e, (float)f);		
	}

	protected void vertexTexture(double d, double e)
	{
		currentMesh.addTexCoords((float)d, (float)e);		
	}
	
	protected void face(int a, int b, int c)
	{
		currentMesh.addFace(a-yoff, b-yoff, c-yoff);
	}

	protected void beginObject(String name, String texture)
	{
		currentMesh = new Mesh();
		meshes.put(name, currentMesh);
		textures.put(name, texture);
		yoff=xoff;
	}
	
	public void render(RenderEngine engine, long elapsed)
	{
		for(String m : meshes.keySet())
		{
			String texture = textures.get(m);
			Mesh mesh = meshes.get(m);
			if(beforeRender(engine, m, texture, mesh, elapsed))
				mesh.Render();
		}
	}

	protected boolean beforeRender(RenderEngine engine, String m, String texture, Mesh mesh, long elapsed) 
	{
		return true;
	}
}

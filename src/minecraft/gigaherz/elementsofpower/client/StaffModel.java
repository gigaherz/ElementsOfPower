package gigaherz.elementsofpower.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class StaffModel extends ModelBase 
{
	//fields
	public final ModelRenderer staff;
	public final ModelRenderer orb1;
	public final ModelRenderer orb2;
	public final ModelRenderer orb3;
	public final ModelRenderer orb4;
	
	public int dmgValue;

	public StaffModel(int dmgValue)
	{
		float xf = -0.5f;
		float yf = -16.0f;
		float zf = -0.5f;
		
		int xOffset = 0;
		
		switch(dmgValue)
		{
		case 0:
			xOffset = 4;
			break;
		case 1:
			xOffset = 22;
			break;
		case 2:
			xOffset = 43;
			break;
		}
		
		staff = new ModelRenderer(this, 0, 0)
			.addBox(xf, yf, zf, 1, 32, 1)
			.setTextureSize(64, 64);
		orb1 = new ModelRenderer(this, xOffset, 0);
		orb1.addBox(xf+0.9f, yf+20, zf+0.9f, 2, 2, 2, -0.20f);
		orb1.setTextureSize(64, 64);
		orb2 = new ModelRenderer(this, xOffset, 0);
		orb2.addBox(xf+0.9f, yf+20, zf+0.9f, 2, 2, 2, -0.20f);
		orb2.setTextureSize(64, 64);
		orb3 = new ModelRenderer(this, xOffset, 0);
		orb3.addBox(xf-0.5f, yf+30.5f, zf-0.5f, 2, 2, 2, -0.20f);
		orb3.setTextureSize(64, 64);
		orb4 = new ModelRenderer(this, -8, -8);
		orb4.addBox(xf-0.5f, yf+20.0f, zf-0.5f, 2, 2, 2, -0.35f);
		orb4.setTextureSize(64, 64);
		staff.setRotationPoint(0, 0, 0);
		//staff.rotateAngleX = -0.2f;
	}
	
	private static final float f5 = 1.0f/6.0f;
		
	public void render(long elapsed) 
	{
		orb1.rotateAngleY += 0.00035f * elapsed;
		if(orb1.rotateAngleY > Math.PI*2)
			orb1.rotateAngleY -= (float)(Math.PI*2);
		orb2.rotateAngleY = orb1.rotateAngleY + (float)Math.PI;
		
		staff.render(f5);
		orb1.render(f5);
		orb2.render(f5);
		orb3.render(f5);
		orb4.render(f5);
	}
}
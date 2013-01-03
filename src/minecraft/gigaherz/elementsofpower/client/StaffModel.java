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
		
		staff = new ModelRenderer(this, 0, 0);
		staff.addBox(-0.5f, -16.0f, -0.5f, 1, 32, 1);
		staff.setTextureSize(64, 64);
		
		orb1 = new ModelRenderer(this, xOffset, 0);
		orb1.addBox(-6f, -16.0f+20, -6f, 8, 8, 8, -3f);
		orb1.setTextureSize(64, 64);
		
		orb2 = new ModelRenderer(this, xOffset, 0);
		orb2.addBox(-6f, -16.0f+20, -6f, 8, 8, 8, -3f);
		orb2.setTextureSize(64, 64);

		orb4 = new ModelRenderer(this, -8, -8);
		orb4.addBox(-4f, -16.0f+20f, -4f, 8, 8, 8, -3.4f);
		orb4.setTextureSize(64, 64);
		
		orb3 = new ModelRenderer(this, xOffset, 0);
		orb3.addBox(-4f, -16.0f+28.5f, -4f, 8, 8, 8, -3.25f);
		orb3.setTextureSize(64, 64);
		
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
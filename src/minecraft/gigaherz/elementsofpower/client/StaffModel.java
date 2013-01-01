package gigaherz.elementsofpower.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class StaffModel extends ModelBase 
{
	//fields
	public final ModelRenderer staff;

	public StaffModel()
	{
		staff = new ModelRenderer(this, 0, 0);
		staff.addBox(0, 0f, 7.5f, 1, 1, 1);
		staff.setRotationPoint(0f, -0.4f, 0f);
		staff.setTextureSize(256, 256);
	}
	
	private static final float f5 = 1; //0.0625f;
	
	public void render() 
	{		
		staff.render(f5);
	}
}
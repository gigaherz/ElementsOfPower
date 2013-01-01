package gigaherz.elementsofpower.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class StaffModel extends ModelBase 
{
	//fields
	public final ModelRenderer staff;

	public StaffModel()
	{
		staff = new ModelRenderer(this, 4*16, 1*16)
			.addBox(0, 0f, 5.0f, 1, 40, 1)
			.setTextureSize(256, 256);
		staff.setRotationPoint(0.5f, -16f, 5.5f);
	}
	
	private static final float f5 = 1.0f/10.0f;
	
	public void render() 
	{		
		staff.render(f5);
	}
}
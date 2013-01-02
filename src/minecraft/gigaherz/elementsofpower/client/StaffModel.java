package gigaherz.elementsofpower.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class StaffModel extends ModelBase 
{
	//fields
	public final ModelRenderer staff;

	public StaffModel()
	{
		staff = new ModelRenderer(this, 0, 0)
			.addBox(0, 0f, 5.0f, 1, 32, 1)
			.setTextureSize(64, 128);
		staff.setRotationPoint(0.5f, -16f, 5.5f);
	}
	
	private static final float f5 = 1.0f/8.0f;
	
	public void render() 
	{		
		staff.render(f5);
	}
}
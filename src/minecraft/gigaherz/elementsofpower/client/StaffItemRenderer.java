package gigaherz.elementsofpower.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

public class StaffItemRenderer implements IItemRenderer
{
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        if (type == ItemRenderType.FIRST_PERSON_MAP)
        {
            return true;
        }

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

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
    	/*GL11.glPushMatrix();
    	GL11.glTranslatef(0.5F, 0.9F, 0.5F);
    	GL11.glRotatef(180F, 0.0F, 0.9F, 0.0F);
    	GL11.glScalef(-1F, -1F, 1.0F);*/

    	bindTextureByName("/terrain.png");
    	modelCraftingTable.render();
    	GL11.glPopMatrix();
    	
        if (type == ItemRenderType.FIRST_PERSON_MAP)
        {
        }

        if (type == ItemRenderType.EQUIPPED)
        {
        }
    }
}

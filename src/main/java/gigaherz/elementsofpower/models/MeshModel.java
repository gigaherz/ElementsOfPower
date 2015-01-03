package gigaherz.elementsofpower.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class MeshModel {

    public List<Vector3f> positions;
    public List<Vector3f> normals;
    public List<Vector2f> texCoords;

    public List<MeshPart> parts;

    private ModelManager manager;

    public MeshModel(ModelManager modelManager) {
        manager = modelManager;
        positions = new ArrayList<Vector3f>();
        normals = new ArrayList<Vector3f>();
        texCoords = new ArrayList<Vector2f>();
        parts = new ArrayList<MeshPart>();
    }

    public void addPosition(float x, float y, float z) {
        positions.add(new Vector3f(x, y, z));
    }

    public void addNormal(float x, float y, float z) {
        normals.add(new Vector3f(x, y, z));
    }

    public void addTexCoords(float x, float y) {
        texCoords.add(new Vector2f(x, y));
    }

    public void addPart(MeshPart part) {
        parts.add(part);
    }

    private int getColorValue(Vector3f color)
    {
        int r = (int)color.x;
        int g = (int)color.y;
        int b = (int)color.z;
        return 0xFF000000 | (b<<16) | (g << 8) | r;
    }

    public List<BakedQuad> bakeModel() {
        List<BakedQuad> bakeList = new ArrayList<BakedQuad>();

        for(int j=0;j<parts.size();j++) {

            MeshPart part = parts.get(j);

            TextureAtlasSprite sprite = null;
            int color = (int)0xFFFFFFFF;

            if(part.material != null) {
                if (part.material.DiffuseTextureMap != null) {
                    sprite = manager.getTextureMap().getAtlasSprite(part.material.DiffuseTextureMap);
                }
                if(part.material.DiffuseColor != null) {
                    color = getColorValue(part.material.DiffuseColor);
                }
            }

            for (int i = 0; i < part.indices.size(); i += 4) {
                BakedQuad quad = bakeQuad(part, i, sprite, color);
                bakeList.add(quad);
            }
        }
        return bakeList;
    }

    private BakedQuad bakeQuad(MeshPart part, int startIndex, TextureAtlasSprite sprite, int color)
    {
        int[] faceData = new int[28];
        for(int i=0;i<4;i++) {
            storeVertexData(faceData, i,
                    positions.get(part.indices.get(startIndex+i)),
                    texCoords.get(part.indices.get(startIndex+i)),
                    sprite, color);
        }
        return new BakedQuad(faceData, -1, FaceBakery.getFacingFromVertexData(faceData));
    }

    private static void storeVertexData(int[] faceData, int storeIndex, Vector3f position, Vector2f faceUV, TextureAtlasSprite sprite, int shadeColor)
    {
        int l = storeIndex * 7;
        faceData[l + 0] = Float.floatToRawIntBits(position.x);
        faceData[l + 1] = Float.floatToRawIntBits(position.y);
        faceData[l + 2] = Float.floatToRawIntBits(position.z);
        faceData[l + 3] = shadeColor;
        if(sprite != null) {
            faceData[l + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(faceUV.x));
            faceData[l + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(faceUV.y));
        } else {
            faceData[l + 4] = Float.floatToRawIntBits(faceUV.x);
            faceData[l + 5] = Float.floatToRawIntBits(faceUV.y);
        }
        faceData[l + 6] = 0;
    }
}

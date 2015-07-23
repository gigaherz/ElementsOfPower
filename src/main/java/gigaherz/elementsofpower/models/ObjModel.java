package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import gigaherz.elementsofpower.ElementsOfPower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.*;

public class ObjModel
{
    public List<Vector3f> positions;
    public List<Vector3f> normals;
    public List<Vector2f> texCoords;

    public final List<MeshPart> parts = new ArrayList<MeshPart>();
    ;

    public void addPosition(float x, float y, float z)
    {
        if (positions == null)
            positions = new ArrayList<Vector3f>();
        positions.add(new Vector3f(x, y, z));
    }

    public void addNormal(float x, float y, float z)
    {
        if (normals == null)
            normals = new ArrayList<Vector3f>();
        normals.add(new Vector3f(x, y, z));
    }

    public void addTexCoords(float x, float y)
    {
        if (texCoords == null)
            texCoords = new ArrayList<Vector2f>();
        texCoords.add(new Vector2f(x, y));
    }

    public void addPart(MeshPart part)
    {
        parts.add(part);
    }

    private int getColorValue(Vector3f color)
    {
        int r = (int) color.x;
        int g = (int) color.y;
        int b = (int) color.z;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public void bake(ModelManager manager, Map<String, String> textures, ImmutableList.Builder<BakedQuad> bakeList)
    {
        for (MeshPart part : parts)
        {
            TextureAtlasSprite sprite = null;
            int color = 0xFFFFFFFF;

            Material m = part.material;
            if (m == null)
            {
                if (part.materialName != null)
                {
                    sprite = manager.getTextureMap().getAtlasSprite(textures.get(part.materialName));
                }
            }
            else
            {
                if (m.DiffuseTextureMap != null)
                {
                    sprite = manager.getTextureMap().getAtlasSprite(textures.get(m.DiffuseTextureMap));
                }
                else if (m.AmbientTextureMap != null)
                {
                    sprite = manager.getTextureMap().getAtlasSprite(textures.get(m.AmbientTextureMap));
                }

                if (m.DiffuseColor != null)
                {
                    color = getColorValue(m.DiffuseColor);
                }
            }

            for (int[][] face : part.indices)
            {
                bakeList.add(makeQuad(face, sprite, color));
            }
        }
    }

    private BakedQuad makeQuad(int[][] face, TextureAtlasSprite sprite, int color)
    {
        int[] faceData = new int[28];

        processVertex(faceData, 0, face[0], color, sprite);
        processVertex(faceData, 1, face[1], color, sprite);
        processVertex(faceData, 2, face[2], color, sprite);
        processVertex(faceData, 3, face[3], color, sprite);

        return new BakedQuad(faceData, -1, FaceBakery.getFacingFromVertexData(faceData));
    }

    private void processVertex(int[] faceData, int i, int[] vertex, int color, TextureAtlasSprite sprite)
    {
        Vector3f position = new Vector3f(0, 0, 0);
        Vector2f texCoord = new Vector2f(0, 0);

        int p = 0;

        if (positions != null)
            position = positions.get(vertex[p++]);

        if (normals != null)
            p++; // normals not used by minecraft

        if (texCoords != null)
        {
            texCoord = texCoords.get(vertex[p]);

            if (sprite != null)
            {
                texCoord = new Vector2f(
                        sprite.getInterpolatedU(texCoord.x * 16),
                        sprite.getInterpolatedV(texCoord.y * 16));
            }
        }

        int l = i * 7;
        faceData[l++] = Float.floatToRawIntBits(position.x);
        faceData[l++] = Float.floatToRawIntBits(position.y);
        faceData[l++] = Float.floatToRawIntBits(position.z);
        faceData[l++] = color;
        faceData[l++] = Float.floatToRawIntBits(texCoord.x);
        faceData[l++] = Float.floatToRawIntBits(texCoord.y);
        faceData[l] = 0;
    }

    public VertexFormat getVertexFormat()
    {
        return Attributes.DEFAULT_BAKED_FORMAT;
    }

    public static class MeshPart
    {
        public String materialName;
        public Material material;
        public List<int[][]> indices;

        public MeshPart()
        {
            indices = new ArrayList<int[][]>();
        }

        public void addFace(int[]... f)
        {
            indices.add(f);
        }
    }

    public static class Loader
    {
        static final Set<String> unknownCommands = new HashSet<String>();

        private ObjModel currentModel;
        private ObjModel.MeshPart currentPart;
        private MaterialLibrary currentMatLib;

        public final ResourceLocation modelLocation;
        public final ResourceLocation baseLocation;

        public final Map<String, String> textures = new HashMap<String, String>();
        public final Set<String> usedTextures = new HashSet<String>();

        public Loader(ResourceLocation baseLocation)
        {
            this.baseLocation = baseLocation;
            this.modelLocation = ObjModelRegistrationHelper.getObjLocation(baseLocation);
        }

        public ObjModel getModel() throws IOException
        {
            if (currentModel != null)
                return currentModel;

            return loadFromResource();
        }

        @SuppressWarnings("unchecked")
        public Collection<String> getTextures(ObjModelRegistrationHelper objModelRegistrationHelper) throws IOException
        {
            ModelBlock modelblock = objModelRegistrationHelper.loadModel(baseLocation);

            while (modelblock != null)
            {
                for (Map.Entry<String, String> e : ((Map<String, String>) modelblock.textures).entrySet())
                {
                    if (!textures.containsKey(e.getKey()))
                        textures.put(e.getKey(), e.getValue());
                }
                modelblock = modelblock.parent;
            }

            ObjModel model = getModel();

            for (MeshPart p : model.parts)
            {
                Material m = p.material;

                if (m == null)
                {
                    if (p.materialName != null)
                        usedTextures.add(textures.get(p.materialName));
                    continue;
                }

                if (m.DiffuseTextureMap != null)
                {
                    usedTextures.add(textures.get(m.DiffuseTextureMap));
                }
                else if (m.AmbientTextureMap != null)
                {
                    usedTextures.add(textures.get(m.AmbientTextureMap));
                }
            }

            if (textures.containsKey("particle"))
                usedTextures.add(textures.get("particle"));

            return usedTextures;
        }

        private void addTexCoord(String line)
        {
            String[] args = line.split(" ");

            float x = Float.parseFloat(args[0]);
            float y = Float.parseFloat(args[1]);

            currentModel.addTexCoords(x, y);
        }

        private void addNormal(String line)
        {
            String[] args = line.split(" ");

            float x = Float.parseFloat(args[0]);
            float y = Float.parseFloat(args[1]);

            float z = args[2].equals("\\\\")
                    ? (float) Math.sqrt(1 - x * x - y * y)
                    : Float.parseFloat(args[2]);

            currentModel.addNormal(x, y, z);
        }

        private void addPosition(String line)
        {
            String[] args = line.split(" ");

            float x = Float.parseFloat(args[0]);
            float y = Float.parseFloat(args[1]);
            float z = Float.parseFloat(args[2]);

            currentModel.addPosition(x, y, z);
        }

        private void addFace(String line)
        {
            String[] args = line.split(" ");

            if (args.length < 3 || args.length > 4)
                throw new InvalidParameterException();

            String[] p1 = args[0].split("/");
            String[] p2 = args[1].split("/");
            String[] p3 = args[2].split("/");

            int[] v1 = parseIndices(p1);
            int[] v2 = parseIndices(p2);
            int[] v3 = parseIndices(p3);

            if (args.length == 3)
            {
                currentPart.addFace(v1, v2, v3, v3);
            }
            else if (args.length == 4)
            {
                String[] p4 = args[3].split("/");
                int[] v4 = parseIndices(p4);

                currentPart.addFace(v1, v2, v3, v4);
            }
        }

        private int[] parseIndices(String[] p1)
        {
            int[] indices = new int[p1.length];
            for (int i = 0; i < p1.length; i++)
            {
                indices[i] = Integer.parseInt(p1[i]) - 1;
            }
            return indices;
        }

        private void useMaterial(String matName)
        {
            currentPart = new ObjModel.MeshPart();
            currentPart.materialName = matName;
            currentPart.material = currentMatLib.materials.get(matName);
            currentModel.addPart(currentPart);
        }

        private void newObject(String line)
        {
        }

        private void newGroup(String line)
        {
        }

        private void loadMaterialLibrary(ResourceLocation locOfParent, String path) throws IOException
        {

            String prefix = locOfParent.getResourcePath();
            int pp = prefix.lastIndexOf('/');
            prefix = (pp >= 0) ? prefix.substring(0, pp + 1) : "";

            ResourceLocation loc = new ResourceLocation(locOfParent.getResourceDomain(), prefix + path);

            currentMatLib.loadFromStream(loc);
        }

        private ObjModel loadFromResource() throws IOException
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(modelLocation);
            InputStreamReader lineStream = new InputStreamReader(res.getInputStream(), Charsets.UTF_8);
            BufferedReader lineReader = new BufferedReader(lineStream);

            currentModel = new ObjModel();
            currentMatLib = new MaterialLibrary();

            for (; ; )
            {
                String currentLine = lineReader.readLine();
                if (currentLine == null)
                    break;

                if (currentLine.length() == 0 || currentLine.startsWith("#"))
                {
                    continue;
                }

                String[] fields = currentLine.split(" ", 2);
                String keyword = fields[0];
                String data = fields[1];

                if (keyword.equalsIgnoreCase("o"))
                {
                    newObject(data);
                }
                else if (keyword.equalsIgnoreCase("g"))
                {
                    newGroup(data);
                }
                else if (keyword.equalsIgnoreCase("mtllib"))
                {
                    loadMaterialLibrary(modelLocation, data);
                }
                else if (keyword.equalsIgnoreCase("usemtl"))
                {
                    useMaterial(data);
                }
                else if (keyword.equalsIgnoreCase("v"))
                {
                    addPosition(data);
                }
                else if (keyword.equalsIgnoreCase("vn"))
                {
                    addNormal(data);
                }
                else if (keyword.equalsIgnoreCase("vt"))
                {
                    addTexCoord(data);
                }
                else if (keyword.equalsIgnoreCase("f"))
                {
                    addFace(data);
                }
                else
                {
                    if (!unknownCommands.contains(keyword))
                    {
                        ElementsOfPower.logger.warn("Unrecognized command: " + currentLine);
                        unknownCommands.add(keyword);
                    }
                }
            }

            return currentModel;
        }
    }

    static class Material
    {
        public Vector3f AmbientColor;
        public Vector3f DiffuseColor;
        public Vector3f SpecularColor;
        public float SpecularCoefficient;

        public float Transparency;

        public int IlluminationModel;

        public String AmbientTextureMap;
        public String DiffuseTextureMap;

        public String SpecularTextureMap;
        public String SpecularHighlightTextureMap;

        public String BumpMap;
        public String DisplacementMap;
        public String StencilDecalMap;

        public String AlphaTextureMap;
    }

    static class MaterialLibrary
    {
        static final Set<String> unknownCommands = new HashSet<String>();

        public final Dictionary<String, Material> materials = new Hashtable<String, Material>();

        public void loadFromStream(ResourceLocation loc) throws IOException
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(loc);
            InputStreamReader lineStream = new InputStreamReader(res.getInputStream(), Charsets.UTF_8);
            BufferedReader lineReader = new BufferedReader(lineStream);

            Material currentMaterial = null;
            for (; ; )
            {
                String currentLine = lineReader.readLine();
                if (currentLine == null)
                    break;

                if (currentLine.length() == 0 || currentLine.startsWith("#"))
                    continue;

                String[] fields = currentLine.split(" ", 2);
                String keyword = fields[0];
                String data = fields[1];

                if (keyword.equalsIgnoreCase("newmtl"))
                {
                    currentMaterial = new Material();
                    materials.put(data, currentMaterial);
                }
                else if (currentMaterial == null)
                {
                    throw new IOException("Found material attributes before 'newmtl'");
                }
                else if (keyword.equalsIgnoreCase("Ka"))
                {
                    currentMaterial.AmbientColor = parseVector3f(data);
                }
                else if (keyword.equalsIgnoreCase("Kd"))
                {
                    currentMaterial.DiffuseColor = parseVector3f(data);
                }
                else if (keyword.equalsIgnoreCase("Ks"))
                {
                    currentMaterial.SpecularColor = parseVector3f(data);
                }
                else if (keyword.equalsIgnoreCase("Ns"))
                {
                    currentMaterial.SpecularCoefficient = Float.parseFloat(data);
                }
                else if (keyword.equalsIgnoreCase("Tr") ||
                        keyword.equalsIgnoreCase("d"))
                {
                    currentMaterial.Transparency = Float.parseFloat(data);
                }
                else if (keyword.equalsIgnoreCase("illum"))
                {
                    currentMaterial.IlluminationModel = Integer.parseInt(data);
                }
                else if (keyword.equalsIgnoreCase("map_Ka"))
                {
                    currentMaterial.AmbientTextureMap = data;
                }
                else if (keyword.equalsIgnoreCase("map_Kd"))
                {
                    currentMaterial.DiffuseTextureMap = data;
                }
                else if (keyword.equalsIgnoreCase("map_Ks"))
                {
                    currentMaterial.SpecularTextureMap = data;
                }
                else if (keyword.equalsIgnoreCase("map_Ns"))
                {
                    currentMaterial.SpecularHighlightTextureMap = data;
                }
                else if (keyword.equalsIgnoreCase("map_d"))
                {
                    currentMaterial.AlphaTextureMap = data;
                }
                else if (keyword.equalsIgnoreCase("map_bump") ||
                        keyword.equalsIgnoreCase("bump"))
                {
                    currentMaterial.BumpMap = data;
                }
                else if (keyword.equalsIgnoreCase("disp"))
                {
                    currentMaterial.DisplacementMap = data;
                }
                else if (keyword.equalsIgnoreCase("decal"))
                {
                    currentMaterial.StencilDecalMap = data;
                }
                else
                {
                    if (!unknownCommands.contains(keyword))
                    {
                        ElementsOfPower.logger.info("Unrecognized command: " + currentLine);
                        unknownCommands.add(keyword);
                    }
                }
            }
        }

        static Vector3f parseVector3f(String data)
        {
            String[] parts = data.split(" ");
            return new Vector3f(
                    Float.parseFloat(parts[0]),
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2])
            );
        }
    }
}

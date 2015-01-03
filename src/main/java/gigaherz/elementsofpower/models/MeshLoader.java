package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.ResourceLocation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

public class MeshLoader
{
    private MeshModel currentModel;
    private MeshPart currentPart;
    private MaterialLibrary currentMatLib;

    int firstIndex;
    int lastIndex;

    private String filePath;

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

        float z = args[2] == "\\\\"
                ? (float)Math.sqrt(1 - x * x - y * y)
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

        if(args.length < 3 || args.length > 4)
            throw new NotImplementedException();

        String[] p1 = args[0].split("/");
        String[] p2 = args[1].split("/");
        String[] p3 = args[2].split("/");

        int[] v1 = parseIndices(p1);
        int[] v2 = parseIndices(p2);
        int[] v3 = parseIndices(p3);

        if (args.length == 3)
        {
            currentPart.addTriangleFace(v1, v2, v3);
        }
        else if (args.length == 4)
        {
            String[] p4 = args[3].split("/");
            int[] v4 = parseIndices(p4);;

            currentPart.addQuadFace(v1, v2, v3, v4);
        }
    }

    private int[] parseIndices(String[] p1) {
        int[] indices = new int[p1.length];
        for(int i=0;i<p1.length;i++) {
            indices[i] = Integer.parseInt(p1[0])-1;
        }
        return indices;
    }

    private void useMaterial(String matName)
    {
        Material mat = currentMatLib.get(matName);
        currentPart.material = mat;
    }

    private void newGroup(String line)
    {
        currentPart = new MeshPart();
        currentModel.addPart(currentPart);
    }

    private void loadMaterialLibrary(ResourceLocation locOfParent, String path) throws IOException
    {
        ResourceLocation loc = new ResourceLocation(locOfParent.getResourceDomain(), path);

        currentMatLib.loadFromStream(loc);
    }

    public MeshModel loadFromResource(ModelManager modelManager, ResourceLocation loc) throws IOException
    {
        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(loc);
        InputStreamReader lineStream = new InputStreamReader(res.getInputStream(), Charsets.UTF_8);
        BufferedReader lineReader = new BufferedReader(lineStream);

        currentModel = new MeshModel(modelManager);
        currentMatLib = new MaterialLibrary();

        {
            for(;;)
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

                if (keyword.equalsIgnoreCase("o")) { newGroup(data); }
                else if (keyword.equalsIgnoreCase("g")) { newGroup(data); }
                else if (keyword.equalsIgnoreCase("mtllib")) { loadMaterialLibrary(loc, data); }
                else if (keyword.equalsIgnoreCase("usemtl")) { useMaterial(data); }
                else if (keyword.equalsIgnoreCase("v")) { addPosition(data); }
                else if (keyword.equalsIgnoreCase("vn")) { addNormal(data); }
                else if (keyword.equalsIgnoreCase("vt")) { addTexCoord(data); }
                else if (keyword.equalsIgnoreCase("f")) { addFace(data); }
                else
                {
                    System.out.println("Unrecognized command: " + currentLine);
                    continue;
                }
            }
        }

        return currentModel;
    }
}

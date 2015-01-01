package gigaherz.elementsofpower.models;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Mesh {
    List<Vector3f> positions;
    List<Vector3f> normals;
    List<Vector2f> texCoords;
    List<Integer> indices;

    public Mesh() {
        positions = new ArrayList<Vector3f>();
        normals = new ArrayList<Vector3f>();
        texCoords = new ArrayList<Vector2f>();
        indices = new ArrayList<Integer>();
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

    public void addFace(int x, int y, int z) {
        indices.add(x);
        indices.add(y);
        indices.add(z);
    }

    public void Render() {
        GL11.glBegin(GL11.GL_TRIANGLES);

        for (int i : indices) {
            Vector3f pos = positions.get(i);
            Vector3f nrm = normals.get(i);
            Vector2f tc = texCoords.get(i);

            GL11.glTexCoord2f(tc.x, tc.y);
            GL11.glNormal3f(nrm.x, nrm.y, nrm.z);
            GL11.glVertex3f(pos.x, pos.y, pos.z);
        }

        GL11.glEnd();
    }
}

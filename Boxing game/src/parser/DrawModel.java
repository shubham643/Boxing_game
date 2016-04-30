package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import com.jogamp.opengl.GL2;

public class DrawModel {
    private final int NUM_FACE_VERTICES = 3;
    private final int NUM_VERTEX_COORDS = 3;
    private final int NUM_TEX_COORDS = 2;
    private final FloatBuffer mVertexBuffer;
    private final ShortBuffer mIndexBuffer;
    public DrawModel(BufferedReader br) {

        // read in all the lines and put in their respective arraylists of strings
        // reason I do this is to get a count of the faces to be used to initialize the
        // float arrays
        ArrayList<String> vertexes = new ArrayList<String>();
        ArrayList<String> textures = new ArrayList<String>();
        ArrayList<String> faces = new ArrayList<String>();

        //InputStream iStream = context.getResources().openRawResource(resId);
        String line;
        try {
            while ((line = br.readLine()) != null) {
            	System.out.println(line);
                // do not read in the leading v, vt or f
                if (line.startsWith("v ")) vertexes.add(line.substring(2));
                if (line.startsWith("vt ")) textures.add(line.substring(3));
                if (line.startsWith("f ")) faces.add(line.substring(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        faces.size();
        // holding arrays for the vertices, texture coords and indexes
        float[] vCoords = new float[faces.size() * NUM_FACE_VERTICES * NUM_VERTEX_COORDS];
        float[] tCoords = new float[faces.size() * NUM_FACE_VERTICES * NUM_TEX_COORDS];
        short[] iCoords = new short[faces.size() * NUM_FACE_VERTICES];

        int vertexIndex = 0;
        int faceIndex = 0;
        int textureIndex = 0;
        // for each face
        for (String i : faces) {
            // for each face component
            for (String j : i.split(" ")) {
                iCoords[faceIndex] = (short) faceIndex++;
                String[] faceComponent = j.split("/");

                String vertex = vertexes.get(Integer.parseInt(faceComponent[0]) - 1);
                String texture = textures.get(Integer.parseInt(faceComponent[1]) - 1);
                String vertexComp[] = vertex.split(" ");
                String textureComp[] = texture.split(" ");

                for (String v : vertexComp) {
                    vCoords[vertexIndex++] = Float.parseFloat(v);
                }

                for (String t : textureComp) {
                    tCoords[textureIndex++] = Float.parseFloat(t);
                }
            }
        }

        // create the final buffers
        mVertexBuffer = makeFloatBuffer(vCoords);
        mIndexBuffer = makeShortBuffer(iCoords);
        makeFloatBuffer(tCoords);

    }

    private FloatBuffer makeFloatBuffer(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    private ShortBuffer makeShortBuffer(short[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer ib = bb.asShortBuffer();
        ib.put(arr);
        ib.position(0);
        return ib;
    }

    public void draw(GL2 gl) {
        
        gl.glFrontFace(GL2.GL_CCW);    // Front face in counter-clockwise orientation
        gl.glEnable(GL2.GL_CULL_FACE); // Enable cull face
        gl.glCullFace(GL2.GL_BACK);    // Cull the back face (don't display)
        
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        //gl.glColor4f(0.5f, 0.5f, 0.1f, 1.0f);
        gl.glVertexPointer(NUM_VERTEX_COORDS, GL2.GL_FLOAT, 0, mVertexBuffer);
        //gl.glTexCoordPointer(NUM_TEX_COORDS, GL10.GL_FLOAT, 0, mTexBuffer);
        gl.glDrawElements(GL2.GL_TRIANGLES, mIndexBuffer.remaining(), GL2.GL_UNSIGNED_SHORT, mIndexBuffer);
        //gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

    }
}


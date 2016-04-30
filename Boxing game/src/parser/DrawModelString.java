package parser;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.jogamp.opengl.GL2;

public class DrawModelString {
	private final int NUM_FACE_VERTICES = 3;
	private final int NUM_VERTEX_COORDS = 3;
	private final int NUM_TEX_COORDS = 2;
	private final FloatBuffer mVertexBuffer;
	private final FloatBuffer mColorBuffer;
	private final IntBuffer mIndexBuffer;
	private FileInputStream fis;
	private FileInputStream fis1;

	public DrawModelString(String s) {

		// read in all the lines and put in their respective arraylists of
		// strings
		// reason I do this is to get a count of the faces to be used to
		// initialize the
		// float arrays
		ArrayList<String> vertexes = new ArrayList<String>();
		ArrayList<String> textures = new ArrayList<String>();
		ArrayList<String> faces = new ArrayList<String>();
		HashMap<String, Color> hm = new HashMap<String, Color>();
		try {
			fis = new FileInputStream(new File("res\\raw\\objs\\" + s + ".obj"));
			fis1 = new FileInputStream(new File("res\\raw\\mtls\\" + s + ".mtl"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		BufferedReader br1 = new BufferedReader(new InputStreamReader(fis1));
		int colorindex = 0, face = 0;
		float r, g, b, a;
		String line, c, sim = null;
		Color tf = null ;
		try {
			while ((line = br1.readLine()) != null) {
				if (line.startsWith("newmtl ")) {
					c = line.substring(7);
					while ((line = br1.readLine()) != null) {
						if (line.startsWith("Kd "))
							break;
					}
					if (line.startsWith("Kd ")) {
						System.out.println(line.substring(3));
						String[] col = line.substring(3).split(" ");
						r = Float.parseFloat(col[0]);
						g = Float.parseFloat(col[1]);
						b = Float.parseFloat(col[2]);
						a = 1.0f;
						Color p = new Color(r, g, b, a);
						//TheFour tf2 = new TheFour(r, g, b, a);
						System.out.println("!!!!" + p.getRed() + " "
								+ p.getGreen() + " " + p.getBlue());
						hm.put(c, p);
						
						System.out.println("!pk!" + hm.get(c).getRed() + " "
								+ hm.get(c).getGreen() + " "
								+ hm.get(c).getBlue());
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				// do not read in the leading v, vt or f
				if (line.startsWith("v "))
					vertexes.add(line.substring(2));
				if (line.startsWith("vt "))
					textures.add(line.substring(3));
				if (line.startsWith("f ") || line.startsWith("usemtl ")) {
					if (line.startsWith("f ")) {
						face++;
						faces.add(line.substring(2));
					} else {
						faces.add("!" + line.substring(7));
					}

				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String sd : faces) {
			System.out.println("!" + sd);
		}

		faces.size();
		// holding arrays for the vertices, texture coords and indexes
		float[] vCoords = new float[face * NUM_FACE_VERTICES
				* NUM_VERTEX_COORDS];
		float[] tCoords = new float[face * NUM_FACE_VERTICES * NUM_TEX_COORDS];
		int[] iCoords = new int[face * NUM_FACE_VERTICES];
		float[] iColors = new float[face * NUM_FACE_VERTICES * 4];

		int vertexIndex = 0;
		int faceIndex = 0;
		int textureIndex = 0;
		// for each face
		for (String i : faces) {
			// for each face component
			if (i.startsWith("!")) {
				sim = i.substring(1);
				tf = hm.get(sim);
				System.out.println("@" + sim + "@");
			} else {
				for (String j : i.split(" ")) {

					iCoords[faceIndex] = faceIndex++;
					iColors[colorindex++] = tf.getRed()/255.0f;
					iColors[colorindex++] = tf.getGreen()/255.0f;
					iColors[colorindex++] = tf.getBlue()/255.0f;
					iColors[colorindex++] = tf.getAlpha()/255.0f;
					System.out.println("!@!" + tf.getRed() + " "
							+ tf.getGreen() + " " + tf.getBlue());
					String[] faceComponent = j.split("/");

					String vertex = vertexes.get(Integer
							.parseInt(faceComponent[0]) - 1);
					String texture = textures.get(Integer
							.parseInt(faceComponent[1]) - 1);
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
		}

		// create the final buffers
		mVertexBuffer = makeFloatBuffer(vCoords);
		mIndexBuffer = makeIntBuffer(iCoords);
		mColorBuffer = makeFloatBuffer(iColors);
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

	private IntBuffer makeIntBuffer(int[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer ib = bb.asIntBuffer();
		ib.put(arr);
		ib.position(0);
		return ib;
	}

	public void draw(GL2 gl) {

		gl.glFrontFace(GL2.GL_CCW); // Front face in counter-clockwise
									// orientation
		gl.glEnable(GL2.GL_CULL_FACE); // Enable cull face
		gl.glCullFace(GL2.GL_BACK); // Cull the back face (don't display)

		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		// gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		// gl.glColor4f(0.5f, 0.5f, 0.1f, 1.0f);
		gl.glVertexPointer(NUM_VERTEX_COORDS, GL2.GL_FLOAT, 0, mVertexBuffer);
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, mColorBuffer);
		// gl.glTexCoordPointer(NUM_TEX_COORDS, GL10.GL_FLOAT, 0, mTexBuffer);
		gl.glDrawElements(GL2.GL_TRIANGLES, mIndexBuffer.remaining(),
				GL2.GL_UNSIGNED_INT, mIndexBuffer);
		// gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

	}
}

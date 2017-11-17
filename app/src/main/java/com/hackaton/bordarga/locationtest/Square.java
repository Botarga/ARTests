package com.hackaton.bordarga.locationtest;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by botarga on 15/11/2017.
 */

public class Square {
    private final static int CORDS_PER_VERTEX = 3;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    private final static String vertexShaderSource =
            "attribute vec3 vPosition;\n" +
                    "uniform mat4 model;\n" +
                    "uniform mat4 view;\n" +
                    "uniform mat4 projection;\n" +
                    "void main()\n" +
                    "{\n" +
                    "\tgl_Position = projection * view * model * vec4(vPosition, 1.0);\n" +
                    "}";
    private final static String fragmentShaderSource = "precision mediump float;\n" +
            "uniform vec4 vColor;\n" +
            "void main(){\n" +
            "gl_FragColor = vColor;\n" +
            "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -1.0f,  1.0f, 0.0f,   // top left
            -1.0f, -1.0f, 0.0f,   // bottom left
            1.0f, -1.0f, 0.0f,   // bottom right
            1.0f,  1.0f, 0.0f }; // top right

    private short[] drawOrder = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private float[] position;
    private float[] rotation;
    private float[] color;
    private float[] scale;
    private int mProgram;

    public Square(float[] aColor, float[] aPosition, float[] aRotation, float[] aScale) {
        color = aColor;
        position = aPosition;
        rotation = aRotation;
        scale = aScale;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        loadShaders();
    }

    private void loadShaders(){
        int vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(){
        GLES20.glUseProgram(mProgram);

        float[] modelMatrix = new float[16];

        float[] matTranslation = new float[16];
        float[] matRotation = new float[16];
        float[] matScale = new float[16];

        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.setIdentityM(matTranslation, 0);
        Matrix.translateM(matTranslation, 0, position[0], position[1], position[2]);
        Matrix.setIdentityM(matRotation, 0);
        Matrix.setIdentityM(matScale, 0);
        Matrix.scaleM(matScale, 0, scale[0], scale[1], scale[2]);
        if(rotation[0] > 0.0f)
            Matrix.rotateM(matRotation, 0, rotation[0], rotation[1], rotation[2], rotation[3]);

        Matrix.multiplyMM(modelMatrix, 0, matTranslation, 0, matRotation, 0);
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, matScale, 0);


        int modelLocation = GLES20.glGetUniformLocation(mProgram, "model");
        int viewLocation = GLES20.glGetUniformLocation(mProgram, "view");
        int projectionLocation = GLES20.glGetUniformLocation(mProgram, "projection");

        GLES20.glUniformMatrix4fv(modelLocation, 1, false, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(viewLocation, 1, false, MyRenderer.mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(projectionLocation, 1, false, MyRenderer.mProjectionMatrix, 0);


        int vertexAttribPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(vertexAttribPosition);
        GLES20.glVertexAttribPointer(vertexAttribPosition, 3, GLES20.GL_FLOAT, false,
                4 * CORDS_PER_VERTEX, vertexBuffer);

        int uniformColorLocation = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(uniformColorLocation, 1, color, 0);



        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(vertexAttribPosition);
    }

    public void setPosition(float[] pos){
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];
    }
}

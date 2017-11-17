package com.hackaton.bordarga.locationtest;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by botarga on 15/11/2017.
 */

public class MyRenderer implements GLSurfaceView.Renderer{
    public enum InputType{
        MOVE_UP, MOVE_DOWN, MOVE_FORWARD, MOVE_BACKWARD, MOVE_RIGHT, MOVE_LEFT, TURN_RIGHT, TURN_LEFT,
        TURN_UP, TURN_DOWN
    }

    ArrayList<Triangle> triangles = new ArrayList<>(10);
    ArrayList<Square> squares = new ArrayList<>(10);
    private static String errorLog = null;

    public static float[] mProjectionMatrix = new float[16];
    public static float[] mViewMatrix = new float[16];
    private float[] rotationCamera = new float[16];

    float[] cameraPos   = new float[]{0.0f, 1.8f,  3.0f};
    float[] cameraFront = new float[]{0.0f, 0.0f, 0.0f};
    float[] cameraUp = new float[]{0.0f, 1.0f, 0.0f};

    float yaw   = -90.0f;	// yaw is initialized to -90.0 degrees since a yaw of 0.0 results in a direction vector pointing to the right so we initially rotate a bit to the left.
    float pitch =  0.0f;
    float fov   =  45.0f;

    float fixY = 0.40f;
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.2f, 0.5f, 0.8f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        triangles.addAll(Arrays.asList(
            new Triangle(
                new float[]{0.0f, 1.0f, 0.0f, 1.0f},
                new float[]{0.0f, 2.0f, 0.0f},
                new float[]{0.0f, 0.0f, 0.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f}
            ),
            new Triangle(
                new float[]{0.0f, 1.0f, 0.0f, 1.0f},
                new float[]{3.0f, 2.0f, 0.0f},
                new float[]{90.0f, 0.0f, 1.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f}
            )
            /*new Triangle(
                new float[]{0.0f, 1.0f, 0.0f, 1.0f},
                new float[]{0.0f, 4.0f, -3.0f},
                new float[]{0.0f, 0.0f, 0.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f}
            ),
            new Triangle(
                new float[]{0.0f, 1.0f, 0.0f, 1.0f},
                new float[]{0.0f, 4.0f, -3.0f},
                new float[]{0.0f, 0.0f, 0.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f}
            )*/
        ));
        squares.addAll(Arrays.asList(
            new Square(
                new float[]{0.3f, 0.8f, 0.1f, 0.5f},
                new float[]{0.0f, 0.01f, 0.0f},
                new float[]{90.0f, 1.0f, 0.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f}
            ),
            new Square(
                new float[]{0.6f, 0.6f, 1.0f, 1.0f},
                new float[]{0.0f, 0.0f, 0.0f},
                new float[]{90.0f, 1.0f, 0.0f, 0.0f},
                new float[]{100.0f, 100.0f, 100.0f}
            )
        ));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.perspectiveM(mProjectionMatrix, 0, fov, (float)(width / height), 0.1f, 100.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0, cameraPos[0], cameraPos[1], cameraPos[2],
                cameraPos[0] + cameraFront[0], cameraPos[1] + cameraFront[1], cameraPos[2] + cameraFront[2],
                cameraUp[0], cameraUp[1], cameraUp[2]);



         //Matrix.multiplyMM(mViewMatrix, 0, rotationCamera, 0, mViewMatrix, 0);

        for (Triangle t: triangles) {
            t.draw();
        }
        for(Square s : squares)
            s.draw();
    }

    public static int loadShader(int type, String source){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        String infoLog = GLES20.glGetShaderInfoLog(shader);
        errorLog = infoLog.isEmpty()? null : infoLog;

        return shader;
    }

    public void processInput(InputType type){
        float cameraSpeed = 0.1f;
        float sensitivity = 2.0f;

        switch (type){
            case MOVE_UP:
                cameraPos = sumVector(cameraPos, multiplyVector(cameraSpeed, cameraUp));
                break;

            case MOVE_DOWN:
                cameraPos = substractVector(cameraPos, multiplyVector(cameraSpeed, cameraUp));
                break;

            case MOVE_RIGHT:
                cameraPos = sumVector(cameraPos, multiplyVector(cameraSpeed, normalizeVector(crossProduct(cameraFront, cameraUp))));
                break;

            case MOVE_LEFT:
                cameraPos = substractVector(cameraPos, multiplyVector(cameraSpeed, normalizeVector(crossProduct(cameraFront, cameraUp))));
                break;

            case MOVE_BACKWARD:
                cameraPos = substractVector(cameraPos, multiplyVector(cameraSpeed, cameraFront));
                break;

            case MOVE_FORWARD:
                cameraPos = sumVector(cameraPos, multiplyVector(cameraSpeed, cameraFront));
                break;

            default:
                switch(type) {
                    case TURN_DOWN:
                        pitch -= sensitivity;
                        break;

                    case TURN_LEFT:
                        yaw -= sensitivity;
                        break;

                    case TURN_RIGHT:
                        yaw += sensitivity;
                        break;

                    case TURN_UP:
                        pitch += sensitivity;
                        break;
                }

                float[] front = new float[3];
                front[0] = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                front[1] = (float) Math.sin(Math.toRadians(pitch));
                front[2] = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                cameraFront = normalizeVector(front);
            break;
        }
    }



    public String getErrorLog(){
        return errorLog;
    }

    public float[] multiplyVector(float factor, float[] v){
        float[] r = new float[v.length];

        for(int i = 0; i < v.length; ++i)
            r[i] = v[i] * factor;
        return r;
    }

    public float[] sumVector(float[] v1, float[] v2){
        if(v1.length == v2.length){
            float[] r = new float[v1.length];
            for(int i = 0; i < r.length; ++i)
                r[i] = v1[i] + v2[i];

            return r;
        }

        return null;
    }

    public float[] substractVector(float[] v1, float[] v2){
        if(v1.length == v2.length){
            float[] r = new float[v1.length];
            for(int i = 0; i < r.length; ++i)
                r[i] = v1[i] - v2[i];

            return r;
        }

        return null;
    }

    public float[] normalizeVector(float[] v){
        float[] r = new float[v.length];

        float divisor = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        r[0] = v[0] / divisor;
        r[1] = v[1] / divisor;
        r[2] = v[2] / divisor;

        return r;
    }

    public float[] crossProduct(float[] v1, float[] v2){
        if(v1.length == v2.length){
            float[] r = new float[v1.length];
            r[0] = v1[1] * v2[2] - v2[1] * v1[2];
            r[1] = v2[0] * v1[2] - v1[0] * v2[2];
            r[2] = v1[0] * v2[1] - v2[0] * v1[1];

            return r;
        }
        return null;
    }

    public Square getSquare(int i){
        return squares.get(i);
    }

    public void setRotationCamera(float[] m){
        rotationCamera = m;
    }

    public void setCameraFront(float[] v){
        cameraFront[0] = v[0];
        cameraFront[1] = v[1];
        cameraFront[2] = v[2];
    }


    public void setYawPitchRoll(float[] v){
        yaw = v[0];
        pitch = v[1];

        float[] front = new float[3];
        front[0] = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front[1] = (float) Math.sin(Math.toRadians(pitch));
        front[2] = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        cameraFront = normalizeVector(front);
    }
}

package kr.ac.hallym.term_project

import android.content.Context
import android.media.Image
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class MyLitTexCube(val myContext: Context) {

    private val drawOrder = intArrayOf(
        0, 3, 2, 0, 2, 1, // back
        2, 3, 7, 2, 7, 6, // right-side
        1, 2, 6, 1, 6, 5, // bottom
        4, 0, 1, 4, 1, 5, // left-side
        3, 0, 4, 3, 4, 7, // top
        5, 6, 7, 5, 7, 4  // front
    )

    private val vertexCoords = FloatArray(108).apply {
        val vertex = arrayOf(
            floatArrayOf(-0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, 0.5f, 0.5f),
            floatArrayOf(-0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, 0.5f, 0.5f)
        )
        var index = 0
        for (i in 0 .. 35) {
            this[index++] = vertex[drawOrder[i]][0]
            this[index++] = vertex[drawOrder[i]][1]
            this[index++] = vertex[drawOrder[i]][2]
        }
    }

    private val vertexNormals = FloatArray(108).apply {
        val normals = arrayOf(
            floatArrayOf(-0.57735f, 0.57735f, -0.57735f),
            floatArrayOf(-0.57735f, -0.57735f, -0.57735f),
            floatArrayOf(0.57735f, -0.57735f, -0.57735f),
            floatArrayOf(0.57735f, 0.57735f, -0.57735f),
            floatArrayOf(-0.57735f, 0.57735f, 0.57735f),
            floatArrayOf(-0.57735f, -0.57735f, 0.57735f),
            floatArrayOf(0.57735f, -0.57735f, 0.57735f),
            floatArrayOf(0.57735f, 0.57735f, 0.57735f)
        )
        var index = 0
        for (i in 0 .. 35) {
            this[index++] = normals[drawOrder[i]][0]
            this[index++] = normals[drawOrder[i]][1]
            this[index++] = normals[drawOrder[i]][2]
        }
    }

    private val vertexUVs = FloatArray(72).apply {
        val UVs = arrayOf(
            floatArrayOf(0.0f, 0.0f),
            floatArrayOf(0.0f, 1.0f),
            floatArrayOf(1.0f, 1.0f),
            floatArrayOf(0.0f, 0.0f),
            floatArrayOf(1.0f, 1.0f),
            floatArrayOf(1.0f, 0.0f),
        )
        var index = 0
        for (i in 0 .. 5) {
            for (j in 0 .. 5) {
                this[index++] = UVs[j][0]
                this[index++] = UVs[j][1]
            }
        }
    }

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float) | 정점 갯수 * 4바이트
        ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
            // use the device hardware's native byte order | 디바이스 하드웨어의 기본 바이트 순서 사용
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer | 바이트 버퍼로부터 floating pont buffer를 생성
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer | 정점들을 FloatBuffer에 추가
                put(vertexCoords)
                // set the buffer to read the first coordinate | 버퍼가 첫번째 정점을 읽도록 설정
                position(0)
            }
        }

    private var normalBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexNormals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexNormals)
                position(0)
            }
        }

    private var uvBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexUVs.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexUVs)
                position(0)
            }
        }

    private val matAmbient = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matShininess = 10.0f

    private var mProgram: Int = -1

    private var mEyePosHandle: Int = - 1;
    private var mColorHandle: Int = -1
    private var mLightDirHandle: Int = -1
    private var mLightAmbiHandle: Int = -1
    private var mLightDiffHandle: Int = -1
    private var mLightSpecHandle: Int = -1
    private var mMatAmbiHandle: Int = -1
    private var mMatSpecHandle: Int = -1
    private var mMatShHandle: Int = -1

    private var mvpMatrixHandle: Int = -1
    private var mWorldMatHandle: Int = -1

    private var textureID = IntArray(1)

    private val vertexCount: Int = vertexCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    init {
        val vertexShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, "cube_light_tex_vert.glsl", myContext)
        val fragmentShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, "cube_light_tex_frag.glsl", myContext)

        // create empty OpenGL ES Program
        mProgram = GLES30.glCreateProgram().also {

            // add the vertex shader to program
            GLES30.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES30.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES30.glLinkProgram(it)
        }

        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram)

        // get handle to vertex shader's vPosition member
        //mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition").also {

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(9)

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
            9,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        GLES30.glEnableVertexAttribArray(10)
        GLES30.glVertexAttribPointer(
            10,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            normalBuffer
        )

        GLES30.glEnableVertexAttribArray(11)
        GLES30.glVertexAttribPointer(
            11,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            uvBuffer
        )

        mEyePosHandle = GLES30.glGetUniformLocation(mProgram, "eyePos").also {
            GLES30.glUniform3fv(it, 1, eyePos, 0)
        }

        mLightDirHandle = GLES30.glGetUniformLocation(mProgram, "lightDir").also {
            GLES30.glUniform3fv(it, 1, lightDir, 0)
        }

        mLightAmbiHandle = GLES30.glGetUniformLocation(mProgram, "lightAmbi").also {
            GLES30.glUniform3fv(it, 1, lightAmbient, 0)
        }

        mLightDiffHandle = GLES30.glGetUniformLocation(mProgram, "lightDiff").also {
            GLES30.glUniform3fv(it, 1, lightDiffuse, 0)
        }

        mLightSpecHandle = GLES30.glGetUniformLocation(mProgram, "lightSpec").also {
            GLES30.glUniform3fv(it, 1, lightSpecular, 0)
        }

        mMatAmbiHandle = GLES30.glGetUniformLocation(mProgram, "matAmbi").also {
            GLES30.glUniform3fv(it, 1, matAmbient, 0)
        }

        mMatSpecHandle = GLES30.glGetUniformLocation(mProgram, "matSpec").also {
            GLES30.glUniform3fv(it, 1, matSpecular, 0)
        }

        mMatShHandle = GLES30.glGetUniformLocation(mProgram, "matSh").also {
            GLES30.glUniform1f(it, matShininess)
        }
        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "fogStart"), 2.0f)
        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "fogEnd"), 10.0f)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(mProgram, "fogColor"), 0.2f, 0.2f, 0.2f)

        mvpMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        mWorldMatHandle = GLES30.glGetUniformLocation(mProgram, "worldMat")

        GLES30.glGenTextures(1, textureID, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0 , loadBitmap("NIS.jpg", myContext), 0)
    }

    fun setTexture(image: String) {
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0 , loadBitmap(image, myContext), 0)
    }

    fun draw(mvpMatrix: FloatArray, worldMat: FloatArray) {

        GLES30.glUseProgram(mProgram)

        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(mWorldMatHandle, 1, false, worldMat, 0)

        GLES30.glUniform3fv(mLightDirHandle, 1, lightDir, 0)
        GLES30.glUniform3fv(mEyePosHandle, 1, eyePos, 0)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0])

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
    }
}
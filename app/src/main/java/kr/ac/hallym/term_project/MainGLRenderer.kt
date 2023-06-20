package kr.ac.hallym.term_project

import android.content.Context
import android.opengl.GLES30
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import java.io.BufferedInputStream
import kotlin.math.sin
import kotlin.math.cos
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

const val COORDS_PER_VERTEX = 3

var eyePos = floatArrayOf(0.0f, 3.0f, 87.0f)
var eyeAt = floatArrayOf(0.0f, 0.0f, 0.0f)
var cameraVec = floatArrayOf(0.0f, -0.7071f, -0.7071f)

var respawnEyePos = floatArrayOf(0.0f, 3.0f, 87.0f)
var respawnCameraVec = floatArrayOf(0.0f, -0.7071f, -0.7071f)
var obstacleSpeed = 0.01f

val lightDir = floatArrayOf(0.0f, 1.0f, 1.0f)
val lightAmbient = floatArrayOf(0.1f, 0.1f, 0.1f)
val lightDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f)
val lightSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)

var prevPosX = 3.0f
var prevPosZ = 3.0f
var outwardDirX = true
var outwardDirZ = true
var maxZ = 47
var minZ = 35

var objectPos = arrayOf(
    floatArrayOf(3.0f, 0.0f, 72.0f), floatArrayOf(-3.0f, 0.0f, 72.0f),
    floatArrayOf(3.0f, 0.0f, 75.0f), floatArrayOf(-3.0f, 0.0f, 75.0f),
    floatArrayOf(3.0f, 0.0f, 78.0f), floatArrayOf(-3.0f, 0.0f, 78.0f),
    floatArrayOf(3.0f, 0.0f, 81.0f), floatArrayOf(-3.0f, 0.0f, 81.0f),
    floatArrayOf(3.0f, 0.0f, 84.0f), floatArrayOf(-3.0f, 0.0f, 84.0f),

    floatArrayOf(3.0f, 0.0f, 54.0f), floatArrayOf(-3.0f, 0.0f, 54.0f),
    floatArrayOf(3.0f, 0.0f, 57.0f), floatArrayOf(-3.0f, 0.0f, 57.0f),
    floatArrayOf(3.0f, 0.0f, 60.0f), floatArrayOf(-3.0f, 0.0f, 60.0f),
    floatArrayOf(3.0f, 0.0f, 63.0f), floatArrayOf(-3.0f, 0.0f, 63.0f),
    floatArrayOf(3.0f, 0.0f, 66.0f), floatArrayOf(-3.0f, 0.0f, 66.0f),

    floatArrayOf(3.0f, 0.0f, 36.0f), floatArrayOf(-3.0f, 0.0f, 36.0f),
    floatArrayOf(3.0f, 0.0f, 39.0f), floatArrayOf(-3.0f, 0.0f, 39.0f),
    floatArrayOf(3.0f, 0.0f, 42.0f), floatArrayOf(-3.0f, 0.0f, 42.0f),
    floatArrayOf(3.0f, 0.0f, 45.0f), floatArrayOf(-3.0f, 0.0f, 45.0f),
    floatArrayOf(3.0f, 0.0f, 48.0f), floatArrayOf(-3.0f, 0.0f, 48.0f),

    floatArrayOf(3.0f, 0.0f, 18.0f), floatArrayOf(-3.0f, 0.0f, 18.0f),
    floatArrayOf(3.0f, 0.0f, 21.0f), floatArrayOf(-3.0f, 0.0f, 21.0f),
    floatArrayOf(3.0f, 0.0f, 24.0f), floatArrayOf(-3.0f, 0.0f, 24.0f),
    floatArrayOf(3.0f, 0.0f, 27.0f), floatArrayOf(-3.0f, 0.0f, 27.0f),
    floatArrayOf(3.0f, 0.0f, 30.0f), floatArrayOf(-3.0f, 0.0f, 30.0f),
)

var savePointPos = arrayOf(
    floatArrayOf(0.0f, 3.0f, 15.0f), floatArrayOf(0.0f, 3.0f, 33.0f),
    floatArrayOf(0.0f, 3.0f, 51.0f), floatArrayOf(0.0f, 3.0f, 69.0f),
)

class MainGLRenderer (val context: Context): GLSurfaceView.Renderer{

    private lateinit var mGround: MyLitTexGround
    private lateinit var mHexa: MyLitHexa
    private lateinit var mCube0: MyLitCube
    private lateinit var mCube: MyLitTexCube
    private lateinit var mArcball: MyArcball

    private var modelMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)
    private var projectionMatrix = FloatArray(16)
    private var vpMatrix = FloatArray(16)
    private var mvpMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    private var startTime = SystemClock.uptimeMillis()
    private var rotYAngle = 0f
    private var colorChangeTime = 0L
    private var colorFlag = true

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.setIdentityM(vpMatrix, 0)

        mGround = MyLitTexGround(context)
        mHexa = MyLitHexa(context)
        mCube0 = MyLitCube(context)
        mCube = MyLitTexCube(context)
        mArcball = MyArcball()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        mArcball.resize(width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 90f, ratio, 0.001f, 1000f)

        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], eyeAt[0], eyeAt[1], eyeAt[2], 0f, 1f, 0f)

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        val currentTime = SystemClock.uptimeMillis()
        val elapsedTime = currentTime - colorChangeTime

        if (elapsedTime >= 500.0f) {
            colorChangeTime = currentTime

            val redColor = floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f)
            val blueColor = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)

            if (colorFlag) {
                colorFlag = false
                mCube0.setColor(redColor)
            } else {
                colorFlag = true
                mCube0.setColor(blueColor)
            }
        }

        eyeAt[0] = eyePos[0] + cameraVec[0]
        eyeAt[1] = eyePos[1] + cameraVec[1]
        eyeAt[2] = eyePos[2] + cameraVec[2]
        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], eyeAt[0], eyeAt[1], eyeAt[2], 0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        var rotTexMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotTexMatrix, 0, 90f, 0f, 1f, 0f)

        for (z in 15 .. 69 step 18) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, 0f, 0f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotTexMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.setTexture("savepoint.jpg")
            mCube.draw(mvpMatrix, modelMatrix)
        }

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, eyePos[0], 0f, eyePos[2])
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotTexMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        mCube.setTexture("answersheet.jpg")
        mCube.draw(mvpMatrix, modelMatrix)
        mCube.setTexture("NIS.jpg")

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        mGround.draw(mvpMatrix, modelMatrix)

        val endTime = SystemClock.uptimeMillis()
        val angle = 0.1f * (endTime - startTime).toFloat()
        startTime = endTime
        rotYAngle += angle
        var rotYMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotYMatrix, 0, rotYAngle, 0f, 1f, 0f)

        var rotMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotMatrix, 0, 45f, 0f, 0f, 1f)
        Matrix.multiplyMM(rotMatrix, 0, rotYMatrix, 0, rotMatrix, 0)

        lightDir[0] = sin(rotYAngle * 0.01f)
        lightDir[2] = cos(rotYAngle * 0.01f)

        val posX: Float
        if (outwardDirX) {
            posX = prevPosX + angle * obstacleSpeed
        } else {
            posX = prevPosX - angle * obstacleSpeed
        }
        if (posX > 9) {
            outwardDirX = false
        } else if (posX < -9) {
            outwardDirX = true
        }
        prevPosX = posX

        val posZ: Float
        if (outwardDirZ) {
            posZ = prevPosZ + angle * obstacleSpeed
        } else {
            posZ = prevPosZ - angle * obstacleSpeed
        }
        if (posZ > maxZ) {
            outwardDirZ = false
        } else if (posZ < minZ) {
            outwardDirZ = true
        }
        prevPosZ = posZ

        var objectId = (4 - ((respawnEyePos[2] - 15) / 18).toInt()) * 10
        if (((respawnEyePos[2] - 15) / 18).toInt() == 4) {
            for (z in 72 .. 84 step 3) {
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, posX, 0f, z.toFloat())
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotTexMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId++][0] = posX

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, posX, 1.5f, z.toFloat())
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -posX, 0f, z.toFloat() + 0.5f)
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotTexMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId++][0] = -posX

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -posX, 1.5f, z.toFloat() + 0.5f)
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)
            }
        }

        if (((respawnEyePos[2] - 15) / 18).toInt() == 3) {
            for (z in 54..66 step 3) {
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, posX, 0f, z.toFloat())
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId++][0] = posX

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, posX, 1.5f, z.toFloat())
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -posX, 0f, z.toFloat())
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId++][0] = -posX

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -posX, 1.5f, z.toFloat())
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)
            }
        }

        if (((respawnEyePos[2] - 15) / 18).toInt() == 2) {
            for (z in 36..48 step 3) {
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, (z % 11).toFloat(), 0f, posZ)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId][0] = posX
                objectPos[objectId++][2] = posZ

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, (z % 11).toFloat(), 1.5f, posZ)
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -(z % 11).toFloat(), 0f, posZ + 6)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId][0] = -posX
                objectPos[objectId++][2] = -posZ

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -(z % 11).toFloat(), 1.5f, posZ + 6)
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)
            }
        }

        if (((respawnEyePos[2] - 15) / 18).toInt() == 1) {
            maxZ = 30
            minZ = 18
            for (z in 18..30 step 3) {
                println(posZ)
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, posX, 0f, posZ)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId][0] = posX
                objectPos[objectId++][2] = posZ

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, posX, 1.5f, posZ)
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -posX, 0f, posZ)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube.draw(mvpMatrix, modelMatrix)

                objectPos[objectId][0] = -posX
                objectPos[objectId++][2] = -posZ

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -posX, 1.5f, posZ)
                Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mCube0.draw(mvpMatrix, modelMatrix)
            }
        }

        if (((respawnEyePos[2] - 15) / 18).toInt() == 0) {
            for (z in 0..12 step 3) {
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, -3f, 0f, z.toFloat())
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mHexa.draw(mvpMatrix, modelMatrix)

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, 0f, 0f, z.toFloat())
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mHexa.draw(mvpMatrix, modelMatrix)

                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, 3f, 0f, z.toFloat())
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mHexa.draw(mvpMatrix, modelMatrix)
            }
        }

        if (detectCollision(eyePos[0], eyePos[2])) {
            eyePos[0] = respawnEyePos[0]
            eyePos[1] = respawnEyePos[1]
            eyePos[2] = respawnEyePos[2]
            cameraVec[0] = respawnCameraVec[0]
            cameraVec[1] = respawnCameraVec[1]
            cameraVec[2] = respawnCameraVec[2]
        }
        // 0 ~ 12 |15| 18 ~ 30 |33| 36 ~ 48 |51| 54 ~ 66 |69| 72 ~ 84
        if (savePoint(eyePos[0], eyePos[2])) {
            if ((eyePos[2].toInt() - 15) % 18 == 0) {
                respawnEyePos[0] = savePointPos[((eyePos[2].toInt() - 15) / 18)][0]
                respawnEyePos[1] = savePointPos[((eyePos[2].toInt() - 15) / 18)][1]
                respawnEyePos[2] = savePointPos[((eyePos[2].toInt() - 15) / 18)][2]
                respawnCameraVec[0] = 0.0f
                respawnCameraVec[1] = -0.7071f
                respawnCameraVec[2] = -0.7071f
            }
        }

        obstacleSpeed = 0.1f / ((respawnEyePos[2] - 15) / 18)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> mArcball.start(x, y)
            MotionEvent.ACTION_MOVE -> {
                mArcball.end(x, y)
                cameraRotate(mArcball.rotationMatrix)
            }
        }
        return true
    }
}

fun loadShader(type: Int, filename: String, myContext: Context): Int {
    return GLES30.glCreateShader(type).also { shader ->

        val inputStream = myContext.assets.open(filename)
        val inputBuffer = ByteArray(inputStream.available())
        inputStream.read(inputBuffer)
        val shaderCode = String(inputBuffer)

        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        val compiled = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled)
        if (compiled.get(0) == 0) {
            GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, compiled)
            if (compiled.get(0) > 1) {
                Log.e("Shader", "$type shader: " + GLES30.glGetShaderInfoLog(shader))
            }
            GLES30.glDeleteShader(shader)
            Log.e("Shader", "$type shader compile error.")
        }
    }
}

fun loadBitmap(filename: String, myContext: Context): Bitmap {
    val manager = myContext.assets
    val inputStream = BufferedInputStream(manager.open(filename))
    val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
    return bitmap!!
}

fun cameraRotate(rotationMatrix: FloatArray) {
    val newVecX = sin(rotationMatrix[8]) * cameraVec[2] + cos(rotationMatrix[10]) * cameraVec[0]
    cameraVec[0] = newVecX
}

fun cameraMove(distance: Float) {
    val newPosX = eyePos[0] + distance * cameraVec[0]
    val newPosZ = eyePos[2] + distance * cameraVec[2]
    if (!detectCollision(newPosX, newPosZ)) {
        eyePos[0] = newPosX
        eyePos[2] = newPosZ
    }
}

fun detectCollision(newPosX: Float, newPosZ: Float): Boolean {
    if (newPosX < -10 || newPosX > 10 || newPosZ < -30 || newPosZ > 90) {
        println("Your on the end of the ground")
        return true
    }

    for (i in 0 .. objectPos.size - 1) {
        if (abs(newPosX - objectPos[i][0]) < 1.3f && abs(newPosZ - objectPos[i][2]) < 1.3f) {
            println("***** detection of collision at($newPosX, 0, $newPosZ) *****")
            return true
        }
    }
    return false
}

fun savePoint(newPosX: Float, newPosZ: Float): Boolean {
    for (i in 0 .. savePointPos.size - 1) {
        if (abs(newPosX - savePointPos[i][0]) < 1.0f && abs(newPosZ - savePointPos[i][2]) < 1.0f) {
            println("***** detection of savePoint collision at($newPosX, 0, $newPosZ) *****")
            return true
        }
    }
    return false
}
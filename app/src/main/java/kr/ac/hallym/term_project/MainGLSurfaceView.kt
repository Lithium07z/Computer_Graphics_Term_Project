package kr.ac.hallym.term_project

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class MainGLSurfaceView (context: Context): GLSurfaceView(context) {

    private val mainRenderer: MainGLRenderer

    init {
        setEGLContextClientVersion(3)

        mainRenderer = MainGLRenderer(context)

        setRenderer(mainRenderer)

        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}
package kr.ac.hallym.term_project

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.ac.hallym.term_project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //private lateinit var mainSurfaceView: MainGLSurfaceView

    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //mainSurfaceView = MainGLSurfaceView(this)
        //setContentView(mainSurfaceView)
        supportActionBar?.hide()
        initSurfaceView()
        setContentView(binding.root)

        binding.eyeForward.setOnClickListener {
            cameraMove(0.5f)
            binding.surfaceView.requestRender()
        }
        binding.eyeBackward.setOnClickListener {
            cameraMove(-0.5f)
            binding.surfaceView.requestRender()
        }
    }

    fun initSurfaceView() {
        binding.surfaceView.setEGLContextClientVersion(3)

        val mainRenderer = MainGLRenderer(this)
        binding.surfaceView.setRenderer(mainRenderer)

        binding.surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        binding.surfaceView.setOnTouchListener { v, event ->
            mainRenderer.onTouchEvent(event)
        }
    }
}
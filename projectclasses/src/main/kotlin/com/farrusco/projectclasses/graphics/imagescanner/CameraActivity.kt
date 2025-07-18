package com.farrusco.projectclasses.graphics.imagescanner

import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.activity.BaseActivity
import com.farrusco.projectclasses.common.Constants.ARG_KEY_URI
import com.farrusco.projectclasses.common.Constants.RATIO_16_9_VALUE
import com.farrusco.projectclasses.common.Constants.RATIO_4_3_VALUE
import com.farrusco.projectclasses.utils.Logging
import com.farrusco.projectclasses.messages.ProgressIndicator
import com.farrusco.projectclasses.utils.TextAnalyser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

//typealias CameraTextAnalyzerListener = (text: String) -> Unit

@OptIn(DelicateCoroutinesApi::class)
class CameraActivity : BaseActivity() {

    override val layoutResourceId: Int = R.layout.activity_camera
    override val mainViewId: Int = R.id.viewMain
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private val executor by lazy { Executors.newSingleThreadExecutor() }
    private lateinit var progressIndicator: ProgressIndicator
    private lateinit var camera: Camera
    private var flashEnabled = false
    private lateinit var viewCamera: PreviewView
    private lateinit var btnFlashControl: ImageView
    private lateinit var btnCamera: ImageButton

    override fun initActivity() {
        //super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_camera)
        viewCamera = findViewById(R.id.view_camera)
        btnFlashControl = findViewById(R.id.btn_flashcontrol)
        btnCamera = findViewById(R.id.btn_camera)

        progressIndicator = ProgressIndicator(this, false)

        btnCamera.setOnClickListener {
            progressIndicator.show()
            takePicture()
        }
        btnFlashControl.setImageResource(R.drawable.ic_round_flash_on)
        btnFlashControl.setOnClickListener {
            camera.cameraControl.enableTorch(!flashEnabled)
            flashEnabled=!flashEnabled
            if (flashEnabled) {
                btnFlashControl.setImageResource(R.drawable.ic_round_flash_off)
            } else {
                btnFlashControl.setImageResource(R.drawable.ic_round_flash_on)
            }
        }
        viewCamera.post {
            startCamera()
        }
    }
    private fun takePicture() {
        val file = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        val outputFileOptions =
            ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputFileOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    GlobalScope.launch(Dispatchers.IO) {
                        TextAnalyser({ scanResult ->
                            if (scanResult.isEmpty()) {
                                progressIndicator.dismiss()
                                Toast.makeText(
                                    this@CameraActivity,
                                    getString(R.string.all_txt_no_text_detected),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                progressIndicator.dismiss()
                                //val intent = Intent(this@CameraActivity, CropImageActivity::class.java)
                                intent.putExtra(ARG_KEY_URI, file.absolutePath)
                                this@CameraActivity.setResult(RESULT_OK,intent)
                                //startActivity(intent)
                                finish()
                            }
                        }, this@CameraActivity, Uri.fromFile(file)).analyseImage()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    progressIndicator.dismiss()
                    Logging.e(exception.localizedMessage!!)
                }
            }
        )
    }

    private fun startCamera() {
        val metrics = DisplayMetrics().also {
            @Suppress("DEPRECATION")
            viewCamera.display.getRealMetrics(it)
        }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = viewCamera.display.rotation
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation).build()

            preview.setSurfaceProvider(viewCamera.surfaceProvider)
            imageCapture = initializeImageCapture(screenAspectRatio, rotation)
            cameraProvider.unbindAll()
            try {
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
                cameraControl.setLinearZoom(0.5f)

                if (!camera.cameraInfo.hasFlashUnit()) {
                    btnFlashControl.visibility = View.GONE

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun initializeImageCapture(
        screenAspectRatio: Int,
        rotation: Int
    ): ImageCapture {
        return ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
    }
}
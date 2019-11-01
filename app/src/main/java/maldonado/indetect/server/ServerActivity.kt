package maldonado.indetect.server

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.wonderkiln.camerakit.*
import maldonado.indetect.R
import maldonado.indetect.server.utils.DrawingView

class ServerActivity: AppCompatActivity() {
    private lateinit var btnOk: Button

    private lateinit var cameraView: CameraView
    private lateinit var imageViewTmp: ImageView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvTextResults: TextView
    private lateinit var tvLoadingText: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server)

        cameraView = findViewById(R.id.server_CameraView)
        imageViewTmp = ImageView(this)
        btnOk = findViewById(R.id.server_BtnDetectOk)

        resultDialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_result,
            null)

        resultDialog.setCancelable(false)
        resultDialog.setContentView(dialogView)

        ivImageResult = dialogView.findViewById(R.id.result_IViewResult)
        tvTextResults = dialogView.findViewById(R.id.result_TvResult)
        tvLoadingText = dialogView.findViewById(R.id.result_TvLoadingRecognition)
        aviLoaderHolder = dialogView.findViewById<View>(R.id.result_AviLoaderHolderView)
        tvTextResults.movementMethod = ScrollingMovementMethod()

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) { }

            override fun onError(cameraKitError: CameraKitError) { }

            override fun onImage(cameraKitImage: CameraKitImage) {
                val bitmap = cameraKitImage.bitmap

                recognize(Bitmap.createScaledBitmap(bitmap, (bitmap.width*0.5).toInt(),
                        (bitmap.height*0.5).toInt(), false))
            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) { }
        })

        btnOk.setOnClickListener {
            cameraView.captureImage()
            resultDialog.show()
            tvTextResults.visibility = View.GONE
            ivImageResult.visibility = View.GONE
        }

        resultDialog.setOnDismissListener {
            tvLoadingText.visibility = View.VISIBLE
            aviLoaderHolder.visibility = View.VISIBLE
        }
    }

    private fun recognize(bitmap: Bitmap) {
        aviLoaderHolder.visibility = View.GONE
        tvLoadingText.visibility = View.GONE

        val options = FirebaseVisionObjectDetectorOptions.Builder()
            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        val detector = FirebaseVision.getInstance().getOnDeviceObjectDetector(options)
        val image = FirebaseVisionImage.fromBitmap(bitmap)

        detector.processImage(image)
            .addOnSuccessListener {
                val drawingView = DrawingView(applicationContext, it)
                drawingView.draw(Canvas(bitmap))
            }
            .addOnFailureListener {
                // Task failed with an exception
                Toast.makeText(
                    baseContext, "Oops, something went wrong!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        ivImageResult.setImageBitmap(bitmap)
        tvTextResults.visibility = View.VISIBLE
        ivImageResult.visibility = View.VISIBLE
        resultDialog.setCancelable(true)
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_server, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_car -> {
                return true
            }
            R.id.m_front -> {
                cameraView.toggleFacing()
                return true
            }
            R.id.m_flash -> {
                cameraView.toggleFlash()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
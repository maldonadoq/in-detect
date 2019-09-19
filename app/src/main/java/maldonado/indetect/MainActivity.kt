package maldonado.indetect

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wonderkiln.camerakit.*

class MainActivity : AppCompatActivity() {
    lateinit var textViewResult: TextView
    lateinit var btnDetectObject: Button
    lateinit var btnToggleCamera: Button
    lateinit var btnUploadPhoto:  Button
    lateinit var imageViewResult: ImageView
    lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.cameraView)
        imageViewResult = findViewById(R.id.imageViewResult)
        textViewResult = findViewById(R.id.textViewResult)
        textViewResult.movementMethod = ScrollingMovementMethod()

        btnToggleCamera = findViewById(R.id.btnToggleCamera)
        btnDetectObject = findViewById(R.id.btnDetectObject)
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto)

        val resultDialog = Dialog(this)
        val customProgressView = LayoutInflater.from(this).inflate(R.layout.activity_result, null)
        resultDialog.setCancelable(false)
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        resultDialog.setContentView(customProgressView)

        val ivImageResult = customProgressView.findViewById<ImageView>(R.id.iViewResult)
        val tvLoadingText = customProgressView.findViewById<TextView>(R.id.tvLoadingRecognition)
        val tvTextResults = customProgressView.findViewById<TextView>(R.id.tvResult)


        // The Loader Holder is used due to a bug in the Avi Loader library
        val aviLoaderHolder = customProgressView.findViewById<View>(R.id.aviLoaderHolderView)

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) { }

            override fun onError(cameraKitError: CameraKitError) { }

            override fun onImage(cameraKitImage: CameraKitImage) {

                var bitmap = cameraKitImage.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

                aviLoaderHolder.visibility = View.GONE
                tvLoadingText.visibility = View.GONE

                val results = "Sabpe"
                ivImageResult.setImageBitmap(bitmap)
                tvTextResults.text = results

                tvTextResults.visibility = View.VISIBLE
                ivImageResult.visibility = View.VISIBLE

                resultDialog.setCancelable(true)

            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) { }
        })

        btnToggleCamera.setOnClickListener { cameraView.toggleFacing() }

        btnDetectObject.setOnClickListener {
            cameraView.captureImage()
            resultDialog.show()
            tvTextResults.visibility = View.GONE
            ivImageResult.visibility = View.GONE
        }

        btnUploadPhoto.setOnClickListener {
            Toast.makeText(this, "Hi there! Saluuute.", Toast.LENGTH_LONG).show()
        }

        resultDialog.setOnDismissListener {
            tvLoadingText.visibility = View.VISIBLE
            aviLoaderHolder.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    companion object {
        // private const val LABEL_PATH = "labels.txt"
        private const val INPUT_SIZE = 300
    }
}

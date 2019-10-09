package maldonado.indetect

import android.app.Dialog
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.wonderkiln.camerakit.*
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var btnDetectObject: Button

    private lateinit var btnToggleCamera: Button
    private lateinit var cameraView: CameraView
    private lateinit var imageViewTmp: ImageView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvLoadingText: TextView
    private lateinit var tvTextResults: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    private lateinit var random: Random
    private var btnType = 0

    // model
    private lateinit var objectClassifier: ObjectClassifier

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.cameraView)
        imageViewTmp = ImageView(this)
        btnToggleCamera = findViewById(R.id.btnToggleCamera)
        btnDetectObject = findViewById(R.id.btnDetectObject)

        resultDialog = Dialog(this)
        val customProgressView = LayoutInflater.from(this).inflate(R.layout.activity_result,
            null)

        resultDialog.setCancelable(false)
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        resultDialog.setContentView(customProgressView)

        ivImageResult = customProgressView.findViewById(R.id.iViewResult)
        tvLoadingText = customProgressView.findViewById(R.id.tvLoadingRecognition)
        tvTextResults = customProgressView.findViewById(R.id.tvResult)

        // The Loader Holder is used due to a bug in the Avi Loader library
        aviLoaderHolder = customProgressView.findViewById<View>(R.id.aviLoaderHolderView)

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

        btnToggleCamera.setOnClickListener { cameraView.toggleFacing() }

        btnDetectObject.setOnClickListener {
            btnType = 1
            tvLoadingText.text = "Object Identification Engine Processing ..."

            cameraView.captureImage()
            resultDialog.show()
            tvTextResults.visibility = View.GONE
            ivImageResult.visibility = View.GONE
        }

        resultDialog.setOnDismissListener {
            tvLoadingText.visibility = View.VISIBLE
            aviLoaderHolder.visibility = View.VISIBLE
        }

        random = Random()
        initTensorFlowAndLoadModel()
    }

    private fun recognize(bitmap: Bitmap) {
        aviLoaderHolder.visibility = View.GONE
        tvLoadingText.visibility = View.GONE

        val results = objectClassifier.recognizeImage(bitmap)

        val canvas = Canvas(bitmap)
        val boxPaint = Paint()
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 15.0f

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 50.0f

        for (result in results) {
            boxPaint.color = Color.argb(255, random.nextInt(256), random.nextInt(
                256), random.nextInt(256))
            canvas.drawRoundRect(result.location, 30.0f, 30.0f, boxPaint)

            canvas.drawText(String.format("%s %.2f", result.title, (100 * result.confidence)),
                result.location.left + 40, result.location.top + 60, textPaint)
        }

        ivImageResult.setImageBitmap(bitmap)
        tvTextResults.text = results.toString()

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

    // tensor
    override fun onDestroy() {
        super.onDestroy()
        executor.execute { objectClassifier.close() }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                objectClassifier = ObjectClassifier.create(assets)

                makeButtonVisible()
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }

    private fun makeButtonVisible() {
        runOnUiThread { btnDetectObject.visibility = View.VISIBLE }
    }
}

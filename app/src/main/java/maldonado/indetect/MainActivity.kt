package maldonado.indetect

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
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
    private lateinit var btnDetectCar: Button

    private lateinit var btnToggleCamera: Button
    private lateinit var btnUploadPhoto:  Button
    private lateinit var cameraView: CameraView
    private lateinit var imageViewTmp: ImageView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvLoadingText: TextView
    private lateinit var tvTextResults: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    private lateinit var bitmap: Bitmap
    private lateinit var random: Random
    private var btnType = 0

    // model
    private lateinit var objectClassifier: ObjectClassifier
    // private lateinit var carClassifier: CarClassifier
    private lateinit var flowerClassifier: FlowerClassifier

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraView = findViewById(R.id.cameraView)
        imageViewTmp = ImageView(this)
        btnToggleCamera = findViewById(R.id.btnToggleCamera)
        btnDetectObject = findViewById(R.id.btnDetectObject)
        btnDetectCar = findViewById(R.id.btnDetectCar)
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto)

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
                recognize(cameraKitImage.bitmap)
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

        btnDetectCar.setOnClickListener {
            btnType = -1
            tvLoadingText.text = "Car Identification Engine Processing ..."

            cameraView.captureImage()
            resultDialog.show()
            tvTextResults.visibility = View.GONE
            ivImageResult.visibility = View.GONE
        }

        btnUploadPhoto.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else{
                    pickImageFromGalley()
                }
            }
            else{
                pickImageFromGalley()
            }
        }

        resultDialog.setOnDismissListener {
            tvLoadingText.visibility = View.VISIBLE
            aviLoaderHolder.visibility = View.VISIBLE
        }

        random = Random()
        initTensorFlowAndLoadModel()
    }

    private fun recognize(tBitmap: Bitmap) {
        aviLoaderHolder.visibility = View.GONE
        tvLoadingText.visibility = View.GONE

        var results = ArrayList<IClassifier.Recognition>()

        if(btnType > 0){
            bitmap = Bitmap.createScaledBitmap(tBitmap, TF_OD_API_INPUT_OBJ_SIZE,
                TF_OD_API_INPUT_OBJ_SIZE, false)
            results = objectClassifier.recognizeImage(bitmap)
        }
        else if(btnType < 0){
            bitmap = Bitmap.createScaledBitmap(tBitmap, TF_OD_API_INPUT_CAR_SIZE,
                TF_OD_API_INPUT_CAR_SIZE, false)
            results = flowerClassifier.recognizeImage(bitmap)
        }

        val canvas = Canvas(bitmap)
        val boxPaint = Paint()
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 3.0f

        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 15.0f

        for (result in results) {
            boxPaint.color = Color.argb(255, random.nextInt(256), random.nextInt(
                256), random.nextInt(256))
            canvas.drawRoundRect(result.location, 5.0f, 5.0f, boxPaint)

            canvas.drawText(String.format("%s %.2f", result.title, (100 * result.confidence)),
                result.location.left + 8, result.location.top + 15, textPaint)
        }

        // bitmap = Bitmap.createScaledBitmap(bitmap, 300, 350, false)
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

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
        private const val TF_OD_API_INPUT_OBJ_SIZE = 300
        private const val TF_OD_API_INPUT_CAR_SIZE = 224
    }

    private fun pickImageFromGalley() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    // Pick Image Functions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGalley()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageViewTmp.setImageURI(data?.data)
            // recognize(imageViewTmp.drawable)
        }
    }

    // tensor
    override fun onDestroy() {
        super.onDestroy()
        executor.execute { objectClassifier.close() }
        // executor.execute { carClassifier.close() }
        executor.execute { flowerClassifier.close() }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                objectClassifier = ObjectClassifier.create(assets)
                // carClassifier = CarClassifier.create(assets)
                flowerClassifier = FlowerClassifier.create(assets)

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

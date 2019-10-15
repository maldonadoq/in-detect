package maldonado.indetect

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var btnDetectOk: Button

    private lateinit var btnToggleCamera: Button
    private lateinit var btnUploadPhoto:  Button
    private lateinit var cameraView: CameraView
    private lateinit var imageViewTmp: ImageView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvLoadingText: TextView
    private lateinit var tvTextResults: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    private lateinit var dictionaryList: HashMap<String, String>
    private lateinit var random: Random
    private var btnType = 1
    private var stringType = "Object Identification Engine Processing ..."

    // model
    private lateinit var objectClassifier: ObjectClassifier
    private lateinit var carClassifier: CarClassifier
    private lateinit var flowerClassifier: FlowerClassifier

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        cameraView = findViewById(R.id.cameraView)
        imageViewTmp = ImageView(this)
        btnToggleCamera = findViewById(R.id.btnToggleCamera)
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto)

        btnDetectOk = findViewById(R.id.btnDetectOk)

        resultDialog = Dialog(this)
        val customProgressView = LayoutInflater.from(this).inflate(R.layout.activity_result,
            null)

        resultDialog.setCancelable(false)
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        resultDialog.setContentView(customProgressView)

        ivImageResult = customProgressView.findViewById(R.id.iViewResult)
        tvLoadingText = customProgressView.findViewById(R.id.tvLoadingRecognition)
        tvTextResults = customProgressView.findViewById(R.id.tvResult)
        tvTextResults.movementMethod = ScrollingMovementMethod()

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

        btnDetectOk.setOnClickListener {
            tvLoadingText.text = stringType

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
        dictionaryList = loadDictionary(assets, "dictionary.txt")
        initTensorFlowAndLoadModel()
    }

    private fun recognize(bitmap: Bitmap) {
        aviLoaderHolder.visibility = View.GONE
        tvLoadingText.visibility = View.GONE

        when(btnType){
            1 -> {
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

                val objects = uniqueList(results)
                tvTextResults.text = ""

                for (obj in objects) {
                    tvTextResults.append(obj + ": " + dictionaryList[obj] + "\n")
                }

            }
            2 -> {
                val results = carClassifier.recognizeImage(bitmap)
                tvTextResults.text = results.toString()
            }
            3 -> {
                val results = flowerClassifier.recognizeImage(bitmap)
                tvTextResults.text = results.toString()
            }
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

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
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

            btnType = 1
            val bt = (imageViewTmp.drawable as BitmapDrawable).bitmap
            recognize(Bitmap.createScaledBitmap(bt, (bt.width*0.5).toInt(), (bt.height*0.5).toInt(),
                false))

            resultDialog.show()
        }
    }

    // tensor
    override fun onDestroy() {
        super.onDestroy()
        executor.execute { objectClassifier.close() }
        executor.execute { carClassifier.close() }
        executor.execute { flowerClassifier.close() }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                objectClassifier = ObjectClassifier.create(assets)
                carClassifier = CarClassifier.create(assets)
                flowerClassifier = FlowerClassifier.create(assets)

                makeButtonVisible()
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }

    private fun makeButtonVisible() {
        runOnUiThread { btnDetectOk.visibility = View.VISIBLE }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_object -> {
                btnType = 1
                stringType = "Object Identification Engine Processing ..."
                btnDetectOk.text = "Detect Object"
                return true
            }
            R.id.m_car -> {
                btnType = 2
                stringType = "Car Classifier Engine Processing ..."
                btnDetectOk.text = "Classify Car"
                return true
            }
            R.id.m_flower -> {
                btnType = 3
                stringType = "Flower Classifier Engine Processing ..."
                btnDetectOk.text = "Classify Flower"
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

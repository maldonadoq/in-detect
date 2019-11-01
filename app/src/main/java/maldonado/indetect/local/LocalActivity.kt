package maldonado.indetect.local

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.wonderkiln.camerakit.*
import maldonado.indetect.R
import maldonado.indetect.local.model.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class LocalActivity : AppCompatActivity() {

    private lateinit var btnDetectOk: Button

    private lateinit var cameraView: CameraView
    private lateinit var imageViewTmp: ImageView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvTextResults: TextView
    private lateinit var tvLoadingText: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    private lateinit var dictionaryList: HashMap<String, String>
    private lateinit var random: Random
    private var btnType = 1

    // model
    private lateinit var objectClassifier: ObjectClassifier
    private lateinit var carClassifier: CarClassifier

    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local)

        cameraView = findViewById(R.id.local_CameraView)
        imageViewTmp = ImageView(this)
        btnDetectOk = findViewById(R.id.local_BtnDetectOk)

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
                Log.i("Local", "init")
                recognize(
                    Bitmap.createScaledBitmap(bitmap, (bitmap.width*0.5).toInt(),
                    (bitmap.height*0.5).toInt(), false))
                Log.i("Local", "end")
            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) { }
        })

        btnDetectOk.setOnClickListener {
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
                tvTextResults.text = recognitionToString(results)
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
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageViewTmp.setImageURI(data?.data)

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
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                objectClassifier = ObjectClassifier.create(assets)
                carClassifier = CarClassifier.create(assets)

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
        menuInflater.inflate(R.menu.menu_local, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_object -> {
                btnType = 1
                btnDetectOk.text = "Detect Object"
                return true
            }
            R.id.m_car -> {
                btnType = 2
                btnDetectOk.text = "Classify Car"
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
            R.id.m_upload -> {
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
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

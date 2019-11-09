package maldonado.indetect.fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wonderkiln.camerakit.*
import maldonado.indetect.R
import maldonado.indetect.models.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class LocalFragment : Fragment() {

    private lateinit var btnDetectOk: FloatingActionButton

    private lateinit var cameraView: CameraView
    private lateinit var imageViewTmp: ImageView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvTextResults: TextView
    private lateinit var tvLoadingText: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    private lateinit var dictionaryList: HashMap<String, String>
    private lateinit var random: Random

    private lateinit var root: View
    private val executor = Executors.newSingleThreadExecutor()

    // model
    private lateinit var objectClassifier: ObjectClassifier
    private lateinit var carClassifier: CarClassifier
    private var btnType = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_local, container, false)

        cameraView = root.findViewById(R.id.local_CameraView)
        imageViewTmp = ImageView(root.context)
        btnDetectOk = root.findViewById(R.id.local_BtnDetectOk)

        resultDialog = Dialog(root.context)
        val dialogView = LayoutInflater.from(root.context).inflate(R.layout.dialog_result, null)

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
                recognize(
                    Bitmap.createScaledBitmap(bitmap, (bitmap.width*0.5).toInt(),
                        (bitmap.height*0.5).toInt(), false))
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
        dictionaryList = loadDictionary(root.context.assets, "dictionary.txt")
        initTensorFlowAndLoadModel()
        return root
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                objectClassifier = ObjectClassifier.create(root.context.assets)
                carClassifier = CarClassifier.create(root.context.assets)
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
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
        super.onPause()
        cameraView.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        /*executor.execute{ objectClassifier.close() }
        executor.execute{ carClassifier.close() }*/
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_local, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_object -> {
                btnType = 1
                return true
            }
            R.id.m_car -> {
                btnType = 2
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
            R.id.m_real -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
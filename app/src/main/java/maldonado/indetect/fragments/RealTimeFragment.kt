package maldonado.indetect.fragments

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Frame
import maldonado.indetect.R
import maldonado.indetect.models.ObjectClassifier
import maldonado.indetect.real.Bounds
import maldonado.indetect.real.Overlay
import java.util.concurrent.Executors

class RealTimeFragment : Fragment() {

    private lateinit var cameraView: CameraView
    private lateinit var overlay: Overlay
    private lateinit var detector: Detector
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_real_time, container, false)

        cameraView = root.findViewById(R.id.real_CameraView)
        overlay = root.findViewById(R.id.overlay)
        detector = Detector(overlay)

        setupCamera()

        return root
    }

    private fun setupCamera(){
        cameraView.addFrameProcessor{
            detector.process(it)
        }
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
        detector.onDestroy()
    }

    // Detector
    inner class Detector(private val overlay: Overlay) {

        private lateinit var objClassifier: ObjectClassifier
        private val executor = Executors.newSingleThreadExecutor()

        init {
            initTensorFlowAndLoadModel()
        }

        fun process(frame: Frame) {
            detectObj(frame)
        }

        private fun detectObj(frame: Frame) {
            frame.data?.let {

                //val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                //val results = objClassifier.recognizeImage(bitmap)

                val tmp = ArrayList<Bounds>()
                tmp.add(Bounds(0, Rect(200,200,500,500)))
                overlay.updateBounds(tmp)
            }
        }

        private fun initTensorFlowAndLoadModel() {
            executor.execute {
                try {
                    objClassifier = ObjectClassifier.create(root.context.assets)
                } catch (e: Exception) {
                    throw RuntimeException("Error initializing TensorFlow!", e)
                }
            }
        }

        fun onDestroy() {
            executor.execute{ objClassifier.close() }
        }
    }
}
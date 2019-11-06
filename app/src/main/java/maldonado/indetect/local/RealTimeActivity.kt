package maldonado.indetect.local

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.otaliastudios.cameraview.CameraView
import maldonado.indetect.R
import maldonado.indetect.local.real.Overlay
import maldonado.indetect.local.real.Detector

class RealTimeActivity : AppCompatActivity() {

    private lateinit var cameraView: CameraView
    private lateinit var overlay: Overlay
    private lateinit var detector: Detector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_real_time)

        cameraView = findViewById(R.id.cameraView)
        overlay = findViewById(R.id.overlay)
        detector = Detector(overlay)

        setupCamera()
    }

    private fun setupCamera() {
        cameraView.addFrameProcessor {
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
        cameraView.destroy()
    }
}

package maldonado.indetect.local.real

import android.graphics.Rect
import com.otaliastudios.cameraview.Frame

class Detector(private val overlay: Overlay) {
    fun process(frame: Frame) {
        detectObj(frame)
    }

    private fun detectObj(frame: Frame) {
        frame.data?.let {
            val tmp = ArrayList<Bounds>()
            tmp.add(Bounds(0, Rect(0,200,600,400)))
            overlay.updateBounds(tmp)
        }
    }
}
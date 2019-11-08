package maldonado.indetect.fragments.model

import android.graphics.Bitmap
import android.graphics.RectF

interface IClassifier {
    data class Recognition(
        var id: String = "", // A unique identifier for what has been recognized. Specific to the class, not the instance of the object.
        var title: String = "", // Display name for the recognition.
        var confidence: Float = 0F, // A sortable score for how good the recognition is relative to others. Higher should be better.
        var location: RectF
    )  {
        override fun toString(): String {
            return String.format("Title = %s (%.2f)", title, confidence)
        }
    }

    fun recognizeImage(tBitmap: Bitmap): List<Recognition>

    fun close()
}
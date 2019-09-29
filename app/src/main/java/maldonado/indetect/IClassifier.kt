package maldonado.indetect

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
            //"%.4f".format(num)
            //return "Title = %.s (%.4f)".format(title, confidence)
            return  "Title = $title ($confidence)"
        }
    }

    fun recognizeImage(bitmap: Bitmap): List<Recognition>

    fun close()
}
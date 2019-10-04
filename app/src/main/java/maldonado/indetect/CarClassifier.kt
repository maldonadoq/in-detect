package maldonado.indetect

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.collections.List
import android.util.Log


@Suppress("DEPRECATION")
class CarClassifier(
    var interpreter: Interpreter? = null,
    var inputSize: Int = 0,
    var labelList: List<String> = emptyList()
) : IClassifier {

    companion object {
        private const val BATCH_SIZE = 1
        private const val PIXEL_SIZE = 3

        // TensorFlow
        // Configuration values for the prepackaged SSD model.
        private const val TF_OD_API_INPUT_SIZE = 224        // Main
        private const val TF_OD_API_MODEL_FILE = "car_graph.tflite"
        private const val TF_OD_API_LABELS_FILE = "car_labels.txt"

        @Throws(IOException::class)
        fun create(assetManager: AssetManager): CarClassifier {

            val classifier = CarClassifier()
            classifier.interpreter = Interpreter(classifier.loadModelFile(assetManager))
            classifier.labelList = classifier.loadLabelList(assetManager)
            classifier.inputSize = TF_OD_API_INPUT_SIZE

            return classifier
        }
    }

    @SuppressLint("UseSparseArrays")
    override fun recognizeImage(bitmap: Bitmap): ArrayList<IClassifier.Recognition> {
        val byteBufferInput = convertBitmapToByteBuffer(bitmap)

        // copy data into TensorFlow
        val byteBufferOutput = ByteArray(196)

        Log.i("Car", "Init Run Ok")

        interpreter!!.run(byteBufferInput, byteBufferOutput)

        Log.i("Car", "Run Ok")

        // Show the best detections.
        // after scaling them back to the input size.
        val recognitions = ArrayList<IClassifier.Recognition>()

        return recognitions
    }

    override fun close() {
        interpreter!!.close()
        interpreter = null
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(TF_OD_API_MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    @Throws(IOException::class)
    private fun loadLabelList(assetManager: AssetManager): List<String> {
        val labelList = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(assetManager.open(TF_OD_API_LABELS_FILE)))
        while (true) {
            val line = reader.readLine() ?: break
            labelList.add(line)
        }
        reader.close()
        return labelList
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val `val` = intValues[pixel++]
                byteBuffer.put((`val` shr 16 and 0xFF).toByte())
                byteBuffer.put((`val` shr 8 and 0xFF).toByte())
                byteBuffer.put((`val` and 0xFF).toByte())

                /*byteBuffer.put((((`val` shr 16 and 0xFF) - 128) / 126.0f).toByte())
                byteBuffer.put((((`val` shr 8 and 0xFF) - 128) / 126.0f).toByte())
                byteBuffer.put((((`val` and 0xFF) - 128) / 126.0f).toByte())*/
            }
        }
        return byteBuffer
    }
}
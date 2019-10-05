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
import java.util.HashMap
import android.graphics.RectF

@Suppress("DEPRECATION")
class ObjectClassifier(
    var interpreter: Interpreter? = null,
    var inputSize: Int = 0,
    var labelList: List<String> = emptyList()
) : IClassifier {

    companion object {
        private const val BATCH_SIZE = 1
        private const val PIXEL_SIZE = 3

        // TensorFlow
        // Configuration values for the prepackaged SSD model.
        private const val TF_OD_API_INPUT_SIZE = 300        // Main
        private const val TF_OD_API_MODEL_FILE = "object_graph.lite"
        private const val TF_OD_API_LABELS_FILE = "object_labels.txt"
        private const val NUM_DETECTIONS = 10

        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f

        @Throws(IOException::class)
        fun create(assetManager: AssetManager): ObjectClassifier {

            val classifier = ObjectClassifier()
            classifier.interpreter = Interpreter(classifier.loadModelFile(assetManager))
            classifier.labelList = classifier.loadLabelList(assetManager)
            classifier.inputSize = TF_OD_API_INPUT_SIZE

            return classifier
        }
    }

    @SuppressLint("UseSparseArrays")
    override fun recognizeImage(bitmap: Bitmap): ArrayList<IClassifier.Recognition> {
        val byteBuffer = convertBitmapToByteBuffer(bitmap)

        // copy data into TensorFlow
        val outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        val outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        val numDetections = FloatArray(1)

        val inputArray = arrayOf<Any>(byteBuffer)

        val outputMap = HashMap<Int, Any>()
        outputMap[0] = outputLocations
        outputMap[1] = outputClasses
        outputMap[2] = outputScores
        outputMap[3] = numDetections

        interpreter!!.runForMultipleInputsOutputs(inputArray, outputMap)

        // Show the best detections.
        // after scaling them back to the input size.
        val recognitions = ArrayList<IClassifier.Recognition>()

        for (i in 0 until NUM_DETECTIONS) {
            val detection = RectF(
                outputLocations[0][i][1] * inputSize,
                outputLocations[0][i][0] * inputSize,
                outputLocations[0][i][3] * inputSize,
                outputLocations[0][i][2] * inputSize
            )
            // SSD Mobilenet V1 Model assumes class 0 is background class
            // in label file and class labels start from 1 to number_of_classes+1,
            // while outputClasses correspond to class index from 0 to number_of_classes
            val score = outputScores[0][i]
            if (score >= MINIMUM_CONFIDENCE_TF_OD_API) {
                recognitions.add(
                    IClassifier.Recognition(
                        "" + i,
                        labelList[outputClasses[0][i].toInt() + 1],
                        score,
                        detection
                    )
                )
            }
        }

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
            }
        }
        return byteBuffer
    }
}
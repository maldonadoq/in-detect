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
import android.bluetooth.BluetoothClass.Device
import android.app.Activity
import android.util.Log
import java.nio.file.Files.size




@Suppress("DEPRECATION")
class FlowerClassifier(
    var interpreter: Interpreter? = null,
    var inputSize: Int = 0,
    var labelSize: Int = 0,
    var labelList: List<String> = emptyList()
) : IClassifier {

    companion object {
        private const val BATCH_SIZE = 1
        private const val PIXEL_SIZE = 3
        private const val BYTES_CHANNEL = 4

        // TensorFlow
        // Configuration values for the prepackaged SSD model.
        private const val TF_OD_API_INPUT_SIZE = 224        // Main
        private const val TF_OD_API_MODEL_FILE = "flower_graph.lite"
        private const val TF_OD_API_LABELS_FILE = "flower_labels.txt"
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.5f
        private const val IMAGE_MEAN = 127.5f
        private const val IMAGE_STD = 127.5f

        @Throws(IOException::class)
        fun create(assetManager: AssetManager): FlowerClassifier {

            val classifier = FlowerClassifier()
            classifier.interpreter = Interpreter(classifier.loadModelFile(assetManager))
            classifier.labelList = classifier.loadLabelList(assetManager)
            classifier.labelSize = classifier.labelList.size
            classifier.inputSize = TF_OD_API_INPUT_SIZE

            return classifier
        }
    }

    @SuppressLint("UseSparseArrays")
    override fun recognizeImage(bitmap: Bitmap): ArrayList<IClassifier.Recognition> {

        Log.i("Flower", labelList.size.toString())

        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val labelProbArray = Array(1) { FloatArray(labelSize) }

        var st = "Init Label Prob: "
        for(i in 0 until (labelProbArray.size)){
            st += " " + labelProbArray[i]
        }

        Log.i("Flower", st)

        Log.i("Flower", "Init Run")
        interpreter!!.run(byteBuffer, labelProbArray)
        Log.i("Flower", "Run Ok")

        st = "End Label Prob: "
        for(i in 0 until (labelProbArray.size)){
            st += " " + labelProbArray[i]
        }
        Log.i("Flower", st)

        val recognitions = ArrayList<IClassifier.Recognition>()


        for (i in 0 until labelSize) {
            val score = labelProbArray[0][i]
            if (score >= MINIMUM_CONFIDENCE_TF_OD_API) {
                recognitions.add(
                    IClassifier.Recognition(
                        "" + i,
                        if(labelList.size > i ) labelList[i]  else "unknown",
                        score,
                        RectF(0.0f, 0.0f, 10.0f, 10.0f)
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
        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize
                * PIXEL_SIZE * BYTES_CHANNEL)

        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val `val` = intValues[i * inputSize + j]
                byteBuffer.putFloat(((`val` shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((`val` shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((`val` and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        return byteBuffer
    }
}
package maldonado.indetect

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
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
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import android.R.attr.bitmap
import android.graphics.RectF
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.os.Trace
import android.util.Log
import java.nio.file.Files.size


@Suppress("DEPRECATION")
class CarClassifier(
    var inputSize: Int = 0,
    var labelList: List<String> = emptyList()
) : IClassifier {

    private lateinit var inputName: String
    private lateinit var outputName: String
    private var imageMean = 0
    private var imageStd = 0.0f

    private lateinit var intValues: IntArray
    private lateinit var floatValues: FloatArray
    private lateinit var outputs: FloatArray
    private lateinit var outputNames: Array<String>
    private var logStats = false
    private lateinit var inferenceInterface: TensorFlowInferenceInterface

    companion object {
        // TensorFlow
        // Configuration values for the prepackaged SSD model.
        private const val TF_OD_API_INPUT_SIZE = 224        // Main
        private const val TF_OD_API_INPUT_MEAN = 128        //
        private const val TF_OD_API_INPUT_STD = 128.0f        //
        private const val TF_OD_API_MODEL_FILE = "car_graph.pb"
        private const val TF_OD_API_LABELS_FILE = "car_labels.txt"

        private const val INPUT_NAME = "input"
        private const val OUTPUT_NAME = "final_result"

        @Throws(IOException::class)
        fun create(assetManager: AssetManager): CarClassifier {

            val classifier = CarClassifier()

            classifier.inputName = INPUT_NAME
            classifier.outputName = OUTPUT_NAME

            classifier.labelList = classifier.loadLabelList(assetManager)
            classifier.inferenceInterface = TensorFlowInferenceInterface(assetManager, TF_OD_API_MODEL_FILE)

            val opt = classifier.inferenceInterface.graphOperation(OUTPUT_NAME)
            val nc = opt.output<Int>(0).shape().size(1)

            classifier.inputSize = TF_OD_API_INPUT_SIZE
            classifier.imageMean = TF_OD_API_INPUT_MEAN
            classifier.imageStd = TF_OD_API_INPUT_STD

            // Pre-allocate buffers.
            classifier.outputNames = arrayOf(OUTPUT_NAME)
            classifier.intValues = IntArray(TF_OD_API_INPUT_SIZE * TF_OD_API_INPUT_SIZE)
            classifier.floatValues = FloatArray(TF_OD_API_INPUT_SIZE * TF_OD_API_INPUT_SIZE * 3)
            classifier.outputs = FloatArray(nc.toInt())

            Log.i("car", "Create OK")
            return classifier
        }
    }

    @SuppressLint("UseSparseArrays")
    override fun recognizeImage(bitmap: Bitmap): ArrayList<IClassifier.Recognition> {

        Log.i("car", "Init Recognize")

        Log.i("car", "Init bitmap")
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        Log.i("car", "init ByteBuffer")
        for (i in 0 until intValues.size) {
            val `val` = intValues[i]
            floatValues[i * 3 + 0] = ((`val` shr 16 and 0xFF) - TF_OD_API_INPUT_MEAN) / TF_OD_API_INPUT_STD
            floatValues[i * 3 + 1] = ((`val` shr 8 and 0xFF) - TF_OD_API_INPUT_MEAN) / TF_OD_API_INPUT_STD
            floatValues[i * 3 + 2] = ((`val` and 0xFF) - TF_OD_API_INPUT_MEAN) / TF_OD_API_INPUT_STD
        }

        Log.i("car", floatValues[0].toString())

        // Copy the input data into TensorFlow.
        inferenceInterface.feed(INPUT_NAME, floatValues, (1 * inputSize * inputSize *3).toLong())
        Log.i("car", "Feed Ok")

        inferenceInterface.run(outputNames, true)
        Log.i("car", "Run Ok")

        inferenceInterface.fetch(OUTPUT_NAME, outputs)
        Log.i("car", "Fetch Ok")

        val recognitions = ArrayList<IClassifier.Recognition>()

        Log.i("car", "${outputs.size}")
        for(i in 0 until outputs.size){
            val score = outputs[i]
            //if(score > THRESHOLD){
                recognitions.add(
                    IClassifier.Recognition(
                        "" + i,
                        if (labelList.size > i) labelList[i] else "unknown",
                        score,
                        RectF(0.0f, 0.0f, 10.0f, 10.0f)
                    )
                )
            //}
        }

        return recognitions
    }

    override fun close() {
        inferenceInterface.close()
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
}
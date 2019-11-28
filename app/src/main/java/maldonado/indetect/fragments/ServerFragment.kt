@file:Suppress("DEPRECATION")

package maldonado.indetect.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.*
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import maldonado.indetect.R
import maldonado.indetect.models.IClassifier
import maldonado.indetect.models.uniqueList
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class ServerFragment : Fragment() {
    private lateinit var progressDialog: ProgressDialog

    private lateinit var btnDetectOk: FloatingActionButton
    private lateinit var btnUpload: Button
    private lateinit var cameraViewS: CameraView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvTextResults: TextView
    private lateinit var tvLoadingText: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    private lateinit var storage: StorageReference
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var imgBitmap: Bitmap
    private lateinit var resBitmap: Bitmap
    private lateinit var results: ArrayList<IClassifier.Recognition>
    private lateinit var random: Random
    private var sessionID: String = ""
    private lateinit var singletonNetwork: SingletonNetwork

    private lateinit var root: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        storage = FirebaseStorage.getInstance().reference.child("Uploads")
        db = FirebaseDatabase.getInstance().reference.child("Uploads")
        random = Random()
        results = ArrayList()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_server, container, false)

        singletonNetwork = SingletonNetwork.getInstance(root.context)

        cameraViewS = root.findViewById(R.id.server_CameraView)
        btnDetectOk = root.findViewById(R.id.server_BtnDetectOk)

        resultDialog = Dialog(root.context)
        val dialogView = LayoutInflater.from(root.context).inflate(R.layout.dialog_result, null)

        resultDialog.setCancelable(false)
        resultDialog.setContentView(dialogView)

        ivImageResult = dialogView.findViewById(R.id.result_IViewResult)
        tvTextResults = dialogView.findViewById(R.id.result_TvResult)
        btnUpload = dialogView.findViewById(R.id.result_BtnUpload)
        tvLoadingText = dialogView.findViewById(R.id.result_TvLoadingRecognition)
        aviLoaderHolder = dialogView.findViewById<View>(R.id.result_AviLoaderHolderView)
        tvTextResults.movementMethod = ScrollingMovementMethod()

        cameraViewS.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(it: ByteArray?) {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it!!.size)
                recognize(Bitmap.createScaledBitmap(bitmap, (bitmap.width*0.5).toInt(),
                    (bitmap.height*0.5).toInt(), false))
            }

            override fun onVideoTaken(it: File?) {   }
        })

        btnDetectOk.setOnClickListener {
            //tvLoadingText.visibility = View.VISIBLE
            //aviLoaderHolder.visibility = View.VISIBLE

            cameraViewS.capturePicture()
            resultDialog.show()
            tvTextResults.visibility = View.GONE
            ivImageResult.visibility = View.GONE
            btnUpload.visibility = View.GONE
        }

        btnUpload.setOnClickListener{
            uploadImage()
        }

        resultDialog.setOnDismissListener {
            tvLoadingText.visibility = View.VISIBLE
            aviLoaderHolder.visibility = View.VISIBLE
        }

        progressDialog = ProgressDialog(root.context)

        return root
    }

    private fun drawResults(name: String, conf: Float){
        aviLoaderHolder.visibility = View.GONE
        tvLoadingText.visibility = View.GONE

        tvTextResults.text = String.format("%s %.2f", name, 100*conf)
        ivImageResult.setImageBitmap(resBitmap)
        tvTextResults.visibility = View.VISIBLE
        ivImageResult.visibility = View.VISIBLE
        btnUpload.visibility = View.VISIBLE
        resultDialog.setCancelable(true)
    }

    private fun recognize(bitmap: Bitmap) {
        imgBitmap = Bitmap.createScaledBitmap(bitmap, 290, 400, false)
        resBitmap = bitmap

        results.clear()

        sendRequest()
    }

    @SuppressLint("SetTextI18n")
    private fun sendRequest(){
        // To base64
        //val url = "http://192.168.42.39:8080/api/v1.0/process"
        val url = "http://192.168.196.213:8080/api/v1.0/process"
        val byteArrayOutputStream = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream .toByteArray()
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(root.context)

        // Json Object
        val obj = JSONObject()
        obj.put("profile", auth.currentUser?.email.toString())
        obj.put("type", "png")
        obj.put("image", encoded)

        // Json Request
        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, url, obj,
            Response.Listener {
                Log.i("Json", it.toString())
                //tmp = JSONObject(it.get(key).toString())

                val className = it.get("Class").toString()
                val confidence = it.get("Confidence").toString().toFloat()
                sessionID = it.get("Sessionid").toString()

                drawResults(className, confidence)

            },
            Response.ErrorListener {
                Toast.makeText(root.context, it.message, Toast.LENGTH_SHORT).show()
                aviLoaderHolder.visibility = View.GONE
                tvLoadingText.visibility = View.GONE
                //tvLoadingText.text = "Server isn't Working"
                resultDialog.setCancelable(true)
            })

        // Add Json Object Request to Queue
        jsonRequest.retryPolicy = DefaultRetryPolicy(15000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        queue.add(jsonRequest)
    }

    private fun uploadImage(){
        val fileReference = storage.child(System.currentTimeMillis().toString() + ".jpg")

        val bao = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao)
        val data = bao.toByteArray()

        progressDialog.setMessage("Uploading Picture!!")
        progressDialog.show()

        val idUser = auth.currentUser?.uid.toString()

        val obj = JSONObject()
        obj.put("session", sessionID)
        singletonNetwork.sendPostRequest(obj, "share")

        fileReference.putBytes(data)
            .addOnSuccessListener {
                Toast.makeText(resultDialog.context, "Uploaded!", Toast.LENGTH_SHORT).show()

                val imgDB = db.child(db.push().key.toString())
                imgDB.child("name").setValue("No Name")
                imgDB.child("id").setValue(idUser)

                it.storage.downloadUrl
                    .addOnSuccessListener {t ->
                        imgDB.child("url").setValue(t.toString())
                    }
                    .addOnFailureListener{t ->
                        imgDB.child("url").setValue(t.message.toString())
                    }

                btnUpload.visibility = View.GONE
                progressDialog.dismiss()
            }
            .addOnFailureListener{
                Toast.makeText(resultDialog.context, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        cameraViewS.start()
    }

    override fun onPause() {
        super.onPause()
        cameraViewS.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraViewS.destroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_server, menu)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_front -> {
                cameraViewS.toggleFacing()
                return true
            }
            R.id.m_flash -> {
                cameraViewS.toggleFlash()
                return true
            }
            R.id.m_real -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
@file:Suppress("DEPRECATION")

package maldonado.indetect.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import maldonado.indetect.R
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

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

    private lateinit var imgBitmap: Bitmap

    private lateinit var root: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        storage = FirebaseStorage.getInstance().reference.child("Uploads")
        db = FirebaseDatabase.getInstance().reference.child("Uploads")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_server, container, false)

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

    private fun recognize(bitmap: Bitmap) {
        aviLoaderHolder.visibility = View.GONE
        tvLoadingText.visibility = View.GONE

        imgBitmap = Bitmap.createScaledBitmap(bitmap, 290, 400, false)
        sendRequest()

        ivImageResult.setImageBitmap(bitmap)
        tvTextResults.visibility = View.VISIBLE
        ivImageResult.visibility = View.VISIBLE
        btnUpload.visibility = View.VISIBLE
        resultDialog.setCancelable(true)

    }

    private fun sendRequest(){
        // To base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream .toByteArray()
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(root.context)
        val url = "http://192.168.196.105:8000/api/v1.0/process"

        // Json Object
        val obj = JSONObject()
        obj.put("type", "png")
        obj.put("image", encoded)

        // Json Request
        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, url, obj,
            Response.Listener {
                Toast.makeText(root.context, "Response is: $it", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener {
                Toast.makeText(root.context, it.message, Toast.LENGTH_SHORT).show()
            })

        // Add Json Object Request to Queue
        queue.add(jsonRequest)
    }

    private fun uploadImage(){
        val fileReference = storage.child(System.currentTimeMillis().toString() + ".jpg")

        val bao = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao)
        val data = bao.toByteArray()

        progressDialog.setMessage("Uploading Picture!!")
        progressDialog.show()

        fileReference.putBytes(data)
            .addOnSuccessListener {
                Toast.makeText(resultDialog.context, "Uploaded!", Toast.LENGTH_SHORT).show()

                val imgDB = db.child(db.push().key.toString())
                imgDB.child("name").setValue("No Name")

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
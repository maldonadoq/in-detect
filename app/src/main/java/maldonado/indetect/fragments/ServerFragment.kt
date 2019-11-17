package maldonado.indetect.fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import maldonado.indetect.R
import java.io.ByteArrayOutputStream
import java.io.File

class ServerFragment : Fragment() {

    private lateinit var btnDetectOk: FloatingActionButton
    private lateinit var cameraView: CameraView

    private lateinit var ivImageResult: ImageView
    private lateinit var tvTextResults: TextView
    private lateinit var tvLoadingText: TextView
    private lateinit var aviLoaderHolder: View
    private lateinit var resultDialog: Dialog

    private lateinit var storage: StorageReference
    private lateinit var db: DatabaseReference

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

        cameraView = root.findViewById(R.id.server_CameraView)
        btnDetectOk = root.findViewById(R.id.server_BtnDetectOk)

        resultDialog = Dialog(root.context)
        val dialogView = LayoutInflater.from(root.context).inflate(R.layout.dialog_result, null)

        resultDialog.setCancelable(false)
        resultDialog.setContentView(dialogView)

        ivImageResult = dialogView.findViewById(R.id.result_IViewResult)
        tvTextResults = dialogView.findViewById(R.id.result_TvResult)
        tvLoadingText = dialogView.findViewById(R.id.result_TvLoadingRecognition)
        aviLoaderHolder = dialogView.findViewById<View>(R.id.result_AviLoaderHolderView)
        tvTextResults.movementMethod = ScrollingMovementMethod()

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(it: ByteArray?) {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it!!.size)
                recognize(Bitmap.createScaledBitmap(bitmap, (bitmap.width*0.5).toInt(),
                    (bitmap.height*0.5).toInt(), false))
            }

            override fun onVideoTaken(it: File?) {   }
        })

        btnDetectOk.setOnClickListener {

            cameraView.capturePicture()
            resultDialog.show()
            tvTextResults.visibility = View.GONE
            ivImageResult.visibility = View.GONE

            //sendRequest()
        }

        resultDialog.setOnDismissListener {
            tvLoadingText.visibility = View.VISIBLE
            aviLoaderHolder.visibility = View.VISIBLE
        }

        return root
    }

    private fun recognize(bitmap: Bitmap) {
        aviLoaderHolder.visibility = View.GONE
        tvLoadingText.visibility = View.GONE

        ivImageResult.setImageBitmap(bitmap)
        tvTextResults.visibility = View.VISIBLE
        ivImageResult.visibility = View.VISIBLE
        resultDialog.setCancelable(true)

        val tmp = Bitmap.createScaledBitmap(bitmap, 290, 400, false)
        uploadImage(tmp)

    }

    private fun sendRequest(){
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(root.context)
        val url = "http://192.168.43.128:8000"
        //val url = "http://www.google.com/"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> {
                response ->

                Log.i("Request", "Response is: $response")
                Toast.makeText(root.context, "Response is: $response", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener {
                Log.i("Request", "Fail")
                Toast.makeText(root.context, "Fail", Toast.LENGTH_SHORT).show()
            })

        queue.add(stringRequest)
    }

    private fun uploadImage(bitmap: Bitmap){
        val fileReference = storage.child(System.currentTimeMillis().toString() + ".jpg")

        val bao = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao)
        val data = bao.toByteArray()

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
            }
            .addOnFailureListener{
                Toast.makeText(resultDialog.context, it.message, Toast.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_server, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.m_front -> {
                cameraView.toggleFacing()
                return true
            }
            R.id.m_flash -> {
                cameraView.toggleFlash()
                return true
            }
            R.id.m_real -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
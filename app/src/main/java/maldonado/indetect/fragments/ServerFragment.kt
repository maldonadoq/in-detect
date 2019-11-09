package maldonado.indetect.fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wonderkiln.camerakit.*
import maldonado.indetect.R
import java.io.ByteArrayOutputStream

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

        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) { }

            override fun onError(cameraKitError: CameraKitError) { }

            override fun onImage(cameraKitImage: CameraKitImage) {
                val bitmap = cameraKitImage.bitmap
                recognize(
                    Bitmap.createScaledBitmap(bitmap, (bitmap.width*0.5).toInt(),
                        (bitmap.height*0.5).toInt(), false))
            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) { }
        })

        btnDetectOk.setOnClickListener {

            cameraView.captureImage()
            resultDialog.show()
            tvTextResults.visibility = View.GONE
            ivImageResult.visibility = View.GONE
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

        val tmp = Bitmap.createScaledBitmap(bitmap, 250, 250, false)
        uploadImage(tmp)
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
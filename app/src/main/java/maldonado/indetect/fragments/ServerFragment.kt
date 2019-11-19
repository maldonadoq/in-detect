@file:Suppress("DEPRECATION")

package maldonado.indetect.fragments

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel
import com.google.firebase.ml.custom.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import maldonado.indetect.R
import java.io.ByteArrayOutputStream
import java.io.File

val pokeArray: Array<String> = arrayOf("Abra", "Aerodactyl", "Alakazam", "Arbok", "Arcanine", "Articuno", "Beedrill", "Bellsprout",
    "Blastoise", "Bulbasaur", "Butterfree", "Caterpie", "Chansey", "Charizard", "Charmander", "Charmeleon", "Clefable", "Clefairy", "Cloyster", "Cubone", "Dewgong",
    "Diglett", "Ditto", "Dodrio", "Doduo", "Dragonair", "Dragonite", "Dratini", "Drowzee", "Dugtrio", "Eevee", "Ekans", "Electabuzz",
    "Electrode", "Exeggcute", "Exeggutor", "Farfetchd", "Fearow", "Flareon", "Gastly", "Gengar", "Geodude", "Gloom",
    "Golbat", "Goldeen", "Golduck", "Golem", "Graveler", "Grimer", "Growlithe", "Gyarados", "Haunter", "Hitmonchan",
    "Hitmonlee", "Horsea", "Hypno", "Ivysaur", "Jigglypuff", "Jolteon", "Jynx", "Kabuto",
    "Kabutops", "Kadabra", "Kakuna", "Kangaskhan", "Kingler", "Koffing", "Krabby", "Lapras", "Lickitung", "Machamp",
    "Machoke", "Machop", "Magikarp", "Magmar", "Magnemite", "Magneton", "Mankey", "Marowak", "Meowth", "Metapod",
    "Mew", "Mewtwo", "Moltres", "Mrmime", "Muk", "Nidoking", "Nidoqueen", "Nidorina", "Nidorino", "Ninetales",
    "Oddish", "Omanyte", "Omastar", "Onix", "Paras", "Parasect", "Persian", "Pidgeot", "Pidgeotto", "Pidgey",
    "Pikachu", "Pinsir", "Poliwag", "Poliwhirl", "Poliwrath", "Ponyta", "Porygon", "Primeape", "Psyduck", "Raichu",
    "Rapidash", "Raticate", "Rattata", "Rhydon", "Rhyhorn", "Sandshrew", "Sandslash", "Scyther", "Seadra",
    "Seaking", "Seel", "Shellder", "Slowbro", "Slowpoke", "Snorlax", "Spearow", "Squirtle", "Starmie", "Staryu",
    "Tangela", "Tauros", "Tentacool", "Tentacruel", "Vaporeon", "Venomoth", "Venonat", "Venusaur", "Victreebel",
    "Vileplume", "Voltorb", "Vulpix", "Wartortle", "Weedle", "Weepinbell", "Weezing", "Wigglytuff", "Zapdos", "Zubat")

class ServerFragment : Fragment() {

    private lateinit var progressDialog: ProgressDialog

    private lateinit var modelInterpreter: FirebaseModelInterpreter
    private lateinit var inputOutputOptions: FirebaseModelInputOutputOptions

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

    private lateinit var imgBitmag: Bitmap

    private lateinit var root: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        storage = FirebaseStorage.getInstance().reference.child("Uploads")
        db = FirebaseDatabase.getInstance().reference.child("Uploads")

        var conditionsBuilder: FirebaseModelDownloadConditions.Builder = FirebaseModelDownloadConditions.Builder().requireWifi()
        conditionsBuilder = conditionsBuilder
            .requireCharging()
            .requireDeviceIdle()

        val conditions = conditionsBuilder.build()
        val cloudSource = FirebaseRemoteModel.Builder("pokedex")
            .enableModelUpdates(true)
            .setInitialDownloadConditions(conditions)
            .setUpdatesDownloadConditions(conditions)
            .build()

        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource)

        val options = FirebaseModelOptions.Builder()
            .setRemoteModelName("pokedex")
            .build()

        modelInterpreter = FirebaseModelInterpreter.getInstance(options)!!
        inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 149))
            .build()
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

            //sendRequest()
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

        // Here Custom Model
        val tmp = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputs = FirebaseModelInputs.Builder()
            .add(convertBitmapToByteBuffer(tmp))
            .build()

        var results = ""
        modelInterpreter.run(inputs, inputOutputOptions)
            .addOnSuccessListener {
                it.getOutput<Array<FloatArray>>(0)[0].forEachIndexed { index, fl ->
                    if (fl > .20){
                        results += "${pokeArray[index]}  $fl \n"
                    }
                }
                tvTextResults.text = results
            }
            .addOnFailureListener {
                Toast.makeText(root.context, it.message, Toast.LENGTH_SHORT).show()
            }

        ivImageResult.setImageBitmap(bitmap)
        tvTextResults.visibility = View.VISIBLE
        ivImageResult.visibility = View.VISIBLE
        btnUpload.visibility = View.VISIBLE
        resultDialog.setCancelable(true)

        imgBitmag = Bitmap.createScaledBitmap(bitmap, 290, 400, false)

        btnUpload.setOnClickListener{
            uploadImage()
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap?): Array<Array<Array<FloatArray> > > {
        val batchNum = 0
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val pixel = bitmap!!.getPixel(i, j)
                input[batchNum][i][j][0] = (pixel.red - 128) / 128.0f
                input[batchNum][i][j][1] = (pixel.green - 128) / 128.0f
                input[batchNum][i][j][2] = (pixel.blue - 128) / 128.0f
            }
        }
        return input
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
                Toast.makeText(root.context, "Response is: $response", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener {
                Toast.makeText(root.context, "Fail", Toast.LENGTH_SHORT).show()
            })

        queue.add(stringRequest)
    }

    private fun uploadImage(){
        val fileReference = storage.child(System.currentTimeMillis().toString() + ".jpg")

        val bao = ByteArrayOutputStream()
        imgBitmag.compress(Bitmap.CompressFormat.JPEG, 100, bao)
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
package maldonado.indetect.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import maldonado.indetect.R
import maldonado.indetect.adapter.ImageAdapter
import maldonado.indetect.adapter.Upload

class GalleryFragment : Fragment(), ImageAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var uploads: ArrayList<Upload>
    private lateinit var dbReference: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var dbListener: ValueEventListener
    private lateinit var root: View
    private lateinit var auth: FirebaseAuth
    private var typeUser: String = ""
    private var idUser: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbReference = FirebaseDatabase.getInstance().reference.child("Uploads")
        storage = FirebaseStorage.getInstance()

        auth = FirebaseAuth.getInstance()
        idUser = auth.currentUser?.uid.toString()

        val tKey = FirebaseDatabase.getInstance().reference.child("User")
            .child(idUser)
        tKey.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p: DataSnapshot) {
                typeUser = p.child("type").value.toString()
            }

            override fun onCancelled(e: DatabaseError) {
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_gallery, container, false)

        recyclerView = root.findViewById(R.id.galleryRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(root.context, 2)

        return root
    }

    override fun onStart() {
        super.onStart()

        uploads = ArrayList()
        imageAdapter = ImageAdapter(root.context, uploads)
        recyclerView.adapter = imageAdapter
        imageAdapter.setOnItemClickListener(this@GalleryFragment)

        dbListener = dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                uploads.clear()

                for(imageSnapshot in dataSnapshot.children) {

                    val id:String = imageSnapshot.child("id").value.toString()
                    val name: String = imageSnapshot.child("name").value.toString()
                    val url: String = imageSnapshot.child("url").value.toString()

                    if(typeUser == "expert"){
                        uploads.add(Upload(name, url, imageSnapshot.key.toString()))
                    }
                    else if ((typeUser == "normal") && (id == idUser)){
                        uploads.add(Upload(name, url, imageSnapshot.key.toString()))
                    }
                }

                imageAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(root.context, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(root.context, "Normal Click at position: $position", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("InflateParams")
    override fun onRenameClick(position: Int) {
        if(typeUser == "expert") {
            val dialogView = LayoutInflater.from(root.context).inflate(R.layout.dialog_rename, null)
            val builder = AlertDialog.Builder(root.context)
                .setView(dialogView)
                .setTitle("Rename")
                .setNegativeButton("Cancel") { _, _ ->

                }
                .setPositiveButton("Save") { _, _ ->
                    val renameTxtName = dialogView.findViewById<EditText>(R.id.rename_TxtName)
                    val name = renameTxtName.text.toString()

                    val selectedUpload = uploads[position]

                    dbReference.child(selectedUpload.getKey()).child("name").setValue(name)
                }

            builder.show()
        }
        else{
            Toast.makeText(root.context, "Only the experts can Rename!!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteClick(position: Int) {
        val selectedUpload = uploads[position]
        val selectedKey = selectedUpload.getKey()

        val imgStorage = storage.getReferenceFromUrl(selectedUpload.getUrl())
        imgStorage.delete()
            .addOnSuccessListener {
                dbReference.child(selectedKey).removeValue()
                Toast.makeText(root.context, "Image Deleted", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbReference.removeEventListener(dbListener)
    }
}
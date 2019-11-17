package maldonado.indetect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import maldonado.indetect.R
import maldonado.indetect.adapter.ImageAdapter
import maldonado.indetect.adapter.Upload

class GalleryFragment : Fragment(), ImageAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var uploads: ArrayList<Upload>
    private lateinit var dbReference: DatabaseReference
    private lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbReference = FirebaseDatabase.getInstance().reference.child("Uploads")
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
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(imageSnapshot in dataSnapshot.children){
                    val name:String = imageSnapshot.child("name").value.toString()
                    val url:String = imageSnapshot.child("url").value.toString()
                    uploads.add(Upload(name, url))
                }

                imageAdapter = ImageAdapter(root.context, uploads)
                imageAdapter.setOnItemClickListener(this@GalleryFragment)
                recyclerView.adapter = imageAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(root.context, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(root.context, "Normal Click at position: $position", Toast.LENGTH_SHORT).show()
    }

    override fun onRenameClick(position: Int) {
        Toast.makeText(root.context, "Rename Click at position: $position", Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteClick(position: Int) {
        Toast.makeText(root.context, "Delete Click at position: $position", Toast.LENGTH_SHORT).show()
    }
}
package maldonado.indetect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import maldonado.indetect.R
import maldonado.indetect.fragments.adapter.ImageAdapter
import maldonado.indetect.fragments.adapter.Upload

class GalleryFragment : Fragment() {

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
        recyclerView.layoutManager = LinearLayoutManager(root.context)

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
                recyclerView.adapter = imageAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(root.context, databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
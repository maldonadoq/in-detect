package maldonado.indetect.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import maldonado.indetect.R

class ProfileFragment : Fragment() {
    private lateinit var btnCar: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val txtEmail = root.findViewById<TextView>(R.id.profile_TxtEmail)
        val txtName = root.findViewById<TextView>(R.id.profile_TxtName)
        val txtPhone = root.findViewById<TextView>(R.id.profile_TxtPhone)
        val txtType = root.findViewById<TextView>(R.id.profile_TxtType)

        btnCar = root.findViewById(R.id.profile_Btn_Car)

        val user = auth.currentUser
        txtEmail.text = user?.email
        txtName.text = user?.displayName


        FirebaseDatabase.getInstance().reference.child("User").child(user?.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(t: DataSnapshot) {
                    txtPhone.text = t.child("Phone").value.toString()
                    txtType.text = t.child("Type").value.toString()
                }

                override fun onCancelled(t: DatabaseError) {
                    Toast.makeText(root.context, t.message, Toast.LENGTH_SHORT).show()
                }
            })


        btnCar.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_nav_profile_to_nav_server)
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                Navigation.findNavController(root).navigate(R.id.action_nav_profile_to_nav_home)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
package maldonado.indetect.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.R

class ProfileFragment : Fragment() {
    private lateinit var btnCar: Button
    private lateinit var txtEmail: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        txtEmail = root.findViewById(R.id.profile_TxtEmail)
        val txtName = root.findViewById<TextView>(R.id.profile_TxtName)
        val txtPhone = root.findViewById<TextView>(R.id.profile_TxtPhone)

        btnCar = root.findViewById(R.id.profile_Btn_Car)

        val user = auth.currentUser

        txtEmail.text = user?.email
        txtName.text = user?.displayName
        txtPhone.text = user?.phoneNumber

        btnCar.setOnClickListener{
            val fm = fragmentManager?.beginTransaction()
            fm?.replace(R.id.nav_host_fragment, LocalFragment())
            fm?.addToBackStack(null)
            fm?.commit()
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
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
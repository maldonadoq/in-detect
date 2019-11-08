package maldonado.indetect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import maldonado.indetect.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val buttonIn = root.findViewById<Button>(R.id.cm_BtnSignIn)
        val buttonUp = root.findViewById<Button>(R.id.cm_BtnSignUp)

        buttonIn.setOnClickListener{
            val fm = fragmentManager?.beginTransaction()
            fm?.replace(R.id.nav_host_fragment, SignInFragment())
            fm?.addToBackStack(null)
            fm?.commit()
        }

        buttonUp.setOnClickListener{
            val fm = fragmentManager?.beginTransaction()
            fm?.replace(R.id.nav_host_fragment, SignUpFragment())
            fm?.addToBackStack(null)
            fm?.commit()
        }

        return root
    }
}
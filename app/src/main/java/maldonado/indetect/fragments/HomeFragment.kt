package maldonado.indetect.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
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
            Navigation.findNavController(it).navigate(R.id.action_nav_home_to_nav_sign_in)
        }

        buttonUp.setOnClickListener{
            Navigation.findNavController(it).navigate(R.id.action_nav_home_to_nav_sign_up)
        }

        return root
    }
}
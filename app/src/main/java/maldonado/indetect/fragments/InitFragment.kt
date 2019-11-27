package maldonado.indetect.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import maldonado.indetect.R
import maldonado.indetect.activities.InitActivity

class InitFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val tIntent = Intent(activity, InitActivity::class.java)
        tIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(tIntent)

        return inflater.inflate(R.layout.fragment_init, container, false)
    }
}
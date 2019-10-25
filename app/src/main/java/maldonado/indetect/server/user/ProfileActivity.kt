package maldonado.indetect.server.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.MainActivity
import maldonado.indetect.R

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var btnLogout: Button
    private lateinit var txtEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        txtEmail =findViewById(R.id.txtEmail)
        btnLogout = findViewById(R.id.btnLogout)

        /*auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        txtEmail.text = user?.email

        btnLogout.setOnClickListener{
            auth.signOut()

            intent = Intent(this@ProfileActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

            startActivity(intent)
        }*/
    }
}

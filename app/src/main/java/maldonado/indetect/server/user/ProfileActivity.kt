package maldonado.indetect.server.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.MainActivity
import maldonado.indetect.R
import maldonado.indetect.server.ServerActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var btnCar: Button
    private lateinit var txtEmail: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        txtEmail = findViewById(R.id.profile_TxtEmail)
        val txtName = findViewById<TextView>(R.id.profile_TxtName)
        val txtPhone = findViewById<TextView>(R.id.profile_TxtPhone)

        btnCar = findViewById(R.id.profile_Btn_Car)

        val user = auth.currentUser

        txtEmail.text = user?.email
        txtName.text = user?.displayName
        txtPhone.text = user?.phoneNumber

        btnCar.setOnClickListener{
            intent = Intent(this@ProfileActivity, ServerActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                intent = Intent(this@ProfileActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

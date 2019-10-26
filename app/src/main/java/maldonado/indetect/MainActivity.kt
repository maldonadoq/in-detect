package maldonado.indetect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.local.LocalActivity
import maldonado.indetect.server.user.ProfileActivity
import maldonado.indetect.server.user.SignInActivity
import maldonado.indetect.server.user.SignUpActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val fab = findViewById<View>(R.id.main_Fab)
        fab.setOnClickListener {
            intent = Intent(this@MainActivity, LocalActivity::class.java)
            startActivity(intent)
        }

        val buttonIn = findViewById<Button>(R.id.cm_BtnSignIn)
        val buttonUp = findViewById<Button>(R.id.cm_BtnSignUp)

        buttonIn.setOnClickListener{
            intent = Intent(this@MainActivity, SignInActivity::class.java)
            startActivity(intent)
        }

        buttonUp.setOnClickListener{
            intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

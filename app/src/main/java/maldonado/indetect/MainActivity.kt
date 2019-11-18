package maldonado.indetect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import maldonado.indetect.activities.AppActivity
import maldonado.indetect.activities.SignInActivity
import maldonado.indetect.activities.SignUpActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonIn = findViewById<Button>(R.id.main_BtnSignIn)
        val buttonUp = findViewById<Button>(R.id.main_BtnSignUp)
        val buttonSkip = findViewById<FloatingActionButton>(R.id.main_Skip)

        buttonIn.setOnClickListener{
            intent = Intent(this@MainActivity, SignInActivity::class.java)
            startActivity(intent)
        }

        buttonUp.setOnClickListener{
            intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        buttonSkip.setOnClickListener{
            intent = Intent(this@MainActivity, AppActivity::class.java)
            startActivity(intent)
        }
    }
}

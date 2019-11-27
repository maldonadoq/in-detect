package maldonado.indetect.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import maldonado.indetect.MainActivity
import maldonado.indetect.R

class InitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        val buttonIn = findViewById<Button>(R.id.main_BtnSignIn)
        val buttonUp = findViewById<Button>(R.id.main_BtnSignUp)
        val buttonSkip = findViewById<FloatingActionButton>(R.id.main_Skip)

        buttonIn.setOnClickListener{
            intent = Intent(this@InitActivity, SignInActivity::class.java)
            startActivity(intent)
        }

        buttonUp.setOnClickListener{
            intent = Intent(this@InitActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        buttonSkip.setOnClickListener{
            intent = Intent(this@InitActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

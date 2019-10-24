package maldonado.indetect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val buttonLocal = findViewById<Button>(R.id.btnLocal)
        val buttonServer = findViewById<Button>(R.id.btnServer)

        buttonLocal.setOnClickListener{
            intent = Intent(this@MainActivity, LocalActivity::class.java)
            startActivity(intent)
        }

        buttonServer.setOnClickListener{
            intent = Intent(this@MainActivity, ServerActivity::class.java)
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

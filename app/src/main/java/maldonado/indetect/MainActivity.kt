package maldonado.indetect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.activities.InitActivity

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth

    @SuppressLint("RestrictedApi", "SetTextI18n", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_local,
                R.id.nav_server,
                R.id.nav_gallery,
                R.id.nav_tools,
                R.id.nav_share,
                R.id.nav_init
            ), drawerLayout
        )

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val imgView = navView.getHeaderView(0)
        imgView.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_profile, null)

            val txtInfo = dialogView.findViewById<TextView>(R.id.profile_TxtInfo)

            txtInfo.text = "Name: ${user?.displayName}\nEmail:  ${user?.email}"

            val builder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Profile")

            builder.setCancelable(true)
            builder.show()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstStar = sharedPreferences.getBoolean("firstStart", true)
        if(firstStar){
            val edit = sharedPreferences.edit()
            edit.putBoolean("firstStart", false)
            edit.apply()

            intent = Intent(this@MainActivity, InitActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}

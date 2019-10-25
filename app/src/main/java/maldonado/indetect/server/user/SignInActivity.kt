package maldonado.indetect.server.user

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.R

class SignInActivity : AppCompatActivity() {
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressDialog: ProgressDialog

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        progressDialog = ProgressDialog(this)

        auth = FirebaseAuth.getInstance()
    }

    fun signIn(view: View){
        loginUser()
    }

    fun forgot(view: View){

    }

    fun register(view: View){
        intent = Intent(this@SignInActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun loginUser(){
        val email:String = txtEmail.text.toString()
        val password:String = txtPassword.text.toString()

        if(email.isNotEmpty() and password.isNotEmpty()){
            progressDialog.setMessage("Performing online registration ..")
            progressDialog.show()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){
                    task ->
                    if(task.isSuccessful){
                        action()
                        Toast.makeText(this, "Welcome: $email" , Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(this, "Authentication Error!", Toast.LENGTH_LONG).show()
                    }

                    progressDialog.dismiss()
                }
        }
        else{
            Toast.makeText(this, "Fields can't be empty!", Toast.LENGTH_LONG).show()
        }
    }

    private fun action(){
        intent = Intent(this@SignInActivity, ProfileActivity::class.java)
        startActivity(intent)
    }
}

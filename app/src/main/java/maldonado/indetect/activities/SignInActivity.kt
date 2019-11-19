@file:Suppress("DEPRECATION")

package maldonado.indetect.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.R

class SignInActivity : AppCompatActivity() {

    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        txtEmail = findViewById(R.id.si_TxtEmail)
        txtPassword = findViewById(R.id.si_TxtPassword)

        val btnSignIn = findViewById<Button>(R.id.si_BtnSignIn)
        val txtForgot = findViewById<TextView>(R.id.si_Forgot)
        val txtSignUp = findViewById<TextView>(R.id.si_SignUp)

        btnSignIn.setOnClickListener{
            loginUser()
        }
        txtForgot.setOnClickListener{
            forgot()
        }
        txtSignUp.setOnClickListener{
            intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        progressDialog = ProgressDialog(this)
    }

    private fun forgot(){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Recuperate your Account")
            .setNegativeButton("Cancel"){
                    _, _ ->

            }
            .setPositiveButton("Send"){
                    _, _ ->
                val forgotTxtEmail = dialogView.findViewById<EditText>(R.id.forgot_TxtEmail)

                val email = forgotTxtEmail.text.toString()

                if(email.isNotEmpty()){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                }

            }

        builder.show()
    }

    private fun loginUser(){
        val email:String = txtEmail.text.toString()
        val password:String = txtPassword.text.toString()

        if(email.isNotEmpty() and password.isNotEmpty()){
            progressDialog.setMessage("Performing online registration ..")
            progressDialog.show()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                        task ->
                    if(task.isSuccessful){
                        action()
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
        intent = Intent(this@SignInActivity, AppActivity::class.java)
        startActivity(intent)
        finish()
    }

}

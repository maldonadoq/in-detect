package maldonado.indetect.server.user

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import maldonado.indetect.R
import maldonado.indetect.server.ServerActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressDialog: ProgressDialog

    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        progressDialog = ProgressDialog(this)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("User")
    }

    fun signUp(view: View) {
        createAccount()
    }

    private fun createAccount(){
        val name:String = txtName.text.toString()
        val email:String = txtEmail.text.toString()
        val password:String = txtPassword.text.toString()

        if(name.isNotEmpty() and email.isNotEmpty() and password.isNotEmpty()){
            progressDialog.setMessage("Performing online registration ..")
            progressDialog.show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){
                    task ->

                    if(task.isSuccessful){
                        val user:FirebaseUser ?= auth.currentUser
                        verifyEmail(user)

                        val userDB = dbReference.child(user?.uid.toString())

                        userDB.child("Name").setValue(name)
                        action()
                    }

                    progressDialog.dismiss()
                }
                .addOnFailureListener {
                    exception ->

                    if (exception is FirebaseAuthUserCollisionException){
                        Toast.makeText(this, "This user already exist!", Toast.LENGTH_LONG).show()
                    }
                }
        }
        else{
            Toast.makeText(this, "Fields can't be empty!", Toast.LENGTH_LONG).show()
        }
    }

    private fun action(){
        intent = Intent(this@SignUpActivity, ServerActivity::class.java)
        startActivity(intent)
    }

    private fun verifyEmail(user:FirebaseUser?){
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this){
                task ->
                if (task.isComplete){
                    Toast.makeText(this, "Email Send", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(this, "Error Sending Email", Toast.LENGTH_LONG).show()
                }
            }
    }
}

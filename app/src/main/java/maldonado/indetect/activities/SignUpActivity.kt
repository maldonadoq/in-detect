@file:Suppress("DEPRECATION")

package maldonado.indetect.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import maldonado.indetect.MainActivity
import maldonado.indetect.R
import maldonado.indetect.fragments.SingletonNetwork
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {
    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtPhone: EditText
    private lateinit var progressDialog: ProgressDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var singletonNetwork: SingletonNetwork

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val btnSignUp = findViewById<Button>(R.id.su_BtnSignUp)
        btnSignUp.setOnClickListener{
            createAccount()
        }

        txtName = findViewById(R.id.su_TxtName)
        txtEmail = findViewById(R.id.su_TxtEmail)
        txtPassword = findViewById(R.id.su_TxtPassword)
        txtPhone = findViewById(R.id.su_TxtPhone)
        progressDialog = ProgressDialog(this)
        singletonNetwork = SingletonNetwork.getInstance(this)

        auth = FirebaseAuth.getInstance()
        dbReference = FirebaseDatabase.getInstance().reference.child("User")
    }

    private fun createAccount(){
        val name:String = txtName.text.toString()
        val email:String = txtEmail.text.toString()
        val password:String = txtPassword.text.toString()
        val phone:String = txtPhone.text.toString()

        if(name.isNotEmpty() and email.isNotEmpty() and password.isNotEmpty()){
            progressDialog.setMessage("Performing online registration ..")
            progressDialog.show()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                        task ->

                    if(task.isSuccessful){
                        val user: FirebaseUser?= auth.currentUser
                        val update = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        val obj = JSONObject()
                        obj.put("username", email)
                        singletonNetwork.sendPostRequest(obj, "user/create")

                        //verifyEmail(user)
                        user?.updateProfile(update)

                        val userDB = dbReference.child(user?.uid.toString())
                        userDB.child("type").setValue("normal")
                        userDB.child("phone").setValue(phone)

                        intent = Intent(this@SignUpActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    progressDialog.dismiss()
                }
                .addOnFailureListener {
                        exception ->

                    if (exception is FirebaseAuthUserCollisionException){
                        Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                    }
                }
        }
        else{
            Toast.makeText(this, "Fields can't be empty!", Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyEmail(user: FirebaseUser?){
        user?.sendEmailVerification()
            ?.addOnCompleteListener{
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

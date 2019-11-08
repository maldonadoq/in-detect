package maldonado.indetect.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import maldonado.indetect.R

class SignUpFragment : Fragment() {

    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtPhone: EditText
    private lateinit var progressDialog: ProgressDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_sign_up, container, false)

        val btnSignUp = root.findViewById<Button>(R.id.su_BtnSignUp)
        btnSignUp.setOnClickListener{
            createAccount()
        }

        txtName = root.findViewById(R.id.su_TxtName)
        txtEmail = root.findViewById(R.id.su_TxtEmail)
        txtPassword = root.findViewById(R.id.su_TxtPassword)
        txtPhone = root.findViewById(R.id.su_TxtPhone)
        progressDialog = ProgressDialog(root.context)

        auth = FirebaseAuth.getInstance()
        dbReference = FirebaseDatabase.getInstance().reference.child("User")

        return root
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

                        verifyEmail(user)
                        user?.updateProfile(update)

                        val userDB = dbReference.child(user?.uid.toString())
                        userDB.child("Type").setValue("1")
                        userDB.child("Phone").setValue(phone)

                        action()
                    }

                    progressDialog.dismiss()
                }
                .addOnFailureListener {
                        exception ->

                    if (exception is FirebaseAuthUserCollisionException){
                        Toast.makeText(root.context, "This user already exist!", Toast.LENGTH_LONG).show()
                    }
                }
        }
        else{
            Toast.makeText(root.context, "Fields can't be empty!", Toast.LENGTH_LONG).show()
        }
    }

    private fun action(){
        val fm = fragmentManager?.beginTransaction()
        fm?.replace(R.id.nav_host_fragment, ServerFragment())
        fm?.addToBackStack(null)
        fm?.commit()
    }

    private fun verifyEmail(user:FirebaseUser?){
        user?.sendEmailVerification()
            ?.addOnCompleteListener{
                    task ->
                if (task.isComplete){
                    Toast.makeText(root.context, "Email Send", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(root.context, "Error Sending Email", Toast.LENGTH_LONG).show()
                }
            }
    }

}
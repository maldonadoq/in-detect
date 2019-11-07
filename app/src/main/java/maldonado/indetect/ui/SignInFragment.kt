package maldonado.indetect.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import maldonado.indetect.R

class SignInFragment : Fragment() {

    private lateinit var root: View
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_sign_in, container, false)

        txtEmail = root.findViewById(R.id.si_TxtEmail)
        txtPassword = root.findViewById(R.id.si_TxtPassword)

        val btnSignIn = root.findViewById<Button>(R.id.si_BtnSignIn)
        val txtForgot = root.findViewById<TextView>(R.id.si_Forgot)
        val txtSignUp = root.findViewById<TextView>(R.id.si_SignUp)

        btnSignIn.setOnClickListener{
            loginUser()
        }
        txtForgot.setOnClickListener{
            forgot()
        }
        txtSignUp.setOnClickListener{

        }

        progressDialog = ProgressDialog(root.context)

        return root
    }

    private fun forgot(){
        val dialogView = LayoutInflater.from(root.context).inflate(R.layout.dialog_forgot, null)
        val builder = AlertDialog.Builder(root.context)
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
                            Toast.makeText(root.context, "Authentication Error!", Toast.LENGTH_LONG).show()
                        }

                        progressDialog.dismiss()
                    }
        }
        else{
            Toast.makeText(root.context, "Fields can't be empty!", Toast.LENGTH_LONG).show()
        }
    }

    private fun action(){
        val fm = fragmentManager?.beginTransaction()
        fm?.replace(R.id.nav_host_fragment, ProfileFragment())
        fm?.addToBackStack(null)
        fm?.commit()
    }

}
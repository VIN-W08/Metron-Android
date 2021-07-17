package com.example.vin.metron.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.vin.metron.MainActivity
import com.example.vin.metron.R
import com.example.vin.metron.UserPreferences
import com.example.vin.metron.UserViewModel
import com.example.vin.metron.databinding.ActivityRegisterBinding
import com.example.vin.metron.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import android.util.Patterns

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var userPreference: UserPreferences
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreferences(this)
        userViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[UserViewModel::class.java]
        binding.registerBtn.setOnClickListener(this)

        fAuth = Firebase.auth
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.registerBtn -> {
                val email = binding.emailET.text.toString().trim()
                val name = binding.nameET.text.toString().trim()
                val noPln = binding.plnNoET.text.toString().trim()
                val noPdam = binding.pdamNoET.text.toString().trim()
                val phone = binding.phoneET.text.toString().trim()
                val password = binding.passwordET.text.toString().trim()
                if(email.isEmpty()) {
                    binding.emailET.error = "Mohon input email"
                    return
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    binding.emailET.error = "Mohon input email yang valid"
                    return
                }
                if(name.isEmpty()) {
                    binding.nameET.error = "Mohon input nama"
                    return
                }
                if(noPln.isEmpty()) {
                    binding.plnNoET.error = "Mohon input nomor PLN"
                    return
                }
                if(noPdam.isEmpty()) {
                    binding.pdamNoET.error = "Mohon input nomor PDAM"
                    return
                }
                if(phone.isEmpty()) {
                    binding.phoneET.error = "Mohon input nomor hp"
                    return
                }
                if(password.isEmpty()) {
                    binding.passwordET.error = "Mohon input kata sandi"
                    return
                }
                if(password.length<8){
                    binding.passwordET.error = "Kata sandi harus minimal 8 karakter"
                    return
                }
                binding.progressBar.visibility = View.VISIBLE
                registerUser(email, name, noPln, noPdam, phone, password)
            }
        }
    }

    private fun registerUser(email: String, name: String, noPln: String,
                             noPdam: String, phone: String, password: String){
        fAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val db = FirebaseFirestore.getInstance()
                    val user = User(email, name, noPln, noPdam, phone, password)
                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener { documentRef ->
                            showToast("Register Successful")
                            userViewModel.getUserDataByDocRef(documentRef).observe(this, {
                                userPreference.setUser(it)
                            })
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                            showToast("Register failed, please check your data again")
                        }
                }else{
                    showToast("Register failed, please try again")
                }
            }
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
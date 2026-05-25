package com.udbvirtual.eventpulse.auth

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udbvirtual.eventpulse.R

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val firstNameEditText = findViewById<EditText>(R.id.editTextFirstName)
        val lastNameEditText = findViewById<EditText>(R.id.editTextLastName)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        signUpButton = findViewById(R.id.buttonSignUp)
        progressBar = findViewById(R.id.progressBarSignUp)

        signUpButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // Deshabilitar botón y mostrar ProgressBar
                toggleLoading(true)

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            val user = mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "email" to email,
                                "role" to "user" // Por defecto, todos son usuarios
                            )

                            // Guardar datos en Firestore
                            userId?.let {
                                db.collection("users").document(it).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG)
                                            .show()
                                    }
                                    .addOnCompleteListener {
                                        // Habilitar botón y ocultar ProgressBar
                                        toggleLoading(false)
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Error al registrar: ${task.exception?.message}", Toast.LENGTH_LONG)
                                .show()
                            // Habilitar botón y ocultar ProgressBar
                            toggleLoading(false)
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            signUpButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            signUpButton.isEnabled = true
        }
    }
}

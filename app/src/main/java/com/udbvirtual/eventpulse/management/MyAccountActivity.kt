package com.udbvirtual.eventpulse.management

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.udbvirtual.eventpulse.R

class MyAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userPhotoImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var newEmailEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var saveChangesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        auth = FirebaseAuth.getInstance()

        // Referencias a las vistas
        userPhotoImageView = findViewById(R.id.imageViewUserPhoto)
        userNameTextView = findViewById(R.id.textViewUserName)
        userEmailTextView = findViewById(R.id.textViewUserEmail)
        newEmailEditText = findViewById(R.id.editTextNewEmail)
        newPasswordEditText = findViewById(R.id.editTextNewPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        saveChangesButton = findViewById(R.id.buttonSaveChanges)

        // Cargar información del usuario
        loadUserInfo()

        // Guardar cambios
        saveChangesButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Mostrar nombre
            userNameTextView.text = currentUser.displayName ?: "Usuario"

            // Mostrar correo
            userEmailTextView.text = currentUser.email ?: "Sin correo electrónico"

            // Cargar foto de perfil
            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(userPhotoImageView)
            } else {
                userPhotoImageView.setImageResource(R.drawable.ic_user_placeholder)
            }
        }
    }

    private fun saveChanges() {
        val newEmail = newEmailEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (newEmail.isNotEmpty()) {
            updateEmail(newEmail)
        }

        if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (newPassword == confirmPassword) {
                updatePassword(newPassword)
            } else {
                Toast.makeText(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmail(newEmail: String) {
        val currentUser = auth.currentUser
        currentUser?.updateEmail(newEmail)
            ?.addOnSuccessListener {
                userEmailTextView.text = newEmail
                Toast.makeText(this, "Correo actualizado.", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar el correo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePassword(newPassword: String) {
        val currentUser = auth.currentUser
        currentUser?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                Toast.makeText(this, "Contraseña actualizada.", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar la contraseña: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

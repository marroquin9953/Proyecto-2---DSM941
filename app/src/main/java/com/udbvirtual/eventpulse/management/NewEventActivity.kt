package com.udbvirtual.eventpulse.management

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.udbvirtual.eventpulse.R
import java.util.*
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NewEventActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var storage: FirebaseStorage

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        checkAndRequestPermissions()

        // Referencias a los elementos del diseño
        val titleEditText = findViewById<EditText>(R.id.editTextEventTitle)
        val descriptionEditText = findViewById<EditText>(R.id.editTextEventDescription)
        dateEditText = findViewById(R.id.editTextEventDate)
        timeEditText = findViewById(R.id.editTextEventTime)
        val locationEditText = findViewById<EditText>(R.id.editTextEventLocation)
        val saveButton = findViewById<Button>(R.id.buttonSaveEvent)
        val uploadImageButton = findViewById<Button>(R.id.buttonUploadImage)
        val imageViewPreview = findViewById<ImageView>(R.id.imageViewPreview)

        // Configurar los campos de fecha y hora
        dateEditText.setOnClickListener { showDatePickerDialog() }
        timeEditText.setOnClickListener { showTimePickerDialog() }

        uploadImageButton.setOnClickListener {
            selectImage()
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val date = dateEditText.text.toString().trim()
            val time = timeEditText.text.toString().trim()
            val location = locationEditText.text.toString().trim()
            val createdBy = auth.currentUser?.uid

            // Validar los campos
            if (title.isEmpty() || description.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (createdBy == null) {
                Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri == null) {
                Toast.makeText(this, "Por favor, sube una imagen.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Subir imagen y guardar evento
            uploadImageAndSaveEvent(title, description, date, time, location, createdBy)
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 101)
    }

    private fun uploadImageAndSaveEvent(
        title: String,
        description: String,
        date: String,
        time: String,
        location: String,
        createdBy: String
    ) {
        val storageReference = storage.reference.child("events/${UUID.randomUUID()}.jpg")
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { imageUrl ->
                    saveEventToFirestore(title, description, date, time, location, createdBy, imageUrl.toString())
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error al subir imagen: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveEventToFirestore(
        title: String,
        description: String,
        date: String,
        time: String,
        location: String,
        createdBy: String,
        imageUrl: String
    ) {
        val event = mapOf(
            "title" to title,
            "description" to description,
            "date" to date,
            "time" to time,
            "location" to location,
            "createdBy" to createdBy,
            "imageUrl" to imageUrl
        )

        db.collection("events").add(event)
            .addOnSuccessListener {
                Toast.makeText(this, "Evento creado exitosamente.", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear evento: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                imageUri = selectedImageUri
                findViewById<ImageView>(R.id.imageViewPreview).setImageURI(selectedImageUri)
            } else {
                Toast.makeText(this, "No se seleccionó ninguna imagen.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                dateEditText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay))
            },
            year, month, day
        ).show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                timeEditText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            },
            hour, minute, true
        ).show()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "Permisos concedidos.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permisos denegados. No podrás seleccionar imágenes.", Toast.LENGTH_LONG).show()
        }
    }
}

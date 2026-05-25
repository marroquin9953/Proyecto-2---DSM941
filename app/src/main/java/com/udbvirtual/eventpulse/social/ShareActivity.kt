package com.udbvirtual.eventpulse.social

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.udbvirtual.eventpulse.R

class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        // Referencias a las vistas
        val titleTextView = findViewById<TextView>(R.id.textViewEventTitle)
        val descriptionTextView = findViewById<TextView>(R.id.textViewEventDescription)
        val shareButton = findViewById<Button>(R.id.buttonShareEvent)

        // Obtener datos del Intent
        val eventTitle = intent.getStringExtra("eventTitle") ?: "Sin título"
        val eventDescription = intent.getStringExtra("eventDescription") ?: "Sin descripción"
        val eventDate = intent.getStringExtra("eventDate") ?: "Fecha no disponible"
        val eventLocation = intent.getStringExtra("eventLocation") ?: "Ubicación no disponible"

        // Mostrar los datos en las vistas
        titleTextView.text = eventTitle
        descriptionTextView.text = eventDescription

        // Configurar el botón para compartir
        shareButton.setOnClickListener {
            shareEvent(eventTitle, eventDescription, eventDate, eventLocation)
        }
    }

    private fun shareEvent(title: String, description: String, date: String, location: String) {
        // Contenido a compartir
        val shareText = """
            ¡Únete a este evento!
            Título: $title
            Descripción: $description
            Fecha: $date
            Ubicación: $location
        """.trimIndent()

        // Intent de compartir
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        // Mostrar el selector para elegir la app con la que compartir
        val chooser = Intent.createChooser(shareIntent, "Compartir evento mediante:")
        startActivity(chooser)
    }
}

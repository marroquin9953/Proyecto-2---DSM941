package com.udbvirtual.eventpulse.management

import android.os.Bundle
import android.widget.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udbvirtual.eventpulse.R
import com.udbvirtual.eventpulse.model.Comment
import com.udbvirtual.eventpulse.social.ShareActivity

class ViewEventActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_event)

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Obtener el eventId del Intent
        val eventId = intent.getStringExtra("eventId") ?: return

        // Configurar RecyclerView
        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter(comments)
        recyclerViewComments.adapter = commentsAdapter

        // Referencias a las vistas
        val titleTextView = findViewById<TextView>(R.id.textViewEventTitle)
        val descriptionTextView = findViewById<TextView>(R.id.textViewEventDescription)
        val dateTextView = findViewById<TextView>(R.id.textViewEventDate)
        val timeTextView = findViewById<TextView>(R.id.textViewEventTime)
        val locationTextView = findViewById<TextView>(R.id.textViewEventLocation)
        val commentEditText = findViewById<EditText>(R.id.editTextComment)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val addCommentButton = findViewById<Button>(R.id.buttonAddComment)
        val imageViewEvent = findViewById<ImageView>(R.id.imageViewEvent)
        val confirmAttendanceButton = findViewById<Button>(R.id.buttonConfirmAttendance)

        // Cargar información del evento
        db.collection("events").document(eventId).get()
            .addOnSuccessListener { document ->
                titleTextView.text = document.getString("title")
                descriptionTextView.text = document.getString("description")
                dateTextView.text = document.getString("date")
                timeTextView.text = document.getString("time")
                locationTextView.text = document.getString("location")

                // Cargar la imagen usando Glide
                val imageUrl = document.getString("imageUrl")

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(imageViewEvent)
                } else {
                    imageViewEvent.setImageResource(R.drawable.ic_placeholder)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar los datos del evento: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        // Confirmar asistencia
        confirmAttendanceButton.setOnClickListener {
            val userId = auth.currentUser?.uid
            val userName = auth.currentUser?.displayName ?: "Anónimo"
            val userEmail = auth.currentUser?.email ?: "Sin correo"

            if (userId == null) {
                Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Datos del asistente
            val attendee = mapOf(
                "name" to userName,
                "email" to userEmail
            )

            // Guardar en la subcolección "attendees"
            db.collection("events").document(eventId).collection("attendees").document(userId)
                .set(attendee)
                .addOnSuccessListener {
                    Toast.makeText(this, "Asistencia confirmada.", Toast.LENGTH_SHORT).show()
                    confirmAttendanceButton.isEnabled = false // Deshabilitar el botón después de confirmar
                    confirmAttendanceButton.text = "Asistencia Confirmada"
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al confirmar asistencia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Verificar si el usuario ya confirmó asistencia
        db.collection("events").document(eventId).collection("attendees").document(auth.currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    confirmAttendanceButton.isEnabled = false
                    confirmAttendanceButton.text = "Asistencia Confirmada"
                }
            }

        // Cargar comentarios
        db.collection("events").document(eventId).collection("comments")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    comments.clear()
                    for (doc in snapshot) {
                        val comment = doc.toObject(Comment::class.java)
                        comments.add(comment)
                    }
                    commentsAdapter.notifyDataSetChanged()
                }
            }

        // Agregar comentario
        addCommentButton.setOnClickListener {
            val text = commentEditText.text.toString().trim()
            val rating = ratingBar.rating.toInt()
            val userId = auth.currentUser?.uid

            if (text.isEmpty() || rating == 0) {
                Toast.makeText(this, "Debes escribir un comentario y dar una calificación.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val comment = mapOf(
                "userId" to userId,
                "text" to text,
                "rating" to rating
            )

            db.collection("events").document(eventId).collection("comments")
                .add(comment)
                .addOnSuccessListener {
                    Toast.makeText(this, "Comentario agregado.", Toast.LENGTH_SHORT).show()
                    commentEditText.text.clear()
                    ratingBar.rating = 0f
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al agregar comentario: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        val shareButton = findViewById<Button>(R.id.buttonShareEvent)
        shareButton.setOnClickListener {
            val eventTitle = titleTextView.text.toString()
            val eventDescription = descriptionTextView.text.toString()
            val eventDate = dateTextView.text.toString()
            val eventLocation = locationTextView.text.toString()

            val intent = Intent(this, ShareActivity::class.java)
            intent.putExtra("eventTitle", eventTitle)
            intent.putExtra("eventDescription", eventDescription)
            intent.putExtra("eventDate", eventDate)
            intent.putExtra("eventLocation", eventLocation)
            startActivity(intent)
        }
    }
}

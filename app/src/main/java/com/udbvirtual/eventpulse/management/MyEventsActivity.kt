package com.udbvirtual.eventpulse.management

import EventsAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.udbvirtual.eventpulse.R
import com.udbvirtual.eventpulse.auth.LoginActivity
import com.udbvirtual.eventpulse.model.Event

class MyEventsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapter
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val events = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_events)

        recyclerView = findViewById(R.id.recyclerViewEvents)
        navigationView = findViewById(R.id.navigation_view)
        drawerLayout = findViewById(R.id.drawer_layout)

        // Configurar Google Sign-In
        configureGoogleSignIn()

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        eventsAdapter = EventsAdapter(events) { event ->
            val intent = Intent(this, ViewEventActivity::class.java)
            intent.putExtra("eventId", event.id)
            startActivity(intent)
        }
        recyclerView.adapter = eventsAdapter

        // Cargar eventos
        loadEvents()

        // Configurar el menú lateral
        setupNavigationMenu()

        // Configurar el header del menú
        configureNavigationHeader()
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun configureNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        val userPhotoImageView = headerView.findViewById<ImageView>(R.id.imageViewUserPhoto)
        val userNameTextView = headerView.findViewById<TextView>(R.id.textViewUserName)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewUserEmail)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            userNameTextView.text = currentUser.displayName ?: "Usuario"
            userEmailTextView.text = currentUser.email ?: "Sin correo"
            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(userPhotoImageView)
            } else {
                userPhotoImageView.setImageResource(R.drawable.ic_user_placeholder)
            }
        }
    }

    private fun loadEvents() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("events")
            .get()
            .addOnSuccessListener { result ->
                events.clear()

                val tasks = result.documents.map { document ->
                    val event = document.toObject(Event::class.java)
                    event?.id = document.id

                    db.collection("events")
                        .document(document.id)
                        .collection("attendees")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { attendeeDoc ->
                            if (attendeeDoc.exists() && event != null) {
                                events.add(event)
                            }
                        }
                }

                tasks.forEach { task ->
                    task.addOnCompleteListener {
                        eventsAdapter.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar eventos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_my_events -> {
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_my_account -> {
                    startActivity(Intent(this, MyAccountActivity::class.java))
                    true
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                R.id.activity_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_logout -> {
                    logOutAndRevokeGoogleAccess()
                    true
                }
                else -> false
            }
        }
    }

    private fun logOutAndRevokeGoogleAccess() {
        // Cerrar sesión de Firebase
        auth.signOut()

        // Revocar acceso de Google
        googleSignInClient.revokeAccess().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Acceso revocado. Elige una cuenta para iniciar sesión nuevamente.", Toast.LENGTH_SHORT).show()

                // Reiniciar la aplicación para ir al LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al revocar acceso de Google.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

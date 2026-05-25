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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.udbvirtual.eventpulse.R
import com.udbvirtual.eventpulse.auth.LoginActivity
import com.udbvirtual.eventpulse.model.Event

class UpcomingEventsActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var recyclerViewUpcomingEvents: RecyclerView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var fabAddEvent: FloatingActionButton
    private val db = FirebaseFirestore.getInstance()
    private val events = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming_events)

        // Inicializar FirebaseAuth y GoogleSignInClient
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configurar RecyclerView
        recyclerViewUpcomingEvents = findViewById(R.id.recyclerViewUpcomingEvents)
        recyclerViewUpcomingEvents.layoutManager = LinearLayoutManager(this)

        // Configurar botón flotante para agregar eventos
        fabAddEvent = findViewById(R.id.fabAddEvent)
        fabAddEvent.setOnClickListener {
            val intent = Intent(this, NewEventActivity::class.java)
            startActivity(intent)
        }

        // Configurar menú lateral
        navigationView = findViewById(R.id.navigation_view)
        setupNavigationMenu()

        // Cargar los eventos próximos
        loadEvents()
    }

    private fun setupNavigationMenu() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_my_events -> {
                    startActivity(Intent(this, MyEventsActivity::class.java))
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
                    signOut()
                    true
                }
                else -> false
            }
        }

        // Configurar el header del menú
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

    private fun signOut() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val providers = currentUser.providerData.map { it.providerId }

            if (providers.contains("google.com")) {
                // Cierre de sesión para usuarios de Google
                googleSignInClient.signOut().addOnCompleteListener(this) { googleSignOutTask ->
                    if (googleSignOutTask.isSuccessful) {
                        auth.signOut()
                        Toast.makeText(this, "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show()
                        redirectToLogin()
                    } else {
                        Toast.makeText(this, "Error al cerrar sesión con Google.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Cierre de sesión para usuarios de correo y contraseña
                auth.signOut()
                Toast.makeText(this, "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
        } else {
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loadEvents() {
        db.collection("events")
            .orderBy("date") // Ordenar por fecha de evento
            .get()
            .addOnSuccessListener { result ->
                events.clear()
                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    event.id = document.id
                    events.add(event)
                }
                recyclerViewUpcomingEvents.adapter = EventsAdapter(events) { event ->
                    val intent = Intent(this, ViewEventActivity::class.java)
                    intent.putExtra("eventId", event.id)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar eventos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

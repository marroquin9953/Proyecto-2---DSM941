package com.udbvirtual.eventpulse.management

import EventsAdapter
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.udbvirtual.eventpulse.R
import com.udbvirtual.eventpulse.model.Event
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var textViewTotalEvents: TextView
    private lateinit var textViewTotalParticipants: TextView
    private lateinit var editTextFilterDate: EditText
    private lateinit var buttonApplyFilters: Button
    private val db = FirebaseFirestore.getInstance()
    private val events = mutableListOf<Event>()
    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerViewHistory = findViewById(R.id.recyclerViewHistory)
        textViewTotalEvents = findViewById(R.id.textViewTotalEvents)
        textViewTotalParticipants = findViewById(R.id.textViewTotalParticipants)
        editTextFilterDate = findViewById(R.id.editTextFilterDate)
        buttonApplyFilters = findViewById(R.id.buttonApplyFilters)

        // Configurar RecyclerView
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        eventsAdapter = EventsAdapter(events) { event ->
            // Abrir el evento en detalle (opcional)
        }
        recyclerViewHistory.adapter = eventsAdapter

        // Configurar el filtro por fecha
        editTextFilterDate.setOnClickListener { showDatePickerDialog() }
        buttonApplyFilters.setOnClickListener { loadFilteredEvents() }

        // Cargar eventos históricos y estadísticas
        loadEvents()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                editTextFilterDate.setText(formattedDate)
            },
            year, month, day
        ).show()
    }

    private fun loadEvents() {
        db.collection("events")
            .whereLessThanOrEqualTo("date", getCurrentDate())
            .get()
            .addOnSuccessListener { result ->
                events.clear()
                var totalParticipants = 0

                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    event.id = document.id
                    events.add(event)

                    // Contar participantes
                    db.collection("events")
                        .document(document.id)
                        .collection("attendees")
                        .get()
                        .addOnSuccessListener { attendees ->
                            totalParticipants += attendees.size()
                            textViewTotalParticipants.text = "Total de participantes: $totalParticipants"
                        }
                }

                textViewTotalEvents.text = "Total de eventos: ${events.size}"
                eventsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Manejo de errores
            }
    }

    private fun loadFilteredEvents() {
        val filterDate = editTextFilterDate.text.toString()

        db.collection("events")
            .whereEqualTo("date", filterDate)
            .get()
            .addOnSuccessListener { result ->
                events.clear()

                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    event.id = document.id
                    events.add(event)
                }

                textViewTotalEvents.text = "Total de eventos filtrados: ${events.size}"
                eventsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Manejo de errores
            }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return String.format("%04d-%02d-%02d", year, month, day)
    }
}

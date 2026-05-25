import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.udbvirtual.eventpulse.R
import com.udbvirtual.eventpulse.model.Event

class EventsAdapter(
    private val events: List<Event>,
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
        holder.itemView.setOnClickListener { onEventClick(event) }
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewEvent: ImageView = itemView.findViewById(R.id.imageViewEvent)
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewEventTitle)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewEventDate)
        private val textViewLocation: TextView = itemView.findViewById(R.id.textViewEventLocation)

        fun bind(event: Event) {
            textViewTitle.text = event.title
            textViewDate.text = event.date
            textViewLocation.text = event.location

            // Usar Glide para cargar imágenes
            if (!event.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(event.imageUrl) // URL de la primera imagen
                    .placeholder(R.drawable.ic_placeholder) // Imagen de carga
                    .error(R.drawable.ic_error) // Imagen en caso de error
                    .into(imageViewEvent)
            } else {
                // Imagen predeterminada si no hay imágenes
                imageViewEvent.setImageResource(R.drawable.ic_placeholder)
            }
        }
    }
}

package com.udbvirtual.eventpulse.management

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.udbvirtual.eventpulse.R

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Si no existe el drawable 'isidro', usa el placeholder de usuario
        val photoView = findViewById<ImageView>(R.id.imageViewDeveloperPhoto)
        val resId = resources.getIdentifier("isidro", "drawable", packageName)
        if (resId == 0) {
            photoView.setImageResource(R.drawable.ic_user_placeholder)
        }
    }
}

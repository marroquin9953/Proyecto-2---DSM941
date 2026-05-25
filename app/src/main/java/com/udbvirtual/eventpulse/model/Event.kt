package com.udbvirtual.eventpulse.model

data class Event(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "",
    var time: String = "",
    var location: String = "",
    var createdBy: String = "",
    var imageUrl: String = "", // Campo para almacenar URLs de las imágenes
    var attendees: List<String> = emptyList() // Campo para almacenar los IDs de los asistentes
)

package edu.pe.taskmanager_pro.data.model

data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val priority: String = "Media", // Alta, Media, Baja
    val dueDate: String = "",
    val status: String = "Pendiente", // Pendiente, Completada
    val createdAt: Long = System.currentTimeMillis()
)

package edu.pe.taskmanager_pro.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import edu.pe.taskmanager_pro.data.model.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")

    // Obtener tareas en tiempo real
    fun getTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val subscription = tasksCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { it.toObject<Task>() } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { subscription.remove() }
    }

    // Crear tarea
    suspend fun createTask(task: Task): Result<String> {
        return try {
            val docRef = tasksCollection.document()
            val newTask = task.copy(id = docRef.id)
            docRef.set(newTask).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar tarea
    suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            tasksCollection.document(task.id).set(task).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar tarea
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            tasksCollection.document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Marcar como completada
    suspend fun markAsCompleted(taskId: String): Result<Unit> {
        return try {
            tasksCollection.document(taskId)
                .update("status", "Completada")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

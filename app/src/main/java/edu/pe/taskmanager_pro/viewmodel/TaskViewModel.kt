package edu.pe.taskmanager_pro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.pe.taskmanager_pro.data.model.Task
import edu.pe.taskmanager_pro.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            repository.getTasks(userId).collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.createTask(task)
                .onSuccess {
                    _message.value = "Tarea creada exitosamente"
                }
                .onFailure { error ->
                    _message.value = "Error: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateTask(task)
                .onSuccess {
                    _message.value = "Tarea actualizada exitosamente"
                }
                .onFailure { error ->
                    _message.value = "Error: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteTask(taskId)
                .onSuccess {
                    _message.value = "Tarea eliminada"
                }
                .onFailure { error ->
                    _message.value = "Error: ${error.message}"
                }
            _isLoading.value = false
        }
    }

    fun markAsCompleted(taskId: String) {
        viewModelScope.launch {
            repository.markAsCompleted(taskId)
                .onSuccess {
                    _message.value = "Tarea completada"
                }
                .onFailure { error ->
                    _message.value = "Error: ${error.message}"
                }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

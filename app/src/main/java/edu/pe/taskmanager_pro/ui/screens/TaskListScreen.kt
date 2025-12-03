package edu.pe.taskmanager_pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.pe.taskmanager_pro.data.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    userId: String,
    onCreateTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onLogout: () -> Unit,
    taskViewModel: edu.pe.taskmanager_pro.viewmodel.TaskViewModel
) {
    LaunchedEffect(userId) {
        taskViewModel.loadTasks(userId)
    }

    val tasks by taskViewModel.tasks.collectAsState()
    val message by taskViewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            kotlinx.coroutines.delay(2000)
            taskViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTask) {
                Icon(Icons.Default.Add, "Crear tarea")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            message?.let {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay tareas. Crea una nueva")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onEdit = { onEditTask(task) },
                            onDelete = { taskViewModel.deleteTask(task.id) },
                            onToggleComplete = { taskViewModel.markAsCompleted(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Eliminar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Prioridad: ${task.priority}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Fecha: ${task.dueDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado: ${task.status}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (task.status != "Completada") {
                    Button(onClick = onToggleComplete) {
                        Text("Completar")
                    }
                }
            }
        }
    }
}

package edu.pe.taskmanager_pro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.pe.taskmanager_pro.data.model.Task
import edu.pe.taskmanager_pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    userId: String,
    onCreateTask: () -> Unit,
    onEditTask: (Task) -> Unit,
    onLogout: () -> Unit,
    taskViewModel: edu.pe.taskmanager_pro.viewmodel.TaskViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var showFilterMenu by remember { mutableStateOf(false) }

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

    // Filtrar y separar tareas
    val filteredTasks = if (selectedFilter == "Todas") {
        tasks
    } else {
        tasks.filter { it.priority == selectedFilter }
    }

    val pendingTasks = filteredTasks
        .filter { it.status != "Completada" }
        .sortedBy { it.dueDate }

    val completedTasks = filteredTasks
        .filter { it.status == "Completada" }
        .sortedBy { it.dueDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas") },
                actions = {
                    // Boton de filtro
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, "Filtrar")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        listOf("Todas", "Alta", "Media", "Baja").forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter = filter
                                    showFilterMenu = false
                                },
                                leadingIcon = {
                                    if (selectedFilter == filter) {
                                        Icon(Icons.Default.Check, "Seleccionado")
                                    }
                                }
                            )
                        }
                    }

                    // Menu de usuario
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Menu, "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Perfil") },
                            onClick = {
                                showMenu = false
                                // TODO: Navegar a perfil
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, "Perfil")
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesion") },
                            onClick = {
                                showMenu = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ExitToApp, "Cerrar sesion")
                            }
                        )
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

            // Mostrar filtro activo
            if (selectedFilter != "Todas") {
                Surface(
                    color = Primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtro",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Mostrando prioridad: $selectedFilter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay tareas. Crea una nueva")
                }
            } else if (pendingTasks.isEmpty() && completedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay tareas con este filtro")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Seccion de tareas pendientes
                    if (pendingTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pendientes (${pendingTasks.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(pendingTasks) { task ->
                            TaskItem(
                                task = task,
                                onEdit = { onEditTask(task) },
                                onDelete = { taskViewModel.deleteTask(task.id) },
                                onToggleComplete = { taskViewModel.markAsCompleted(task.id) }
                            )
                        }
                    }

                    // Seccion de tareas completadas
                    if (completedTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Completadas (${completedTasks.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Success,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(completedTasks) { task ->
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
}

@Composable
fun TaskItem(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "Alta" -> HighPriority
        "Media" -> MediumPriority
        "Baja" -> LowPriority
        else -> MediumPriority
    }

    val isCompleted = task.status == "Completada"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(2.dp, priorityColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor,
                        modifier = Modifier.size(12.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            "Editar",
                            tint = Primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            "Eliminar",
                            tint = Error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = "Prioridad",
                        tint = priorityColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.priority,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = priorityColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Fecha",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.dueDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) Success.copy(alpha = 0.2f)
                           else Warning.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule,
                            contentDescription = "Estado",
                            tint = if (isCompleted) Success else Warning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.status,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) Success else Warning
                        )
                    }
                }

                if (!isCompleted) {
                    Button(
                        onClick = onToggleComplete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Success
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Completar")
                    }
                }
            }
        }
    }
}

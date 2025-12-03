package edu.pe.taskmanager_pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.pe.taskmanager_pro.data.model.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditTaskScreen(
    userId: String,
    task: Task? = null,
    onNavigateBack: () -> Unit,
    taskViewModel: edu.pe.taskmanager_pro.viewmodel.TaskViewModel
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "Media") }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val isLoading by taskViewModel.isLoading.collectAsState()
    val message by taskViewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            if (it.contains("exitosamente")) {
                kotlinx.coroutines.delay(1000)
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (task == null) "Crear Tarea" else "Editar Tarea") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de prioridad
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = priority,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Prioridad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Alta", "Media", "Baja").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                priority = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Selector de fecha con calendario
            OutlinedTextField(
                value = dueDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha límite") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // DatePicker Dialog
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                calendar.timeInMillis = millis
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                                dueDate = dateFormat.format(calendar.time)
                            }
                            showDatePicker = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Button(
                onClick = {
                    val newTask = Task(
                        id = task?.id ?: "",
                        userId = userId,
                        title = title,
                        priority = priority,
                        dueDate = dueDate,
                        status = task?.status ?: "Pendiente"
                    )
                    if (task == null) {
                        taskViewModel.createTask(newTask)
                    } else {
                        taskViewModel.updateTask(newTask)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && title.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (task == null) "Crear" else "Actualizar")
                }
            }

            message?.let {
                Text(
                    text = it,
                    color = if (it.contains("Error")) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

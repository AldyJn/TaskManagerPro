package edu.pe.taskmanager_pro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.pe.taskmanager_pro.ui.screens.*
import edu.pe.taskmanager_pro.viewmodel.AuthState
import edu.pe.taskmanager_pro.viewmodel.AuthViewModel
import edu.pe.taskmanager_pro.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object TaskList : Screen("task_list")
    object CreateTask : Screen("create_task")
    object EditTask : Screen("edit_task")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.TaskList.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.TaskList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.TaskList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.TaskList.route) {
            val userId = (authState as? AuthState.Authenticated)?.userId ?: ""
            TaskListScreen(
                userId = userId,
                onCreateTask = {
                    navController.navigate(Screen.CreateTask.route)
                },
                onEditTask = { task ->
                    taskViewModel.selectTask(task)
                    navController.navigate(Screen.EditTask.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                taskViewModel = taskViewModel
            )
        }

        composable(Screen.CreateTask.route) {
            val userId = (authState as? AuthState.Authenticated)?.userId ?: ""
            CreateEditTaskScreen(
                userId = userId,
                task = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                taskViewModel = taskViewModel
            )
        }

        composable(Screen.EditTask.route) {
            val userId = (authState as? AuthState.Authenticated)?.userId ?: ""
            val selectedTask by taskViewModel.selectedTask.collectAsState()
            CreateEditTaskScreen(
                userId = userId,
                task = selectedTask,
                onNavigateBack = {
                    taskViewModel.selectTask(null)
                    navController.popBackStack()
                },
                taskViewModel = taskViewModel
            )
        }
    }
}

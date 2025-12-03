package edu.pe.taskmanager_pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import edu.pe.taskmanager_pro.navigation.AppNavigation
import edu.pe.taskmanager_pro.ui.theme.Taskmanager_proTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Taskmanager_proTheme {
                AppNavigation()
            }
        }
    }
}
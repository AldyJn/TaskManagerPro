package edu.pe.taskmanager_pro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.pe.taskmanager_pro.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val currentUser = repository.getCurrentUser()
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser.uid)
        } else {
            AuthState.Unauthenticated
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.register(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Error al registrar")
                }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.login(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Error al iniciar sesion")
                }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

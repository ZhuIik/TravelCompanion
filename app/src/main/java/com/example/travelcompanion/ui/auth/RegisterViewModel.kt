package com.example.travelcompanion.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelcompanion.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val registeredSuccessfully: Boolean = false,
    val registeredUserId: Long? = null
)

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun register() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Введите email и пароль")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.register(email, password)) {
                is UserRepository.RegisterResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        registeredSuccessfully = true,
                        registeredUserId = result.userId
                    )
                }
                is UserRepository.RegisterResult.EmailAlreadyExists -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Пользователь с таким email уже существует"
                    )
                }
            }
        }
    }
}

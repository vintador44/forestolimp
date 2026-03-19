package org.example.project.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.data.api.RetrofitClient

class AuthViewModel : ViewModel() {
    private val api = RetrofitClient.instance

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Попытка входа для: $email")
            _authState.value = AuthState.Loading
            try {
                val request = org.example.project.data.api.LoginRequest(email = email, password = password)
                val response = api.login(request)
                Log.d("AuthViewModel", "Код ответа: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    RetrofitClient.token = authResponse.token
                    RetrofitClient.currentUser = authResponse.user
                    Log.d("AuthViewModel", "Вход успешен, пользователь: ${authResponse.user?.fio}")
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Ошибка входа"
                    Log.e("AuthViewModel", "Ошибка входа: $errorMsg")
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Исключение при входе", e)
                _authState.value = AuthState.Error("Ошибка входа: ${e.message}")
            }
        }
    }

    fun register(fio: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = org.example.project.data.api.RegisterRequest(email = email, password = password, fio = fio)
                val response = api.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    RetrofitClient.token = authResponse.token
                    RetrofitClient.currentUser = authResponse.user
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Ошибка регистрации")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Ошибка регистрации: ${e.message}")
            }
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

package org.example.project.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var fio by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()


    LaunchedEffect(authState) {
        Log.d("LoginScreen", "Текущее состояние authState: $authState")
        if (authState is AuthViewModel.AuthState.Success) {
            Log.d("LoginScreen", "Состояние Success получено, вызываем onLoginSuccess()")
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Вход" else "Регистрация",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!isLogin) {
            OutlinedTextField(
                value = fio,
                onValueChange = { fio = it },
                label = { Text("ФИО") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isLogin) {
                    viewModel.login(email, password)
                } else {
                    viewModel.register(fio, email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthViewModel.AuthState.Loading
        ) {
            Text(if (authState is AuthViewModel.AuthState.Loading) "Загрузка..." else if (isLogin) "Войти" else "Зарегистрироваться")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(if (isLogin) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка для ручного перехода (если вдруг автомат не сработает)
        if (authState is AuthViewModel.AuthState.Success) {
            Button(onClick = onLoginSuccess) {
                Text("Перейти на карту (Success!)")
            }
        }


        if (authState is AuthViewModel.AuthState.Error) {
            Text(
                text = (authState as AuthViewModel.AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

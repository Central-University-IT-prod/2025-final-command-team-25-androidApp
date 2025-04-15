package com.chupapis.bookit

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.auth.AuthRepositoryImpl
import com.chupapis.bookit.navigation.AppNavigation
import com.chupapis.bookit.ui.viewmodules.AuthViewModel
import com.chupapis.bookit.ui.theme.BookITTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("Insrt")
        println(intent.data)

        enableEdgeToEdge()
        setContent {
            BookITTheme {
                val context = LocalContext.current
                // Получаем SharedPreferences
                val sharedPreferences = remember {
                    context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                }
                // Инициализируем репозиторий авторизации
                val authRepository = AuthRepositoryImpl(
                    RetrofitClient.getRetrofitInstance(context),
                    sharedPreferences
                )
                // Инициализируем AuthViewModel через фабрику
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.AuthViewModelFactory(authRepository)
                )
                var isAuthorized by remember { mutableStateOf(false) }
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    val token = authRepository.getValidAccessToken()
                    isAuthorized = token != null
                    Log.d("DEBUG", "Токен найден: ${token != null}")
                    authViewModel.getUserInfo()
                }

                val userResult by authViewModel.userResult.observeAsState()
                val accessLevel = userResult?.getOrNull()?.access_level

                var invite: String? = null
                if (intent.data != null) {
                    println("Use intent")
                    invite = intent.data?.getQueryParameter("booking_id")
                }

                AppNavigation(
                    isAuthorized = isAuthorized,
                    invite = invite,
                    accessLevel = accessLevel,
                    sharedPreferences = sharedPreferences,
                    navController = navController,
                    onAuthSuccess = {
                        isAuthorized = true
                        authViewModel.getUserInfo()
                    },
                    onLogout = {
                        isAuthorized = false
                    },
                    authRepositoryImpl = authRepository,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

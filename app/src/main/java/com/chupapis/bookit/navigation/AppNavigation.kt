package com.chupapis.bookit.navigation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.chupapis.bookit.data.model.booking.BookingResponseChange
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.admin.AdminRepositoryImpl
import com.chupapis.bookit.data.repository.auth.AuthRepositoryImpl
import com.chupapis.bookit.data.repository.seats.FreeSeatRepositoryImpl
import com.chupapis.bookit.ui.AdminViewModel
import com.chupapis.bookit.ui.FreeSeatViewModel
import com.chupapis.bookit.ui.ProfileViewModel
import com.chupapis.bookit.ui.dialogs.CalendarAlertDialog
import com.chupapis.bookit.ui.dialogs.TimeAlertDialog
import com.chupapis.bookit.ui.main.admin.AdminBookingDetailScreen
import com.chupapis.bookit.ui.main.admin.AdminScreen
import com.chupapis.bookit.ui.main.admin.AdminScreenMirror
import com.chupapis.bookit.ui.main.admin.ClientInfoScreen
import com.chupapis.bookit.ui.main.admin.qr.QrCodeScreen
import com.chupapis.bookit.ui.main.admin.qr.QrScannerScreen
import com.chupapis.bookit.ui.main.homepage.CoworkingSelectionScreen
import com.chupapis.bookit.ui.main.homepage.MapScreen
import com.chupapis.bookit.ui.main.homepage.PaymentScreen
import com.chupapis.bookit.ui.main.invite.InviteScreen
import com.chupapis.bookit.ui.main.mybooking.MyBookingsScreen
import com.chupapis.bookit.ui.main.profile.ProfileScreen
import com.chupapis.bookit.ui.verification.VerificationScreen
import com.chupapis.bookit.ui.viewmodules.AuthViewModel
import com.chupapis.bookit.ui.viewmodules.MapViewModel
import kotlinx.serialization.Serializable


const val DEEPLINK_DOMAIN = "prod"


@Serializable
data class DeepLinkScreen(val id: Int)


@Composable
fun AppNavigation(
    isAuthorized: Boolean,
    invite: String?,
    accessLevel: String?,
    sharedPreferences: SharedPreferences,
    navController: NavHostController = rememberNavController(),
    onAuthSuccess: () -> Unit,
    onLogout: () -> Unit,
    authRepositoryImpl: AuthRepositoryImpl,
    authViewModel: AuthViewModel
) {
    Log.d("DEBUG", "Проверка доступа: isAuthorized=$isAuthorized, accessLevel=$accessLevel")
    val context = LocalContext.current

    var inviteState by remember { mutableStateOf(invite) }

    if (!isAuthorized) {
        // Если не авторизован, переходим в граф авторизации
        AuthNavGraph(
            navController = navController,
            onAuthSuccess = onAuthSuccess,
            authViewModel = authViewModel
        )
    } else {
        if (accessLevel == null) {
            // Если accessLevel еще не загружен, показываем экран загрузки
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Определяем стартовый экран:
            // Для администратора всегда "coworking_selection" (если еще не выбран),
            // для обычного пользователя – "coworking_selection", если коворкинг не выбран, иначе "home"
            val selectedCoworking = sharedPreferences.getString("selected_coworking_id", null)
            val startDestination = if (selectedCoworking == null) {
                "coworking_selection"
            } else {
                if (accessLevel.uppercase() == "ADMIN") "admin" else "home"
            }

            val apiService = RetrofitClient.getRetrofitInstance(context)

            val profileViewModel = viewModel<ProfileViewModel>(
                factory = ProfileViewModel.ProfileViewModelFactory(authRepositoryImpl, apiService)
            )
            val mapViewModel = viewModel<MapViewModel>(
                factory = MapViewModel.MapViewModelFactory(authRepositoryImpl, apiService, sharedPreferences)
            )

            // Определяем текущий маршрут, чтобы условно отобразить нижнюю панель
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route

            Scaffold(
                bottomBar = {
                    if (currentRoute != "coworking_selection") {
                        BottomNavBar(
                            navController = navController,
                            accessLevel = accessLevel
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(
                        navController = navController,
                        startDestination = if (inviteState == null) startDestination else "invite"
                    ) {
                        composable("home") {
                            MapScreen(
                                navController = navController,
                                authRepository = authRepositoryImpl,
                                mapViewModel = mapViewModel
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onLogout = { onLogout() },
                                navController = navController
                            )
                        }
                        composable("verification") {
                            VerificationScreen(
                                profileViewModel = profileViewModel,
                                navController = navController
                            )
                        }
                        composable("calendar") {
                            CalendarAlertDialog(
                                mapViewModel = mapViewModel,
                                onSave = { navController.popBackStack() },
                                onDismiss = { navController.popBackStack() }
                            )
                        }
                        composable("time") {
                            TimeAlertDialog(
                                mapViewModel = mapViewModel,
                                onSave = { navController.popBackStack() },
                                onDismiss = { navController.popBackStack() }
                            )
                        }
                        composable("invite") {
                            InviteScreen(
                                bookingId = inviteState ?: "",
                                onInvited = {
                                    navController.navigate("home")
                                    inviteState = null
                                },
                                authRepository = authRepositoryImpl,
                                apiService = apiService
                            )
                        }
                        composable("booking") {
                            PaymentScreen(
                                mapViewModel = mapViewModel,
                                onBack = { navController.popBackStack() },
                                onBook = {
                                    navController.popBackStack()
                                    Toast.makeText(context, "Booked", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        composable("qr_scanner") {
                            QrScannerScreen(repository = authRepositoryImpl)
                        }
                        composable("qr_code/{token}") { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            QrCodeScreen(token = token)
                        }
                        composable("my_booking") {
                            MyBookingsScreen(
                                navController = navController,
                                authRepository = authRepositoryImpl,
                                sharedPreferences
                            )
                        }

                        composable("coworking_selection") {
                            // Экран выбора коворкинга – здесь пользователь не может выйти, пока не выберет коворкинг.
                            val freeSeatRepository = FreeSeatRepositoryImpl(
                                api = apiService,
                                authRepository = authRepositoryImpl
                            )
                            val freeSeatViewModel: FreeSeatViewModel = viewModel(
                                factory = FreeSeatViewModel.FreeSeatViewModelFactory(
                                    freeSeatRepository
                                )
                            )
                            CoworkingSelectionScreen(
                                freeSeatViewModel = freeSeatViewModel,
                                sharedPreferences = sharedPreferences,
                                onSelectionComplete = {
                                    if (accessLevel.uppercase() == "ADMIN") {
                                        navController.navigate("admin") {
                                            popUpTo("coworking_selection") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("home") {
                                            popUpTo("coworking_selection") { inclusive = true }
                                        }
                                    }
                                },
                                isAdmin = accessLevel == "ADMIN",
                                onCoworkingClick = { coworkingId ->
                                    // Сохраняем выбранный coworking id в SharedPreferences
                                    sharedPreferences.edit().putString("selected_coworking_id", coworkingId).apply()
                                }
                            )
                        }



                        composable("admin") {
                            AdminScreenMirror(
                                onClientClick = { userId ->
                                    navController.navigate("client_bookings/$userId")
                                },
                                authRepository = authRepositoryImpl
                            )
                        }


                        composable("client_bookings/{userId}") { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            ClientInfoScreen(
                                userId = userId,
                                onBookingClick = { bookingId ->
                                    // Переход к детальному экрану бронирования
                                    navController.navigate("booking_detail/$bookingId")
                                },
                                onBack = {
                                    navController.popBackStack()
                                },
                                authRepository = authRepositoryImpl
                            )
                        }


                        composable("booking_detail/{bookingId}") { backStackEntry ->
                            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                            val context = LocalContext.current
                            val viewModel: AdminViewModel = viewModel(
                                factory = AdminViewModel.AdminViewModelFactory(
                                    AdminRepositoryImpl(
                                        RetrofitClient.getRetrofitInstance(context),
                                        authRepository = authRepositoryImpl
                                    )
                                )
                            )
                            val bookingsResult by viewModel.bookingsResult.observeAsState()
                            var currentBooking by remember {
                                mutableStateOf<BookingResponseChange?>(
                                    null
                                )
                            }

                            LaunchedEffect(bookingsResult) {
                                currentBooking = bookingsResult
                                    ?.getOrNull()
                                    ?.firstOrNull { it.booking_id == bookingId }
                            }

                            if (currentBooking != null) {
                                AdminBookingDetailScreen(
                                    booking = currentBooking!!,
                                    onBack = { navController.popBackStack() },
                                    onSelectDateTime = {
                                        // здесь можно реализовать логику выбора даты/времени
                                    },
                                    onDeleteBooking = {
                                        // здесь можно реализовать логику удаления брони
                                    }
                                )
                            } else {
                                // Показ индикатора загрузки, пока бронь не загружена
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        composable<DeepLinkScreen>(
                            deepLinks = listOf(
                                navDeepLink<DeepLinkScreen>(
                                    basePath = "bookit://$DEEPLINK_DOMAIN"
                                )
                            )
                        ) {
                            val id = it.toRoute<DeepLinkScreen>().id
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                println("SDFSDFSDF")
                                Text(text = "The ID is $id")
                            }
                        }
                    }

                }
            }
        }
    }
}
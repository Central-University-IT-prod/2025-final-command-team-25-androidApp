package com.chupapis.bookit.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.chupapis.bookit.ui.auth.LoginScreen
import com.chupapis.bookit.ui.auth.RegisterScreen
import com.chupapis.bookit.ui.viewmodules.AuthViewModel
import kotlinx.serialization.Serializable


@Serializable
data class Profile(val id: String)


@Composable
fun AuthNavGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { onAuthSuccess() },
                onNavigateToRegister = { navController.navigate("register") },
                viewModel = authViewModel
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    onAuthSuccess()
                },
                onBackToLogin = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        println("bookit://$DEEPLINK_DOMAIN")

        composable<DeepLinkScreen>(
            deepLinks = listOf(
                navDeepLink<DeepLinkScreen>(
                    basePath = "bookit://$DEEPLINK_DOMAIN"
                )
            )
        ) {
            println("QWEQWEQWE")
            val id = it.toRoute<DeepLinkScreen>().id
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                println("ASDASDASD")
                Text(text = "The ID is $id")
            }
        }

        composable<Profile>(
            deepLinks = listOf(
                navDeepLink<Profile>(basePath = "bookit://prod/profile")
            )
        ) { backStackEntry ->
            Text(backStackEntry.toRoute<Profile>().id)
        }


        composable(
            route = "invite/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "bookit://prod/invite/{id}" }
            )
        ) { backStackEntry ->
            println("ASDFGH")
            val id = backStackEntry.arguments?.getString("id")
            Text("IDIDIDID")
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController, accessLevel: String?) {
    NavigationBar {
        if (accessLevel == "ADMIN") {
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate("admin") {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Home, contentDescription = "Админ") },
                label = { Text("Админ") }
            )
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate("qr_scanner") {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "QR Сканер") },
                label = { Text("QR Сканер") }
            )
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                label = { Text("Профиль") }
            )
        } else {
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate("home") {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
                label = { Text("Главная") }
            )
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate("my_booking") {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.DateRange, contentDescription = "Моя бронь") },
                label = { Text("Мои брони") }
            )
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate("profile") {
                        launchSingleTop = true
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                label = { Text("Профиль") }
            )
        }
    }
}

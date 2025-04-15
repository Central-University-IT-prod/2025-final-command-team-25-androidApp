package com.chupapis.bookit.ui.main.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chupapis.bookit.R
import com.chupapis.bookit.ui.ProfileUiState
import com.chupapis.bookit.ui.ProfileViewModel
import com.chupapis.bookit.ui.UserProfile
import com.chupapis.bookit.ui.main.admin.StatusBadge

enum class UserStatus {
    GUEST, GOLD, ADMIN, STUDENT
}


@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit, navController: NavController) {
    when (val uiState = viewModel.uiState) {
        is ProfileUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            viewModel.fetchUserProfile()
            CircularProgressIndicator()
        }

        is ProfileUiState.Success -> {
            println(uiState.profile.verificationLevel)

            ProfileCard(
                userProfile = uiState.profile,
                onLogout = {
                    viewModel.logout()
                    onLogout()
                },
                onAddVerification = {
                    navController.navigate("verification")
                }
            )
        }

        is ProfileUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = uiState.message, color = Color.Red)
            }
        }
    }
}

@Composable
fun ProfileCard(
    userProfile: UserProfile,
    onLogout: () -> Unit,
    onAddVerification: () -> Unit
) {
    // Определяем статус пользователя
    val userStatus = UserStatus.valueOf(userProfile.status)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Аватар пользователя
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Имя пользователя
            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Text(
                text = userProfile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Статус пользователя в виде бейджа
            StatusBadge(status = userStatus)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (userStatus != UserStatus.ADMIN && userProfile.verificationLevel == "GUEST") {
                    Button(
                        onClick = {
                            // Обработчик верификации аккаунта
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6026BA))
                    ) {
                        Text(text = "Verify Account")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Button(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6327BE))
                ) {
                    Text(text = "Logout")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: UserStatus) {
    val color = when (status) {
        UserStatus.GUEST -> Color.Gray
        UserStatus.GOLD -> Color(0xFFFFD700)
        UserStatus.ADMIN -> Color.Red
        UserStatus.STUDENT -> Color(0xFFD3C459)
    }

    Box(
        modifier = Modifier
            .background(color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}


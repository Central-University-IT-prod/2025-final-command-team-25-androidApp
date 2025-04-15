package com.chupapis.bookit.ui.main.invite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.chupapis.bookit.data.model.invite.AcceptRequest
import com.chupapis.bookit.data.network.ApiService
import com.chupapis.bookit.data.repository.auth.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun InviteScreen(
    bookingId: String,
    onInvited: () -> Unit,
    authRepository: AuthRepository,
    apiService: ApiService
) {
    // Set in center
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    // Run coroutine
                    val response =
                        apiService.acceptInvite(authRepository.getValidAccessToken()!!, AcceptRequest(bookingId.replace("_", "-")))
                    if (response.isSuccessful) {
                        onInvited()
                    } else {
                        println("Error: ${response.code()}")
                    }
                }
            }
        ) {
            Text("Accept")
        }
    }
}
package com.chupapis.bookit.ui.main.admin

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chupapis.bookit.data.model.admins.ClientResponse
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.admin.AdminRepositoryImpl
import com.chupapis.bookit.data.repository.auth.AuthRepository
import com.chupapis.bookit.ui.AdminViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreenMirror(
    onClientClick: (String) -> Unit,
    authRepository: AuthRepository
) {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(
        factory = AdminViewModel.AdminViewModelFactory(
            AdminRepositoryImpl(
                RetrofitClient.getRetrofitInstance(context),
                authRepository = authRepository
            )
        )
    )

    val clientsResult by viewModel.clientsResult.observeAsState()
    var clients by remember { mutableStateOf<List<ClientResponse>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val deleteUserResult by viewModel.deleteUserResult.observeAsState()
    var clientToDelete by remember { mutableStateOf<ClientResponse?>(null) }
    var deletionUserId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Подгружаем всех клиентов при старте экрана
    LaunchedEffect(Unit) {
        viewModel.fetchAllClients()
    }

    // Следим за результатом и обновляем список клиентов
    LaunchedEffect(clientsResult) {
        clientsResult?.onSuccess { list ->
            clients = list.filter { it.access_level != "ADMIN" }
            isDataLoaded = true
        }?.onFailure { error ->
            isDataLoaded = true
            Toast.makeText(context, "Ошибка загрузки: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Обработка результата удаления пользователя
    LaunchedEffect(deleteUserResult) {
        deleteUserResult?.onSuccess {
            deletionUserId?.let { id ->
                // Локальное обновление
                clients = clients.filter { it.client_id != id }
                // Альтернативно или дополнительно обновить список с сервера
                viewModel.fetchAllClients()
            }
            deletionUserId = null
        }?.onFailure { error ->
            Toast.makeText(
                context,
                "Ошибка при удалении пользователя: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


        Column {
            // Добавляем текстовое поле вместо topBar

            Text(
                text = "Список клиентов",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing),
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        viewModel.fetchAllClients()
                        kotlinx.coroutines.delay(500)
                        isRefreshing = false
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (!isDataLoaded) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                } else if (clients.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Нет клиентов для отображения")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp, bottom = 0.dp, end = 16.dp)
                    ) {
                        items(clients, key = { it.client_id }) { client ->
                            var isSwiped by remember { mutableStateOf(false) }
                            val offsetX = remember { Animatable(0f) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (isSwiped) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(end = 16.dp),
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = {
                                            clientToDelete = client
                                            showDeleteDialog = true
                                        }) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                                contentDescription = "Удалить",
                                                tint = androidx.compose.ui.graphics.Color.Gray,
                                            )
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    coroutineScope.launch {
                                                        if (offsetX.value < -100) {
                                                            isSwiped = true
                                                            offsetX.animateTo(-200f)
                                                        } else {
                                                            offsetX.animateTo(0f)
                                                            isSwiped = false
                                                        }
                                                    }
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    coroutineScope.launch {
                                                        val newOffset =
                                                            (offsetX.value + dragAmount).coerceIn(-200f, 0f)
                                                        offsetX.snapTo(newOffset)
                                                    }
                                                }
                                            )
                                        }
                                        .fillMaxWidth()
                                ) {
                                    ClientItem(
                                        client = client,
                                        onClick = { onClientClick(client.client_id) }
                                    )
                                }
                            }

                    }
                }
            }
        }
    }

    // Диалог удаления пользователя
    if (showDeleteDialog && clientToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                clientToDelete = null
            },
            title = { Text("Удаление пользователя") },
            text = { Text("Вы уверены, что хотите удалить ${clientToDelete?.username}?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    deletionUserId = clientToDelete?.client_id
                    deletionUserId?.let { viewModel.deleteUser(it) }
                    clientToDelete = null
                    showDeleteDialog = false
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteDialog = false
                    clientToDelete = null
                }) {
                    Text("Нет")
                }
            }
        )
    }
}


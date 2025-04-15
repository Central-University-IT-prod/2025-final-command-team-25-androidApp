package com.chupapis.bookit.ui.main.homepage

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chupapis.bookit.ui.FreeSeatViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoworkingSelectionScreen(
    freeSeatViewModel: FreeSeatViewModel,
    sharedPreferences: SharedPreferences,
    onSelectionComplete: () -> Unit,
    onCoworkingClick: (coworkingId: String) -> Unit, // Новый callback для получения coworking id
    isAdmin: Boolean = false  // проверка прав администратора
) {
    val context = LocalContext.current

    // Загружаем список коворкингов при входе на экран
    LaunchedEffect(Unit) {
        freeSeatViewModel.loadCoworkings()
    }
    // При каждом изменении LiveData экран автоматически обновляется
    val coworkings by freeSeatViewModel.coworkings.observeAsState(emptyList())

    // Состояния для диалога создания коворкинга
    var showDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var tzOffsetInput by remember { mutableStateOf("") }
    var svgUri by remember { mutableStateOf<Uri?>(null) }

    // Лаунчер для выбора файла SVG
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        svgUri = uri
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Создать коворкинг",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Название коворкинга", fontSize = 14.sp) },
                        textStyle = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Адрес коворкинга", fontSize = 14.sp) },
                        textStyle = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tzOffsetInput,
                        onValueChange = { tzOffsetInput = it },
                        label = { Text("Смещение часового пояса", fontSize = 14.sp) },
                        textStyle = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { launcher.launch("image/svg+xml") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (svgUri == null) "Выбрать SVG файл" else "Файл выбран",
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (svgUri == null) {
                            Toast.makeText(context, "Пожалуйста, выберите SVG файл", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showDialog = false
                        val tz = tzOffsetInput.toIntOrNull() ?: 0
                        val file = uriToFile(context, svgUri!!)
                        freeSeatViewModel.createCoworking(
                            name = nameInput,
                            address = addressInput,
                            tzOffset = tz,
                            file = file,
                            onSuccess = {
                                // Очищаем поля формы после успешного создания
                                nameInput = ""
                                addressInput = ""
                                tzOffsetInput = ""
                                svgUri = null
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, "Ошибка: $errorMsg", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text("Создать", fontSize = 14.sp)
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Отмена", fontSize = 14.sp)
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Выберите коворкинг",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить коворкинг")
                }
            }
        }
    ) { paddingValues ->
        if (coworkings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(coworkings.reversed()) { coworking ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                // Сохраняем данные в SharedPreferences
                                sharedPreferences.edit()
                                    .putString("selected_coworking_id", coworking.coworking_id)
                                    .putString("selected_coworking_name", coworking.title)
                                    .apply()
                                // Вызываем callback с coworking id
                                onCoworkingClick(coworking.coworking_id)
                                // Завершаем выбор
                                onSelectionComplete()
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = coworking.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Адрес",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = coworking.address,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Вспомогательная функция для копирования содержимого URI во временный файл.
 */
fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val tempFile = File.createTempFile("temp", ".svg", context.cacheDir)
    inputStream.use { input ->
        tempFile.outputStream().use { output ->
            input?.copyTo(output)
        }
    }
    return tempFile
}

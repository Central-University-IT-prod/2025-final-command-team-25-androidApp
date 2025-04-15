package com.chupapis.bookit.ui.main.admin.qr

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.chupapis.bookit.data.model.qr.CheckQrResponse
import com.chupapis.bookit.data.network.RetrofitClient
import com.chupapis.bookit.data.repository.auth.AuthRepositoryImpl
import com.chupapis.bookit.data.repository.qr.CheckQrRepositoryImpl
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(repository: AuthRepositoryImpl) {
    val context = LocalContext.current

    var qrResult by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var checkQRResponse by remember { mutableStateOf<CheckQrResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }

    // Запрос разрешения на камеру
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // При получении QR-данных выполняем запрос на сервер
    LaunchedEffect(qrResult) {
        qrResult?.let { qrData ->
            val apiService = RetrofitClient.getRetrofitInstance(context)
            val checkQRRepository = CheckQrRepositoryImpl(apiService, repository)
            val result = checkQRRepository.checkQr(qrData)
            result.onSuccess { response ->
                checkQRResponse = response
                error = null
                // Если требуется верификация, отображаем AlertDialog
                if (response.need_verification) {
                    showDialog = true
                }
            }.onFailure { e ->
                error = e.message
            }
        }
    }

    // Если разрешение получено и шторка не показана, показываем сканер
    if (hasCameraPermission && !showSheet) {
        AndroidView(
            factory = { ctx ->
                DecoratedBarcodeView(ctx).apply {
                    barcodeView.decoderFactory =
                        DefaultDecoderFactory(listOf(com.google.zxing.BarcodeFormat.QR_CODE))
                    decodeContinuous { result ->
                        qrResult = result.text
                        showSheet = true
                        pause() // Останавливаем сканирование после первого успешного считывания
                    }
                }
            },
            update = { scannerView ->
                scannerView.resume()
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Шторка для отображения результата запроса (если не требуется верификация)
    if (showSheet && (checkQRResponse == null || !checkQRResponse!!.need_verification)) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                qrResult = null
                checkQRResponse = null
                error = null
            }
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                when {
                    checkQRResponse != null -> {
                        Text(text = "QR проверен")
                        Text(text = "Booking ID: ${checkQRResponse!!.booking_id}")
                        Text(text = "Seat ID: ${checkQRResponse!!.seat.seat_id}")
                        Text(text = "Seat UUID: ${checkQRResponse!!.seat.seat_uuid}")
                        Text(text = "User: ${checkQRResponse!!.user.username} (${checkQRResponse!!.user.email})")
                    }
                    error != null -> {
                        Text(text = "Ошибка: $error")
                    }
                    else -> {
                        Text(text = "Загрузка...")
                    }
                }
            }
        }
    }

    // AlertDialog, если требуется верификация
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                // При отмене можно также сбросить состояние, чтобы повторить сканирование
                showSheet = false
                qrResult = null
                checkQRResponse = null
            },
            title = { Text("Требуется верификация") },
            text = { Text("Пользователю необходимо предъявить паспорт") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Здесь выполняется запрос на сервер для подтверждения верификации.
                        // Например: checkQRRepository.confirmVerification(qrResult!!)
                        // Замените следующую строку на реальную логику отправки запроса.
                        sendVerificationRequest(qrResult)
                        showDialog = false
                        showSheet = false
                    }
                ) {
                    Text("Подтверждение")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        showSheet = false
                        qrResult = null
                        checkQRResponse = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Пример функции, отправляющей запрос на сервер для подтверждения верификации.
// Необходимо реализовать реальную логику отправки запроса.
private fun sendVerificationRequest(qrData: String?) {
    // Реализация запроса на сервер для подтверждения верификации
    // Например, можно использовать Retrofit для вызова соответствующего API-метода.




}


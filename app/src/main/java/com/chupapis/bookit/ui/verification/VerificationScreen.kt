package com.chupapis.bookit.ui.verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chupapis.bookit.ui.ProfileViewModel

@Composable
fun VerificationScreen(
    profileViewModel: ProfileViewModel,
    navController: NavController
) {
    if (!profileViewModel.passportSet) {
        VerificationView(
            onSave = {
                profileViewModel.setPassportData(it)
                navController.navigate("profile")
            }
        )
    }
}


@Composable
fun VerificationView(
    onSave: (PassportData) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var passportSeries by remember { mutableStateOf("") }
    var passportNumber by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Заполнение паспортных данных", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = passportSeries,
            onValueChange = { passportSeries = it },
            label = { Text("Серия паспорта") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = passportNumber,
            onValueChange = { passportNumber = it },
            label = { Text("Номер паспорта") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )


        errorMessage?.let {
            Text(it, color = Color.Red)
        }

        Button(
            onClick = {
                val validationError = validatePassportData(
                    fullName,
                    passportSeries,
                    passportNumber,
                )
                if (validationError == null) {
                    onSave(
                        PassportData(
                            fullName,
                            passportSeries,
                            passportNumber,
                        )
                    )
                } else {
                    errorMessage = validationError
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отправить")
        }
    }
}

data class PassportData(
    val name: String,
    val series: String,
    val number: String,
)

fun validatePassportData(
    fullName: String,
    passportSeries: String,
    passportNumber: String,
): String? {
    if (fullName.isBlank() || passportSeries.isBlank() || passportNumber.isBlank()) {
        return "Все поля должны быть заполнены."
    }
    if (passportSeries.length != 4 || passportSeries.any { !it.isDigit() }) {
        return "Серия паспорта должна содержать ровно 4 цифры."
    }
    if (passportNumber.length != 6 || passportNumber.any { !it.isDigit() }) {
        return "Номер паспорта должен содержать ровно 6 цифр."
    }

    return null
}

@Preview
@Composable
fun VerificationScreenPreview() {
    Scaffold {
        VerificationView(onSave = {}, modifier = Modifier.padding(it))
    }
}

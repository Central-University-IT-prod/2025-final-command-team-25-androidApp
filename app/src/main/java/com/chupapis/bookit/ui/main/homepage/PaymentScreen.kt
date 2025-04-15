package com.chupapis.bookit.ui.main.homepage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chupapis.bookit.ui.viewmodules.MapViewModel
import java.time.ZoneOffset

@Composable
fun PaymentScreen(
    mapViewModel: MapViewModel,
    onBook: () -> Unit,
    onBack: () -> Unit
) {
    println(mapViewModel.userProfile!!.access_level)

    if (mapViewModel.userProfile!!.access_level == "STUDENT") {
        mapViewModel.book()
        onBook()
        return
    }

    val seats by mapViewModel.seats.collectAsState()


    PaymentView(
        amount = (
                seats.sumOf { it.price } * (
                        mapViewModel.endDateTime!!.toEpochSecond(ZoneOffset.ofHours(0)) - mapViewModel.startDateTime!!.toEpochSecond(
                            ZoneOffset.ofHours(0)
                        )
                        ) / 60 / 30 / 100f
                ).toString(),
        onPayClick = {
            mapViewModel.book()
            onBook()
        },
        onBack = onBack
    )
}


@Composable
fun PaymentView(
    amount: String = "100.00",
    onPayClick: (String) -> Unit,
    onBack: () -> Unit // Fixed typo from obBack to onBack
) {
    var selectedMethod by remember { mutableStateOf("Credit Card") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Bar with Back Button and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "Payment",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(
                text = "Total Amount: $amount $",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Select Payment Method:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PaymentMethodSelector(
                selectedMethod = selectedMethod,
                onMethodSelected = { selectedMethod = it }
            )
        }

        Button(
            onClick = { onPayClick(selectedMethod) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Pay Now")
        }
    }
}

@Composable
fun PaymentMethodSelector(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    val methods = listOf("Credit Card", "PayPal", "Google Pay")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        methods.forEach { method ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { onMethodSelected(method) }
            ) {
                RadioButton(
                    selected = (method == selectedMethod),
                    onClick = { onMethodSelected(method) }
                )
                Text(
                    text = method,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun PaymentViewPreview() {
    PaymentView(
        amount = "100.00",
        onPayClick = {},
        onBack = {}
    )
}
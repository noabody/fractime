package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FracTimeApp()
                }
            }
        }
    }
}

@Composable
private fun FracTimeApp() {
    var entriesText by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("7.5") }
    var totalOutput by remember { mutableStateOf("–") }
    var diffOutput by remember { mutableStateOf("–") }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun calculate() {
        val target = parseNumber(targetText)
        if (target == null) {
            errorText = "Enter a numeric target (e.g., 7.5)."
            return
        }

        val values = entriesText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { parseNumber(it) }
            .toList()

        if (values.isEmpty()) {
            errorText = "No valid numeric values found."
            return
        }

        val total = values.sum()
        val diff = target - total

        errorText = null
        totalOutput = formatSummary(total)
        diffOutput = formatSummary(diff)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Fractional Time Calculator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = entriesText,
            onValueChange = { entriesText = it; errorText = null },
            label = { Text("Enter fractional hours (one per line)") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
        )

        OutlinedTextField(
            value = targetText,
            onValueChange = { targetText = it; errorText = null },
            label = { Text("Target hours (e.g., 7.5 for 7h 30m)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
        )

        Button(
            onClick = ::calculate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        errorText?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Total: $totalOutput", fontFamily = FontFamily.Monospace)
                Text("Difference (target - total): $diffOutput", fontFamily = FontFamily.Monospace)
            }
        }
    }
}

private val NUMBER_REGEX = Regex("""^-?(\d+([.,]\d*)?|[.,]\d+)$""")

private fun parseNumber(raw: String): Double? {
    val trimmed = raw.trim()
    if (!NUMBER_REGEX.matches(trimmed)) return null
    return trimmed.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
}

private fun formatSummary(hours: Double): String {
    val clean = if (abs(hours) < 1e-9) 0.0 else hours
    val decimal = String.format(Locale.US, "%.2f", clean)
    return "$decimal hours (${toHoursMinutes(clean)})"
}

private fun toHoursMinutes(hours: Double): String {
    if (hours.isNaN() || hours.isInfinite()) return "N/A"
    val sign = if (hours < 0.0) "-" else ""
    val absVal = abs(hours)
    var h = floor(absVal).toInt()
    var m = ((absVal - h) * 60.0).roundToInt()
    if (m == 60) {
        h += 1
        m = 0
    }
    return "$sign${h}h ${m.toString().padStart(2, '0')}m"
}

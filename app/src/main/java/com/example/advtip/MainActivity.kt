package com.example.advtip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.advtip.ui.theme.AdvTipTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    // Instantiate Gemini AI (API)
    private val geminiService = GeminiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdvTipTheme {
                // Pass geminiService to TipCalculator
                TipCalculator(geminiService = geminiService)
            }
        }
    }
}

// Preview is for testing purposes
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AdvTipTheme {
        // For preview, we'll use TipCalculator without the geminiService
        TipCalculatorPreview()
    }
}

// Passed Gemini to the TipCalculator itself so entire functionality works
// We're using experimental designs in the app, so we need to enable them (OptIn)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipCalculator(geminiService: GeminiService) {
    // State variables
    var amountInput by remember { mutableStateOf("") }
    var tipPercentInput by remember { mutableStateOf("") }
    // Service quality default is Good
    var serviceQuality by remember { mutableStateOf("Good") }
    var aiSuggestion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var isLegendExpanded by remember { mutableStateOf(false) }

    //  Launch coroutine and declare the overall material theme for app colors
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    // Convert inputs to doubles
    // Elvis operator to handle null cases easily
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipPercentInput.toDoubleOrNull() ?: 0.0
    val tip = amount * tipPercent / 100

    // Use scaffold to add top bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Tip Calculator") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.primary,
                    titleContentColor = colors.onPrimary
                )
            )
        },
        // Add padding to the content
        containerColor = colors.background,
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Input fields with colors and labels (boxes)
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Bill Amount") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedLabelColor = colors.primary,
                        cursorColor = colors.primary
                    )
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = tipPercentInput,
                    onValueChange = { tipPercentInput = it },
                    label = { Text("Tip %") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedLabelColor = colors.primary,
                        cursorColor = colors.primary
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Dropdown menu for service quality
                Box {
                    Button(
                        onClick = { showDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)
                    ) {
                        Text("Service Quality: $serviceQuality", color = colors.onSecondary)
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        val serviceOptions = listOf("Poor", "Fair", "Good", "Excellent")
                        serviceOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = colors.onSurface) },
                                onClick = {
                                    serviceQuality = option
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Tip legend for percentages
                // Both expanded and collapsed functionality
                Card(
                    onClick = { isLegendExpanded = !isLegendExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Info",
                                    tint = colors.onSurface, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tipping Guide", style = MaterialTheme.typography.titleSmall,
                                    color = colors.onSurface)
                            }
                            Icon(
                                imageVector = if (isLegendExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                tint = colors.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Expanded content
                        if (isLegendExpanded) {
                            Spacer(Modifier.height(8.dp))
                            Text("Poor: 10–12%", color = colors.onSurface)
                            Text("Fair: 13–17%", color = colors.onSurface)
                            Text("Good: 15–20%", color = colors.onSurface)
                            Text("Excellent: 18–25%", color = colors.onSurface)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Button to get AI tip suggestion
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            aiSuggestion = geminiService.generateTipSuggestion(
                                billAmount = amount,
                                serviceQuality = serviceQuality,
                                groupSize = 2
                            )
                            isLoading = false
                        }
                    },
                    enabled = amountInput.isNotEmpty() && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp),
                            color = colors.onPrimary)
                    } else {
                        Text("Get AI Tip Suggestion", color = colors.onPrimary)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Tip: $${"%.2f".format(tip)}", color = colors.onBackground)
                Text("Total: $${"%.2f".format(amount + tip)}", color = colors.onBackground)

                if (aiSuggestion.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                    ) {
                        Text(
                            text = "\n$aiSuggestion",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
            }
        }
    )
}

// Preview version without geminiService
@Composable
fun TipCalculatorPreview() {
    var amountInput by remember { mutableStateOf("") }
    var tipPercentInput by remember { mutableStateOf("") }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipPercentInput.toDoubleOrNull() ?: 0.0
    val tip = amount * tipPercent / 100

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Bill Amount") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = tipPercentInput,
            onValueChange = { tipPercentInput = it },
            label = { Text("Tip %") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(16.dp))
        Text("Tip: $${"%.2f".format(tip)}")
    }
}
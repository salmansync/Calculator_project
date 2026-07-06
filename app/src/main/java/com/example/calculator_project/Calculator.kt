package com.example.calculator_project

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val standardButtons = listOf(
    "()", "%", "C", "⌫",
    "7", "8", "9", "÷",
    "4", "5", "6", "x",
    "1", "2", "3", "-",
    "=", "0", ".", "+"
)

@Composable
fun Calculator(modifier: Modifier = Modifier, viewModel: CalculatorViewModel) {
    val equationText = viewModel.equationText.observeAsState()
    val resultText = viewModel.resultText.observeAsState()
    val historyList by viewModel.historyList.observeAsState(emptyList())

    var showHistory by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            confirmButton = {
                TextButton(onClick = { showHistory = false }) { Text("Close") }
            },
            title = { Text("Calculation History") },
            text = {
                LazyColumn {
                    items(historyList) { item ->
                        Text(text = item, fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        )
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val char = keyEvent.utf16CodePoint.toChar()

                    when {
                        char.isDigit() -> viewModel.onButtonClick(char.toString())
                        char == '+' -> viewModel.onButtonClick("+")
                        char == '-' -> viewModel.onButtonClick("-")
                        char == '*' || char == 'x' || char == 'X' -> viewModel.onButtonClick("x")
                        char == '/' || char == '÷' -> viewModel.onButtonClick("÷")
                        char == '.' || char == ',' -> viewModel.onButtonClick(".")
                        char == '%' -> viewModel.onButtonClick("%")
                        char == '(' || char == ')' -> viewModel.onButtonClick("()")
                        char == 'c' || char == 'C' || keyEvent.key == Key.Escape -> viewModel.onButtonClick("C")
                        keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter || char == '=' -> viewModel.onButtonClick("=")
                        keyEvent.key == Key.Backspace || keyEvent.key == Key.Delete -> viewModel.onButtonClick("⌫")
                    }
                    true
                } else {
                    false
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Button(
                    onClick = { showHistory = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("History", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = equationText.value ?: "",
                style = TextStyle(fontSize = 30.sp, textAlign = TextAlign.End),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = resultText.value ?: "",
                style = TextStyle(fontSize = 60.sp, textAlign = TextAlign.End),
                maxLines = 2,
                modifier = Modifier.padding(end = 12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
            ) {
                items(standardButtons) { buttonSymbol ->
                    CalculatorButton(
                        btn = buttonSymbol,
                        onClick = { viewModel.onButtonClick(buttonSymbol) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(btn: String, onClick: () -> Unit) {
    Box(modifier = Modifier.padding(6.dp)) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(80.dp),
            contentColor = getTextColor(btn),
            containerColor = getBackgroundColor(btn)
        ) {
            Text(text = btn, fontSize = 26.sp)
        }
    }
}

fun getBackgroundColor(btn: String): Color {
    if (btn == "⌫" || btn == "C") return Color(0xFFD00000)
    if (btn == "()" || btn == "%") return Color.White
    if (btn == "÷" || btn == "x" || btn == "+" || btn == "-") return Color(0xFF000000)
    if (btn == "=") return Color(0xFFD00000)
    return Color(0xFFFFFFFF)
}

fun getTextColor(btn: String): Color {
    if (btn == "⌫" || btn == "C") return Color(0xFFFFFFFF)
    if (btn == "÷" || btn == "x" || btn == "+" || btn == "-" || btn == "=") return Color(0xFFFFFFFF)
    return Color(0xFF000000)
}
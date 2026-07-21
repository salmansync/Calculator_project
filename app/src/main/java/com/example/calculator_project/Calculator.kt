package com.example.calculator_project

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// This list defines the exact layout of the 24 buttons (4 columns x 6 rows)
val standardButtons = listOf(
    "√", "Xʸ", "C", "⌫",
    "()", "¹/ₓ", "%", "÷",
    "7", "8", "9", "x",
    "4", "5", "6", "-",
    "1", "2", "3", "+",
    "ANS", "0", ".", "="
)

// --- MAIN UI FUNCTION ---
// @Composable means this function draws visuals on the phone screen
@Composable
fun Calculator(modifier: Modifier = Modifier, viewModel: CalculatorViewModel) {

    // --- SECTION 1: OBSERVERS (The UI's Ears) ---
    // observeAsState listens to the ViewModel. If the math changes, it instantly redraws the screen.
    val equationText by viewModel.equationText.observeAsState("")
    val resultText by viewModel.resultText.observeAsState("0")
    val historyList by viewModel.historyList.observeAsState(emptyList())
    // Observe the power state to change button color dynamically
    val isPoweredOn by viewModel.isPoweredOn.observeAsState(true)

    // UI States (Remembering if History popup is open, or if Dark Mode is ON)
    var showHistory by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Requests keyboard focus as soon as the app launches
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // --- SECTION 2: HISTORY POPUP DIALOG ---
    if (showHistory) {
        AlertDialog(
            onDismissRequest = { showHistory = false },
            confirmButton = { TextButton(onClick = { showHistory = false }) { Text("Close") } },
            title = { Text("Calculation History") },
            text = {
                // LazyColumn allows scrolling if the history list gets too long
                LazyColumn { items(historyList) { item -> Text(text = item, fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp)) } }
            }
        )
    }

    // --- SECTION 3: MAIN APP LAYOUT ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xFF121212) else Color(0xFFF3F3F3)) // Background flips based on theme
            .focusRequester(focusRequester)
            .focusable()
            // Physical Keyboard Listener (Connects keyboard typing to the ViewModel logic)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val char = keyEvent.utf16CodePoint.toChar()
                    when {
                        char.isDigit() || char in listOf('+', '-', '.', '^', '%', '(', ')') -> viewModel.onButtonClick(char.toString())
                        char in listOf('*', 'x', 'X') -> viewModel.onButtonClick("x")
                        char in listOf('/', '÷') -> viewModel.onButtonClick("÷")
                        char in listOf('s', 'S') -> viewModel.onButtonClick("√")
                        char in listOf('m', 'M') -> viewModel.onButtonClick("MOD")
                        char in listOf('c', 'C') || keyEvent.key == Key.Escape -> viewModel.onButtonClick("C")
                        keyEvent.key in listOf(Key.Enter, Key.NumPadEnter) || char == '=' -> viewModel.onButtonClick("=")
                        keyEvent.key in listOf(Key.Backspace, Key.Delete) -> viewModel.onButtonClick("⌫")
                    }
                    true
                } else false
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {

            // --- AREA A: THE DISPLAY SCREEN ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 'weight(1f)' makes the display stretch to push the buttons to the bottom
                    .background(if (isDarkMode) Color(0xFF242424) else Color(0xFFEAEAEA), shape = RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                // The Top small text (The typed equation)
                Text(
                    text = equationText,
                    style = TextStyle(fontSize = 32.sp, textAlign = TextAlign.End, color = if(isDarkMode) Color.LightGray else Color.DarkGray),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                // The Bottom large text (The live answer)
                Text(
                    text = resultText,
                    style = TextStyle(fontSize = 56.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color.Black),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- AREA B: THE CONTROL ROW ---
            val btnBg = if (isDarkMode) Color(0xFF333333) else Color.White
            val btnTxt = if (isDarkMode) Color.White else Color.Black

            // Power Button color logic: Green if ON, Red if OFF
            val powerBtnBg = if (isPoweredOn) Color(0xFF4CAF50) else Color(0xFFD32F2F)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ControlRowButton("History", btnBg, btnTxt) { showHistory = true }
                ControlRowButton("MOD", btnBg, btnTxt) { viewModel.onButtonClick("MOD") }
                ControlRowButton(if (isDarkMode) "☀️" else "🌙", btnBg, btnTxt) { isDarkMode = !isDarkMode }
                // ON/OFF now at the right corner with dynamic colors
                ControlRowButton("ON/OFF", powerBtnBg, Color.White) { viewModel.onButtonClick("ON/OFF") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- AREA C: THE NUMBER GRID ---
            // LazyVerticalGrid takes the 'standardButtons' list and deals them out into 4 neat columns
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(standardButtons) { btn -> CalculatorButton(btn, isDarkMode) { viewModel.onButtonClick(btn) } }
            }
        }
    }
}

// --- HELPER COMPONENT FUNCTIONS ---
// Makes the main UI code much smaller by reusing these button templates

@Composable
fun ControlRowButton(text: String, bg: Color, txt: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = txt),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) { Text(text, fontSize = 14.sp) }
}

@Composable
fun CalculatorButton(btn: String, isDarkMode: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1.1f).fillMaxWidth(), // aspect ratio forces square-like buttons
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = getBackgroundColor(btn, isDarkMode),
            contentColor = getTextColor(btn, isDarkMode)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        // Adjusts font size automatically if the button has long text (like "ANS")
        Text(text = btn, fontSize = if (btn.length > 2) 18.sp else 28.sp, fontWeight = if (btn in listOf("=", "C", "⌫")) FontWeight.Bold else FontWeight.Normal)
    }
}

// --- SMART COLOR FUNCTIONS ---
// Changes button colors dynamically based on what symbol it is and whether Dark Mode is ON

fun getBackgroundColor(btn: String, isDarkMode: Boolean): Color {
    if (btn in listOf("C", "⌫")) return Color(0xFFD31313) // Clear and Backspace are always Red
    if (btn in listOf("÷", "x", "-", "+")) return Color(0xFF050505) // Main Operators are always Black
    return if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFFFFFFF) // Numbers flip between dark grey and white
}

fun getTextColor(btn: String, isDarkMode: Boolean): Color {
    if (btn in listOf("C", "⌫", "÷", "x", "-", "+")) return Color.White // Dark buttons need white text
    return if (isDarkMode) Color.White else Color.Black // Regular buttons flip text color
}
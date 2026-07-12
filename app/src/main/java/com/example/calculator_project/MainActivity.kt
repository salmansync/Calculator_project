package com.example.calculator_project
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.calculator_project.ui.theme.Calculator_projectTheme

// --- MAIN ACTIVITY: The front door of the app ---
// ComponentActivity is the basic window where the Android app runs.
class MainActivity : ComponentActivity() {

    // onCreate is the very first function that runs when the app is opened.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- CONNECTING THE BRAIN (VIEW MODEL) ---
        // We use ViewModelProvider so that if the user rotates the phone,
        // the app doesn't lose its memory or reset to zero.
        val calculatorViewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]

        // Makes the app stretch to the very edges of the phone screen
        enableEdgeToEdge()

        // --- LAUNCHING THE UI ---
        // setContent draws the actual visual design on the screen.
        setContent {
            Calculator_projectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Here we call our Calculator UI file and hand it the Brain (calculatorViewModel)
                    // so the screen and the logic can talk to each other.
                    Calculator(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = calculatorViewModel
                    )
                }
            }
        }
    }
}
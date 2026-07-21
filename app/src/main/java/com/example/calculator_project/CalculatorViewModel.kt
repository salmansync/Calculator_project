package com.example.calculator_project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

// --- VIEW MODEL: The logic engine that does all the math ---
class CalculatorViewModel : ViewModel() {

    // --- SECTION 1: APP MEMORY (LIVE DATA) ---
    // LiveData acts like a radio broadcaster. When the data changes here,
    // it automatically tells the UI screen to update itself.
    private val _equationText = MutableLiveData("")
    val equationText: LiveData<String> = _equationText // Stores what the user is typing (e.g., "5+5")

    private val _resultText = MutableLiveData("0")
    val resultText: LiveData<String> = _resultText // Stores the live answer (e.g., "10")

    private val _historyList = MutableLiveData<List<String>>(emptyList())
    val historyList: LiveData<List<String>> = _historyList // Stores old calculations

    private val _isPoweredOn = MutableLiveData(true)
    val isPoweredOn: LiveData<Boolean> = _isPoweredOn // Tracks if calculator is ON or OFF

    // Hidden variables for logic tracking
    private var lastAnswer = "0" // Remembers the last answer for the 'ANS' button
    private val operators = listOf("+", "-", "x", "÷", "MOD", "^") // List of recognized math symbols
    private var isNewCalculation = false // Flag to know when to clear the screen for a new math problem

    // Helper: Checks if the very last character typed is a math operator
    private fun endsWithOperator(equation: String) = operators.any { equation.endsWith(it) }

    // --- SECTION 2: BUTTON CLICK CONTROLLER ---
    // Every time the user taps a button, this function figures out what to do.
    fun onButtonClick(btn: String) {

        // --- POWER BUTTON LOGIC ---
        // Turns screen on/off. If turning off, it wipes the memory completely.
        if (btn == "ON/OFF") {
            _isPoweredOn.value = !(_isPoweredOn.value ?: true)
            _equationText.value = ""
            _resultText.value = if (_isPoweredOn.value == true) "0" else ""
            isNewCalculation = false
            return
        }

        // If the calculator is turned off, ignore all other button clicks!
        if (_isPoweredOn.value == false) return

        val currentEq = _equationText.value ?: ""
        val lastChar = currentEq.lastOrNull()

        // 'when' acts like a traffic cop, routing the button press to the right logic
        when (btn) {
            "C" -> { // Clear Button: Wipes current math
                _equationText.value = ""
                _resultText.value = "0"
                isNewCalculation = false
            }
            "⌫" -> { // Backspace Button
                isNewCalculation = false
                if (currentEq.isNotEmpty()) {
                    // If the last thing is a word like "MOD", delete the whole word.
                    // Otherwise, just delete the last single character.
                    if (endsWithOperator(currentEq)) {
                        val op = operators.first { currentEq.endsWith(it) }
                        _equationText.value = currentEq.dropLast(op.length)
                    } else {
                        _equationText.value = currentEq.dropLast(1)
                    }
                    if (_equationText.value!!.isEmpty()) _resultText.value = "0" else evaluateRealTime(_equationText.value!!)
                }
            }
            "=" -> { // Equals Button: Finalizes the math
                if (currentEq.isEmpty()) return
                val finalResult = calculateResult(currentEq)

                // Error handling: Catch weird math like dividing by zero
                if (finalResult in listOf("Infinity", "-Infinity", "NaN", "Error")) {
                    _resultText.value = "Math Error"
                    isNewCalculation = true
                } else {
                    // Save successful math to the History popup
                    if (operators.any { currentEq.contains(it) } || currentEq.contains("%")) {
                        val historyEntry = "$currentEq = $finalResult"
                        _historyList.value = listOf(historyEntry) + (_historyList.value ?: emptyList())
                    }
                    _equationText.value = finalResult // Move answer up
                    _resultText.value = finalResult   // Show answer large
                    lastAnswer = finalResult          // Save for ANS button
                    isNewCalculation = true           // Lock state so next number clears screen
                }
            }
            "ANS" -> {
                isNewCalculation = false
                typeSmartly(currentEq, if (lastAnswer.startsWith("-")) "($lastAnswer)" else lastAnswer, lastChar)
            }
            "Xʸ", "^" -> {
                isNewCalculation = false
                if (currentEq.isNotEmpty() && !endsWithOperator(currentEq) && lastChar != '(') {
                    _equationText.value = currentEq + "^"
                }
            }
            "()" -> {
                isNewCalculation = false
                val open = currentEq.count { it == '(' }
                val close = currentEq.count { it == ')' }
                if (open > close && (lastChar?.isDigit() == true || lastChar == ')' || lastChar == '%' || currentEq.endsWith(lastAnswer))) {
                    if (lastChar == '(' || endsWithOperator(currentEq)) {
                        typeSmartly(currentEq, "(", lastChar)
                    } else {
                        _equationText.value = currentEq + ")"
                        evaluateRealTime(_equationText.value!!)
                    }
                } else {
                    typeSmartly(currentEq, "(", lastChar)
                }
            }
            "¹/ₓ" -> {
                isNewCalculation = false
                if (currentEq.isNotEmpty()) {
                    _equationText.value = "1÷($currentEq)"
                    evaluateRealTime(_equationText.value!!)
                }
            }
            "√" -> {
                isNewCalculation = false
                // Now it only types "√" on the screen, keeping the UI clean!
                typeSmartly(currentEq, "√", lastChar)
            }
            "(" -> {
                isNewCalculation = false
                typeSmartly(currentEq, "(", lastChar)
            }
            ")" -> {
                isNewCalculation = false
                val open = currentEq.count { it == '(' }
                val close = currentEq.count { it == ')' }
                // Only allow closing bracket if there is an unclosed open bracket
                if (open > close && lastChar != '(' && !endsWithOperator(currentEq)) {
                    _equationText.value = currentEq + btn
                    evaluateRealTime(_equationText.value!!)
                }
            }
            "%" -> {
                isNewCalculation = false
                if (currentEq.isNotEmpty() && (lastChar?.isDigit() == true || lastChar == ')')) {
                    _equationText.value = currentEq + "%"
                    evaluateRealTime(_equationText.value!!)
                }
            }
            in operators -> { // Logic for +, -, x, ÷, MOD, ^
                isNewCalculation = false
                if (currentEq.isEmpty() || lastChar == '(') {
                    if (btn == "-") _equationText.value = currentEq + btn // Allows negative numbers
                } else if (endsWithOperator(currentEq)) {
                    // If user typed '+' then '-', replace '+' with '-'
                    val op = operators.first { currentEq.endsWith(it) }
                    _equationText.value = currentEq.dropLast(op.length) + btn
                } else if (lastChar != '.') {
                    _equationText.value = currentEq + btn
                }
            }
            "." -> { // Decimal Logic
                if (isNewCalculation) {
                    _equationText.value = "0."
                    isNewCalculation = false
                    return
                }
                // Splits the equation to ensure the user doesn't type two decimals in one number (e.g., 5.5.5)
                val delimiters = arrayOf("+", "-", "x", "÷", "(", ")", "^", "MOD")
                val currentSegment = currentEq.split(*delimiters).last()
                if (!currentSegment.contains(".")) {
                    _equationText.value = if (currentEq.isEmpty() || endsWithOperator(currentEq) || lastChar == '(') currentEq + "0." else currentEq + btn
                }
            }
            else -> { // Regular Numbers (0-9)
                if (isNewCalculation) {
                    _equationText.value = btn // Starts a fresh screen if previous math was finished
                    isNewCalculation = false
                    evaluateRealTime(btn)
                } else {
                    typeSmartly(currentEq, btn, lastChar, isNumber = true)
                }
            }
        }
    }

    // --- SECTION 3: SMART AUTO-TYPING ---
    // If user types ')' and then '5', this smartly auto-inserts a multiplication sign: ')x5'
    private fun typeSmartly(currentEq: String, input: String, lastChar: Char?, isNumber: Boolean = false) {
        val needsMultiply = lastChar == ')' || lastChar == '%' || (!isNumber && lastChar?.isDigit() == true)
        val newEq = if (needsMultiply) currentEq + "x" + input else currentEq + input
        _equationText.value = newEq
        evaluateRealTime(newEq) // Update live answer immediately
    }

    // Runs math silently in the background before the user presses '='
    private fun evaluateRealTime(equation: String) {
        val result = calculateResult(equation)
        if (result !in listOf("NaN", "undefined", "Infinity", "Error", "")) _resultText.value = result
    }

    // --- SECTION 4: THE MATH ENGINE (JAVASCRIPT) ---
    private fun calculateResult(equation: String): String {
        if (equation.isEmpty() || equation == "-") return "0"

        try {
            // Using Mozilla Rhino: We open a Javascript Engine to solve complex string math
            val context = Context.enter()
            context.optimizationLevel = -1
            context.languageVersion = Context.VERSION_ES6 // Enables ES6 so it understands "^" as "**"
            val scriptable = context.initStandardObjects()

            var validEq = equation

            // 1. Translate visual UI symbols to Computer Code symbols
            // We translate "√" directly into "Math.sqrt(" here so the engine doesn't crash
            validEq = validEq.replace("÷", "/")
                .replace("x", "*")
                .replace("√", "Math.sqrt(")
                .replace("^", "**")

            // 2. Auto-close missing brackets so engine doesn't crash
            val missingBrackets = validEq.count { it == '(' } - validEq.count { it == ')' }
            if (missingBrackets > 0) {
                validEq += ")".repeat(missingBrackets)
            }

            // 3. Regex Magic: Fixes percentage math (Translates "100-20%" into standard code "100-(100*20/100)")
            validEq = Regex("""(\d+(?:\.\d+)?)([\+\-])(\d+(?:\.\d+)?)%""").replace(validEq) {
                "${it.groupValues[1]} ${it.groupValues[2]} (${it.groupValues[1]} * ${it.groupValues[3]} / 100)"
            }
            validEq = validEq.replace("%", "/100")

            // 4. FINALLY, we translate MOD so it doesn't get messed up by the percentage logic above
            validEq = validEq.replace("MOD", "%")

            // 5. Ask Javascript to solve it
            val rawResult = context.evaluateString(scriptable, validEq, "Javascript", 1, null).toString()

            // Clean up ".0" at the end of whole numbers
            return if (rawResult in listOf("undefined", "NaN")) "Error" else rawResult.removeSuffix(".0")
        } catch (_: Exception) {
            return "Error"
        } finally {
            Context.exit() // Close engine to save phone memory
        }
    }
}
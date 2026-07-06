package com.example.calculator_project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable

class CalculatorViewModel : ViewModel() {

    private val _equationText = MutableLiveData("")
    val equationText: LiveData<String> = _equationText

    private val _resultText = MutableLiveData("0")
    val resultText: LiveData<String> = _resultText

    private val _historyList = MutableLiveData<List<String>>(emptyList())
    val historyList: LiveData<List<String>> = _historyList

    private val operators = listOf("+", "-", "x", "÷")
    private var isNewCalculation = false

    fun onButtonClick(btn: String) {
        Log.i("Clicked Button", btn)

        val currentEquation = _equationText.value ?: ""
        val lastChar = currentEquation.lastOrNull()

        when (btn) {
            "C" -> {
                _equationText.value = ""
                _resultText.value = "0"
                isNewCalculation = false
            }
            "⌫" -> {
                isNewCalculation = false
                if (currentEquation.isNotEmpty()) {
                    val newEquation = currentEquation.dropLast(1)
                    _equationText.value = newEquation

                    if (newEquation.isEmpty()) {
                        _resultText.value = "0"
                    } else {
                        evaluateRealTime(newEquation)
                    }
                }
            }
            "=" -> {
                if (currentEquation.isEmpty()) return
                try {
                    val finalResult = calculateResult(currentEquation)
                    if (finalResult == "Infinity" || finalResult == "-Infinity" || finalResult == "NaN" || finalResult == "Error") {
                        _resultText.value = "Math Error"
                    } else {
                        val openParens = currentEquation.count { it == '(' }
                        val closedParens = currentEquation.count { it == ')' }
                        var balancedEquation = currentEquation
                        if (openParens > closedParens) {
                            balancedEquation += ")".repeat(openParens - closedParens)
                        }

                        val hasOperator = balancedEquation.any { it == '+' || it == '-' || it == 'x' || it == '÷' || it == '%' }

                        if (hasOperator && balancedEquation != finalResult) {
                            val historyEntry = "$balancedEquation = $finalResult"
                            val currentHistory = _historyList.value ?: emptyList()
                            _historyList.value = listOf(historyEntry) + currentHistory
                        }

                        _equationText.value = finalResult
                        _resultText.value = finalResult
                        isNewCalculation = true
                    }
                } catch (_: Exception) {
                    _resultText.value = "Math Error"
                }
            }
            "%" -> {
                isNewCalculation = false
                if (currentEquation.isNotEmpty() && (lastChar?.isDigit() == true || lastChar == ')')) {
                    val newEquation = currentEquation + "%"
                    _equationText.value = newEquation
                    evaluateRealTime(newEquation)
                }
            }
            "()" -> {
                isNewCalculation = false
                val openParens = currentEquation.count { it == '(' }
                val closedParens = currentEquation.count { it == ')' }

                val shouldClose = openParens > closedParens && lastChar != '(' && lastChar?.toString() !in operators && lastChar != '.'

                if (shouldClose) {
                    val newEquation = currentEquation + ")"
                    _equationText.value = newEquation
                    evaluateRealTime(newEquation)
                } else {
                    if (lastChar == '(') return

                    if (currentEquation.isNotEmpty() && (lastChar?.isDigit() == true || lastChar == '.' || lastChar == ')' || lastChar == '%')) {
                        val newEquation = currentEquation + "x("
                        _equationText.value = newEquation
                        evaluateRealTime(newEquation)
                    } else {
                        _equationText.value = currentEquation + "("
                    }
                }
            }
            "." -> {
                if (isNewCalculation) {
                    _equationText.value = "0."
                    isNewCalculation = false
                    return
                }
                val currentNumberSegment = currentEquation.split('+', '-', 'x', '÷', '(', ')').last()

                if (!currentNumberSegment.contains(".")) {
                    if (currentEquation.isEmpty() || lastChar?.toString() in operators || lastChar == '(') {
                        _equationText.value = currentEquation + "0."
                    } else {
                        _equationText.value = currentEquation + btn
                    }
                }
            }
            in operators -> {
                isNewCalculation = false
                if (currentEquation.isEmpty() || lastChar == '(') {
                    if (btn == "-") _equationText.value = currentEquation + btn
                } else if (lastChar?.toString() in operators) {
                    val newEquation = currentEquation.dropLast(1) + btn
                    _equationText.value = newEquation
                } else if (lastChar != '.') {
                    val newEquation = currentEquation + btn
                    _equationText.value = newEquation
                }
            }
            else -> {
                if (isNewCalculation) {
                    _equationText.value = btn
                    isNewCalculation = false
                    evaluateRealTime(btn)
                } else {
                    if (lastChar == ')' || lastChar == '%') {
                        val newEquation = currentEquation + "x" + btn
                        _equationText.value = newEquation
                        evaluateRealTime(newEquation)
                    } else {
                        val newEquation = currentEquation + btn
                        _equationText.value = newEquation
                        evaluateRealTime(newEquation)
                    }
                }
            }
        }
    }

    private fun evaluateRealTime(equation: String) {
        try {
            val result = calculateResult(equation)
            if (result != "NaN" && result != "undefined" && result != "Infinity" && result != "Error" && result.isNotEmpty()) {
                _resultText.value = result
            }
        } catch (_: Exception) { }
    }

    private fun calculateResult(equation: String): String {
        if (equation.isEmpty() || equation == "-") return "0"

        try {
            val context: Context = Context.enter()
            context.optimizationLevel = -1
            val scriptable: Scriptable = context.initStandardObjects()

            val openParens = equation.count { it == '(' }
            val closedParens = equation.count { it == ')' }
            var balancedEquation = equation
            if (openParens > closedParens) {
                balancedEquation += ")".repeat(openParens - closedParens)
            }

            var validEquation = balancedEquation
                .replace("÷", "/")
                .replace("x", "*")

            // Smart Percentage Logic for Addition and Subtraction
            val percentRegex = Regex("""(\d+(?:\.\d+)?)([\+\-])(\d+(?:\.\d+)?)%""")
            validEquation = percentRegex.replace(validEquation) { matchResult ->
                val num1 = matchResult.groupValues[1]
                val operator = matchResult.groupValues[2]
                val num2 = matchResult.groupValues[3]
                "$num1 $operator ($num1 * $num2 / 100)"
            }

            // Replace remaining percentages
            validEquation = validEquation.replace("%", "/100")

            val rawResult = context.evaluateString(scriptable, validEquation, "Javascript", 1, null).toString()

            if (rawResult == "undefined" || rawResult == "NaN") return "Error"

            var finalResult = rawResult
            if (finalResult.endsWith(".0")) {
                finalResult = finalResult.replace(".0", "")
            }
            return finalResult
        } catch (_: Exception) {
            return "Error"
        } finally {
            Context.exit()
        }
    }
}
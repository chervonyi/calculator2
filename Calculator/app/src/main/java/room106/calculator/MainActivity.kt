package room106.calculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var expressionTextView: TextView
    private lateinit var resultTextView: TextView
    private var memorySlots = ArrayList<CalculatorButton>()




    // Calculator Engine
    private val calculator = Calculator()

    private val decimalFormat = DecimalFormat("#.###")

    private var memory = ArrayList<Double>().apply {
        add(0.0)
        add(0.0)
        add(0.0)
    }

    private val MAX_LENGTH = 30
    private val MEMORY_SLOTS_COUNT = memory.size

    private val MULTIPLY_CHARACTER = "\u00D7"
    private val DIVISION_CHARACTER = "\u00F7"
    private val MINUS_CHARACTER = "\u002D"
    private val PLUS_CHARACTER = "\u002B"
    private val DECIMAL_POINT = "."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expressionTextView = findViewById(R.id.expressionTextView)
        resultTextView = findViewById(R.id.resultTextView)

        expressionTextView.text = ""
        resultTextView.text = "0"

        // Connect memory buttons
        memorySlots.add(findViewById(R.id.buttonMemory0))
        memorySlots.add(findViewById(R.id.buttonMemory1))
        memorySlots.add(findViewById(R.id.buttonMemory2))

        // Assign Long Click Listener to memory buttons
        for (memoryButton in memorySlots) {
            memoryButton.setOnLongClickListener(onLongClickOnMemorySlotButton)
        }
    }


    //region Listeners
    fun onClickDigit(v: View) {
        appendDigit((v as CalculatorButton).getText())
    }

    fun onClickOperationButton(v: View) {
        when (v.id) {
            R.id.buttonOpDivision   -> appendDigit(DIVISION_CHARACTER)
            R.id.buttonOpMultiply   -> appendDigit(MULTIPLY_CHARACTER)
            R.id.buttonOpSubtract   -> appendDigit(MINUS_CHARACTER)
            R.id.buttonOpAdd        -> appendDigit(PLUS_CHARACTER)
            R.id.buttonDecimalPoint -> appendDigit(DECIMAL_POINT)
        }
    }

    fun onClickMemoryButton(v: View) {
        val position = resources.getResourceName(v.id).last().toString().toInt()

        if (isMemorySlotEmpty(position)) {
            var value = 0.0
            try {
                value = calculator.calculate(expressionTextView.text.toString())

                expressionTextView.text = ""
                resultTextView.text = ""
            } catch (e: CalculatorSyntaxException) {
                expressionTextView.text = ""
                resultTextView.text = ""
            }

            saveNumberAt(value, position)
        } else {
            useNumberFrom(position)
        }
    }

    private val onLongClickOnMemorySlotButton = View.OnLongClickListener {
        val position = resources.getResourceName(it.id).last().toString().toInt()

        var value = 0.0
        try {
            value = calculator.calculate(expressionTextView.text.toString())

            expressionTextView.text = ""
            resultTextView.text = ""
        } catch (e: CalculatorSyntaxException) {
            expressionTextView.text = ""
            resultTextView.text = ""
        }

        saveNumberAt(value, position)
        true
    }

    fun onClickEraseButton(v: View) {
        eraseLastDigit()
    }

    fun onClickCalculationButton(v: View) {
        try {
            val res = calculator.calculate(expressionTextView.text.toString())

            expressionTextView.text = decimalFormat.format(res)
            resultTextView.text = ""
        } catch (e: CalculatorSyntaxException) {
            expressionTextView.text = ""
            resultTextView.text = ""
        }
    }
    //endregion


    //region Memory Methods

    private fun isMemorySlotEmpty(position: Int): Boolean {
        return memory[position] == 0.0
    }

    private fun useNumberFrom(position: Int) {
        appendDigit(decimalFormat.format(memory[position]))
    }

    private fun saveNumberAt(value: Double, position: Int) {
        if (position in 0 until MEMORY_SLOTS_COUNT && value != 0.0) {
            memory[position] = value
            memorySlots[position].setMemory(value)
        }
    }

    fun resetMemoryNumberAt(position: Int) {
        memory[position] = 0.0
        memorySlots[position].setMemory(0.0)
    }


    //endregion

    //region Supporting Methods

    private fun preCalculation() {

        try {
            val res = calculator.calculate(expressionTextView.text.toString())
            resultTextView.text = decimalFormat.format(res)
        } catch (e: CalculatorSyntaxException) { }
    }


    private fun appendDigit(c1: CharSequence) {

        var str = c1.toString()
        var expression = expressionTextView.text.toString()

        // Check on max length
        if (expression.length >= MAX_LENGTH) {
            return
        }

        // Check on empty string of inputView
        if (expression.isEmpty()) {
            // One of these character cannot be the first one in the input string
            if ("$PLUS_CHARACTER$MINUS_CHARACTER$DIVISION_CHARACTER$MULTIPLY_CHARACTER)".contains(str)) {
                return
            }
        }

        // Check if pointer does not make error
        if (str == ".") {
            // Put '0' before point to make input string like: "0."
            if (isLastEquals(expression, "$PLUS_CHARACTER$MINUS_CHARACTER$DIVISION_CHARACTER$MULTIPLY_CHARACTER") || expression.isEmpty()) {
                expression += "0"
            }

            else if (!isLastNumberWell() || isLastEquals(expression, ")")) {
                return
            }
        }

        // Check on main operation symbols
        if ("$PLUS_CHARACTER$MINUS_CHARACTER$DIVISION_CHARACTER$MULTIPLY_CHARACTER".contains(str)) {
            if (isLastEquals(expression, "$PLUS_CHARACTER$MINUS_CHARACTER$DIVISION_CHARACTER$MULTIPLY_CHARACTER")) {
                expression = expression.dropLast(1)
            } else if (isLastEquals(expression, ".")) {
                return
            }
        }

        // Check on "0" symbol
        if (str == "0") {
            if (expression.isEmpty() || isLastEquals(expression, "$PLUS_CHARACTER$MINUS_CHARACTER$DIVISION_CHARACTER$MULTIPLY_CHARACTER")) {
                str += "."
            }
        }
        expressionTextView.text = expression + str
        preCalculation()
    }

    private fun count(stringToCheck: String, symbol: String): Int {
        return stringToCheck.length -
                stringToCheck.replace(String.format("\\%s", symbol).toRegex(), "").length
    }

    private fun isLastNumberWell(): Boolean {
        return count(getLastNumber(expressionTextView.text.toString()), ".") < 1
    }

    /**
     * Returns the whole number (Including delimiter) like: "242.2", "1984", "0.001"
     * @param input string to search for number
     * @return string with necessary number
     */
    private fun getLastNumber(input: String): String {
        var input = input
        var num = ""
        while (isLastEquals(input, "0123456789.")) {
            num = input[input.length - 1].toString() + num
            input = input.substring(0, input.length - 1)
        }
        return num
    }

    private fun isLastEquals(
        stringToCheck: String,
        set: String
    ): Boolean {
        return if (stringToCheck.isEmpty()) {
            false
        } else set.contains(stringToCheck[stringToCheck.length - 1].toString())
    }

    private fun eraseLastDigit() {
        expressionTextView.text = expressionTextView.text.dropLast(1)
        preCalculation()
    }
    //endregion
}

package room106.calculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    // Header
    private lateinit var expressionTextView: TextView
    private lateinit var resultTextView: TextView

    // Memory Slot Buttons
    private lateinit var buttonMemory1 : CalculatorButton
    private lateinit var buttonMemory2 : CalculatorButton
    private lateinit var buttonMemory3 : CalculatorButton

    // Digits
    private val numsId = arrayOf(
        R.id.buttonNum0,
        R.id.buttonNum1,
        R.id.buttonNum2,
        R.id.buttonNum3,
        R.id.buttonNum4,
        R.id.buttonNum5,
        R.id.buttonNum6,
        R.id.buttonNum7,
        R.id.buttonNum8,
        R.id.buttonNum9
    )
    private lateinit var buttonNums: ArrayList<CalculatorButton>

    // Operations
    private lateinit var buttonOpDivision : CalculatorButton
    private lateinit var buttonOpMultiply : CalculatorButton
    private lateinit var buttonOpSubtract : CalculatorButton
    private lateinit var buttonOpAdd : CalculatorButton

    // Other
    private lateinit var buttonErase : CalculatorButton
    private lateinit var buttonCalculation : CalculatorButton
    private lateinit var buttonDecimalPoint : CalculatorButton

    // Calculator Engine
    private val calculator = Calculator()

    private val decimalFormat = DecimalFormat("#.###")

    private val MAX_LENGTH = 30

    private val MULTIPLY_CHARACTER = "\u00D7"
    private val DIVISION_CHARACTER = "\u00F7"
    private val MINUS_CHARACTER = "\u002D"
    private val PLUS_CHARACTER = "\u002B"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expressionTextView = findViewById(R.id.expressionTextView)
        resultTextView = findViewById(R.id.resultTextView)

        expressionTextView.text = ""
        resultTextView.text = "0"


        buttonMemory1 = findViewById(R.id.buttonMemory1)
        buttonMemory2 = findViewById(R.id.buttonMemory2)
        buttonMemory3 = findViewById(R.id.buttonMemory3)

        buttonNums = ArrayList()
        for (id in numsId) {
            buttonNums.add(findViewById<CalculatorButton>(id).apply {
                // Instantly connect appropriate listener
                setOnClickListener(numsOnClickListener)
            })
        }

        buttonOpDivision = findViewById(R.id.buttonOpDivision)
        buttonOpMultiply = findViewById(R.id.buttonOpMultiply)
        buttonOpSubtract = findViewById(R.id.buttonOpSubtract)
        buttonOpAdd = findViewById(R.id.buttonOpAdd)

        buttonErase = findViewById(R.id.buttonErase)
        buttonCalculation = findViewById(R.id.buttonCalculation)
        buttonDecimalPoint = findViewById(R.id.buttonDecimalPoint)


        // Connect listeners
        buttonMemory1.setOnClickListener(memoryOnClickListener)
        buttonMemory2.setOnClickListener(memoryOnClickListener)
        buttonMemory3.setOnClickListener(memoryOnClickListener)

        buttonOpDivision.setOnClickListener(operatorsOnClickListener)
        buttonOpMultiply.setOnClickListener(operatorsOnClickListener)
        buttonOpSubtract.setOnClickListener(operatorsOnClickListener)
        buttonOpAdd.setOnClickListener(operatorsOnClickListener)

        buttonErase.setOnClickListener(eraseOnClickListener)
        buttonCalculation.setOnClickListener(calculationOnClickListener)
        buttonDecimalPoint.setOnClickListener(decimalPointOnClickListener)
    }


    //region Listeners
    private val numsOnClickListener = View.OnClickListener {
        appendDigit((it as CalculatorButton).getText())
    }

    private val operatorsOnClickListener = View.OnClickListener {
        when (it.id) {
            R.id.buttonOpDivision -> appendDigit(DIVISION_CHARACTER)
            R.id.buttonOpMultiply -> appendDigit(MULTIPLY_CHARACTER)
            R.id.buttonOpSubtract -> appendDigit(MINUS_CHARACTER)
            R.id.buttonOpAdd -> appendDigit(PLUS_CHARACTER)
        }
    }

    private val memoryOnClickListener = View.OnClickListener {
        Log.d("TEST", "Clicked on memory button!")
    }

    private val eraseOnClickListener = View.OnClickListener {
        eraseLastDigit()
    }

    private val calculationOnClickListener = View.OnClickListener {
        try {
            val res = calculator.calculate(expressionTextView.text.toString())

            expressionTextView.text = decimalFormat.format(res)
            resultTextView.text = ""
        } catch (e: CalculatorSyntaxException) {
            expressionTextView.text = ""
            resultTextView.text = ""
        }
    }

    private val decimalPointOnClickListener = View.OnClickListener {
        appendDigit(".")
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
        Log.d("TEST", "Expr: $expression")

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

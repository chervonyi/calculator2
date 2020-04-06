package room106.calculator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var supportingTextView: TextView
    private lateinit var mainTextView: TextView
    private var memorySlots = ArrayList<CalculatorButton>()

    private lateinit var additionOperatorButton: CalculatorButton
    private lateinit var subtractionOperatorButton: CalculatorButton
    private lateinit var multiplicationOperatorButton: CalculatorButton
    private lateinit var divisionOperatorButton: CalculatorButton


    // Calculator Engine
    private var num1: Double? = null
    private var operator: Operator? = null
    private var numberIsSaved = false

    enum class Operator {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION
    }

    private val decimalFormat = DecimalFormat("#.###")

    private var memory = ArrayList<Double>().apply {
        add(0.0)
        add(0.0)
        add(0.0)
    }

    private val MAX_LENGTH = 30
    private val MEMORY_SLOTS_COUNT = memory.size
    private val DECIMAL_POINT = "."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportingTextView = findViewById(R.id.supportingTextView)
        mainTextView = findViewById(R.id.mainTextView)
        additionOperatorButton = findViewById(R.id.buttonOpAdd)
        subtractionOperatorButton = findViewById(R.id.buttonOpSubtract)
        multiplicationOperatorButton = findViewById(R.id.buttonOpMultiply)
        divisionOperatorButton = findViewById(R.id.buttonOpDivision)

        supportingTextView.visibility = View.INVISIBLE
        mainTextView.text = "0"

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
        var expression = mainTextView.text.toString()
        val typedDigit = (v as CalculatorButton).getText()

        if (numberIsSaved) {
            numberIsSaved = false
            expression = ""
        }

        if (expression == "0") {
            mainTextView.text = typedDigit
        } else {
            mainTextView.text = expression + typedDigit
        }

        highlightOperator(null)
    }

    fun onClickOperationButton(v: View) {
        val typedNumber = mainTextView.text.toString().toDouble()
        val selectedOperator = getOperatorType(v.id)

        if (num1 == null) {
            num1 = typedNumber
            operator = selectedOperator
            highlightOperator(operator)
        } else {
            val result = calculate(num1!!, typedNumber, operator)
            num1 = result
            displayResult(result)
            operator = selectedOperator
            highlightOperator(operator)
        }
        numberIsSaved = true
    }

    fun onClickCalculationButton(v: View) {
        val typedNumber = mainTextView.text.toString().toDouble()

        if (num1 == null && operator == null) {
            return
        } else {
            Log.d("TEST", "num1: $num1, typedNumber: $typedNumber, operator: $operator")
            val result = calculate(num1!!, typedNumber, operator)
            num1 = null
            operator = null
            displayResult(result)
            numberIsSaved = true
        }
    }

    fun onClickMemoryButton(v: View) {
        val position = resources.getResourceName(v.id).last().toString().toInt()

        if(isMemorySlotEmpty(position)) {

            val typedNumber = mainTextView.text.toString().toDouble()

            if (num1 == null && operator == null) {
                saveNumberAt(typedNumber, position)
            } else {
                val result = calculate(num1!!, typedNumber, operator)
                num1 = null
                operator = null
                displayResult(result)
                numberIsSaved = true
                saveNumberAt(result, position)
            }

            mainTextView.text = "0"
        } else {
            useNumberFrom(position)
        }
    }

    private val onLongClickOnMemorySlotButton = View.OnLongClickListener {
        val position = resources.getResourceName(it.id).last().toString().toInt()

        val typedNumber = mainTextView.text.toString().toDouble()

        if (num1 == null && operator == null) {
            saveNumberAt(typedNumber, position)
        } else {
            val result = calculate(num1!!, typedNumber, operator)
            num1 = null
            operator = null
            displayResult(result)
            numberIsSaved = true
            saveNumberAt(result, position)
        }

        mainTextView.text = "0"

        true
    }

    fun onClickEraseButton(v: View) {
        mainTextView.text = "0"

        if (operator != null) {
            highlightOperator(operator)
        }
    }

    fun onClickDecimalPoint(v: View) {

        var expression = mainTextView.text.toString()

        if (!expression.contains(DECIMAL_POINT)) {
            when {
                expression.isEmpty() -> {
                    expression = "0$DECIMAL_POINT"
                }
                numberIsSaved -> {
                    numberIsSaved = false
                    expression = "0$DECIMAL_POINT"
                }
                else -> {
                    expression += DECIMAL_POINT
                }
            }
        }

        mainTextView.text = expression
    }
    //endregion


    //region Memory Methods
    private fun isMemorySlotEmpty(position: Int): Boolean {
        return memory[position] == 0.0
    }

    private fun useNumberFrom(position: Int) {
        mainTextView.text = decimalFormat.format(memory[position])
        highlightOperator(null)

        // Test:?
        numberIsSaved = true
    }

    private fun saveNumberAt(value: Double, position: Int) {
        if (position in 0 until MEMORY_SLOTS_COUNT) {
            memory[position] = value
            memorySlots[position].setMemory(value)
        }
    }
    //endregion


    //region Main methods
    private fun calculate(num1: Double, num2: Double, operator: Operator?): Double {
        return when (operator) {
            Operator.ADDITION -> num1 + num2
            Operator.SUBTRACTION -> num1 - num2
            Operator.MULTIPLICATION -> num1 * num2
            Operator.DIVISION -> num1 / num2
           else -> 0.0
        }
    }

    private fun getOperatorType(id: Int): Operator? {
        return when(id) {
            R.id.buttonOpAdd -> Operator.ADDITION
            R.id.buttonOpSubtract -> Operator.SUBTRACTION
            R.id.buttonOpMultiply -> Operator.MULTIPLICATION
            R.id.buttonOpDivision -> Operator.DIVISION
            else -> null
        }
    }

    private fun highlightOperator(op: Operator?) {
        additionOperatorButton.setActivationOperatorButton(op == Operator.ADDITION)
        subtractionOperatorButton.setActivationOperatorButton(op == Operator.SUBTRACTION)
        multiplicationOperatorButton.setActivationOperatorButton(op == Operator.MULTIPLICATION)
        divisionOperatorButton.setActivationOperatorButton(op == Operator.DIVISION)
    }

    private fun displayResult(result: Double) {
        mainTextView.text = decimalFormat.format(result)
    }
    //endregion
}

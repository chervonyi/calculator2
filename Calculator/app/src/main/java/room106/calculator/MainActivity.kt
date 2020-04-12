package room106.calculator

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var buttonChangeTheme: ImageButton
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
    private var numberIsReady = false
    private var operatorMayBeChanged = false

    private lateinit var buttonSound: MediaPlayer

    enum class Operator(val value: Int) {
        ADDITION(0),
        SUBTRACTION(1),
        MULTIPLICATION(2),
        DIVISION(3);

        companion object {
            private val values = Operator.values()
            fun getByValue(value: Int) = values.firstOrNull { it.value == value }
        }
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

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.LightTheme)
        }
        setContentView(R.layout.activity_main)

        buttonChangeTheme = findViewById(R.id.buttonChangeTheme)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            buttonChangeTheme.setImageResource(R.drawable.ic_sun)
        } else {
            buttonChangeTheme.setImageResource(R.drawable.ic_moon_transparent)
        }

        buttonSound = MediaPlayer.create(this, R.raw.button_click_sound)
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

        // Load data (if saved before)
        if (intent.hasExtra("memory0")) {
            memory[0] =  intent.extras!!.getDouble("memory0")
            memory[1] =  intent.extras!!.getDouble("memory1")
            memory[2] =  intent.extras!!.getDouble("memory2")

            memorySlots[0].setMemory(memory[0])
            memorySlots[1].setMemory(memory[1])
            memorySlots[2].setMemory(memory[2])
        }

        // Assign Long Click Listener to memory buttons
        for (memoryButton in memorySlots) {
            memoryButton.setOnLongClickListener(onLongClickOnMemorySlotButton)
        }
    }


    //region Listeners
    fun onClickDigit(v: View) {
        buttonSound.start()
        var expression = mainTextView.text.toString()
        val typedDigit = (v as CalculatorButton).getText()

        if (numberIsReady) {
            numberIsReady = false
            expression = ""
        }

        if (expression == "0") {
            mainTextView.text = typedDigit
        } else {
            mainTextView.text = expression + typedDigit
        }

        highlightOperator(null)
        operatorMayBeChanged = false
    }

    fun onClickOperationButton(v: View) {
        buttonSound.start()
        val typedNumber = mainTextView.text.toString().toDouble()
        val selectedOperator = getOperatorType(v.id)

        if (num1 == null) {
            num1 = typedNumber
        } else if (operator != null && !operatorMayBeChanged) {
            val result = calculate(num1!!, typedNumber, operator)
            num1 = result
            displayResult(result)
        }

        operator = selectedOperator
        highlightOperator(operator)
        numberIsReady = true
        operatorMayBeChanged = true

    }

    fun onClickCalculationButton(v: View) {
        buttonSound.start()
        val typedNumber = mainTextView.text.toString().toDouble()

        if (num1 != null && operator != null) {
            Log.d("TEST", "num1: $num1, typedNumber: $typedNumber, operator: $operator")
            val result = calculate(num1!!, typedNumber, operator)
            operator = null
            num1 = null
            displayResult(result)
            numberIsReady = true
        }
    }

    fun onClickMemoryButton(v: View) {
        buttonSound.start()
        val position = resources.getResourceName(v.id).last().toString().toInt()

        if(isMemorySlotEmpty(position)) {

            val typedNumber = mainTextView.text.toString().toDouble()

            if (num1 != null && operator != null) {
                // Calculate new number and save it
                val result = calculate(num1!!, typedNumber, operator)
                displayResult(result)
                saveNumberAt(result, position)
            } else {
                // Save number that typed
                saveNumberAt(typedNumber, position)
            }

            num1 = null
            operator = null
            highlightOperator(null)
            mainTextView.text = "0"
        } else {
            useNumberFrom(position)
        }
        operatorMayBeChanged = false
    }

    private val onLongClickOnMemorySlotButton = View.OnLongClickListener {
        buttonSound.start()
        val position = resources.getResourceName(it.id).last().toString().toInt()

        val typedNumber = mainTextView.text.toString().toDouble()

        if (num1 != null && operator != null) {
            val result = calculate(num1!!, typedNumber, operator)
            num1 = null
            operator = null
            displayResult(result)
            numberIsReady = true
            saveNumberAt(result, position)
        } else {
            saveNumberAt(typedNumber, position)
        }

        mainTextView.text = "0"

        true
    }

    fun onClickEraseButton(v: View) {
        buttonSound.start()
        if (mainTextView.text.isEmpty() || mainTextView.text == "0") {
            // Total clear (saved num1 and saved operation)
            operator = null
            highlightOperator(null)
            num1 = null
            return
        }

        if (operator != null) {
            highlightOperator(operator)
        }

        mainTextView.text = "0"
    }

    fun onClickDecimalPoint(v: View) {
        buttonSound.start()
        var expression = mainTextView.text.toString()

        if (numberIsReady) {
            expression = "0$DECIMAL_POINT"
            numberIsReady = false
        } else if (!expression.contains(DECIMAL_POINT)) {
            if (expression.isEmpty()) {
                expression = "0$DECIMAL_POINT"
            } else {
                expression += DECIMAL_POINT
            }
        }

        mainTextView.text = expression
        operatorMayBeChanged = false
    }

    fun onClickChangeTheme(v: View) {
        buttonSound.start()

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("memory0",  memory[0])
        intent.putExtra("memory1",  memory[1])
        intent.putExtra("memory2",  memory[2])
        startActivity(intent)
        finish()
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
        numberIsReady = true
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
            Operator.DIVISION -> {
                if (num2 != 0.0) {
                    num1 / num2
                } else {
                    0.0
                }
            }
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

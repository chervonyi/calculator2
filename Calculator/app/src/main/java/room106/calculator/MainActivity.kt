package room106.calculator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var buttonChangeTheme: ImageButton
    private lateinit var mainTextView: TextView
    private lateinit var infoPanelView: InfoPanelView
    private var memorySlots = ArrayList<CalculatorButton>()

    private lateinit var additionOperatorButton: CalculatorButton
    private lateinit var subtractionOperatorButton: CalculatorButton
    private lateinit var multiplicationOperatorButton: CalculatorButton
    private lateinit var divisionOperatorButton: CalculatorButton


    // Calculator Engine
    private var num1: Double? = null
    private var operator: Operator? = null
    private var operatorForCalculationInRow: Operator? = null
    private var valueForCalculationInRow: Double? = null
    private var numberIsReady = false
    private var operatorMayBeChanged = false
    private var calculationInRow = false

    private lateinit var buttonSound: MediaPlayer
    private lateinit var audioSystem: AudioManager
    private lateinit var mPreferredThemeManager: PreferredThemeManager
    private lateinit var mInfoManager: InfoManager

    private val MAX_DIGITS = 8

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

    private val MEMORY_SLOTS_COUNT = memory.size
    private val DECIMAL_POINT = "."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up theme
        mPreferredThemeManager = PreferredThemeManager(this)
        if (mPreferredThemeManager.getPreferredTheme() == PreferredThemeManager.Mode.DarkTheme) {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.LightTheme)
        }
        setContentView(R.layout.activity_main)

        // Set up appropriate icon (sun or moon) according to Preferred Theme
        buttonChangeTheme = findViewById(R.id.buttonChangeTheme)
        if (mPreferredThemeManager.getPreferredTheme() == PreferredThemeManager.Mode.DarkTheme) {
            buttonChangeTheme.setImageResource(R.drawable.ic_sun)
        } else {
            buttonChangeTheme.setImageResource(R.drawable.ic_moon_transparent)
        }

        // Create button click sound effect
        buttonSound = MediaPlayer.create(this, R.raw.button_click_sound)
        audioSystem = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Connect all views
        mainTextView = findViewById(R.id.mainTextView)
        additionOperatorButton = findViewById(R.id.buttonOpAdd)
        subtractionOperatorButton = findViewById(R.id.buttonOpSubtract)
        multiplicationOperatorButton = findViewById(R.id.buttonOpMultiply)
        divisionOperatorButton = findViewById(R.id.buttonOpDivision)
        infoPanelView = findViewById(R.id.infoPanelView)

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

        mInfoManager = InfoManager(this)
        checkTutorialRequest()
    }


    //region Listeners
    fun onClickDigit(v: View) {
        playButtonClickSound()
        var expression = mainTextView.text.toString()
        val typedDigit = (v as CalculatorButton).getText()

        if (numberIsReady) {
            numberIsReady = false
            expression = ""
        }

        if (getDigitCount(expression) < MAX_DIGITS) {
            if (expression == "0") {
                mainTextView.text = typedDigit
            } else {
                mainTextView.text = expression + typedDigit
            }
        }

        highlightOperator(null)
        operatorMayBeChanged = false
        calculationInRow = false
    }

    fun onClickOperationButton(v: View) {
        playButtonClickSound()
        val typedNumber = mainTextView.text.toString().toDouble()
        val selectedOperator = getOperatorType(v.id)

        if (num1 == null) {
            num1 = typedNumber
        } else if (operator != null && !operatorMayBeChanged) {
            var result = calculate(num1!!, typedNumber, operator)

            // In case of negative 0 make it as positive
            if (result == 0.0) {
                result = 0.0
            }

            num1 = result
            displayResult(result)
        }

        operator = selectedOperator
        highlightOperator(operator)
        numberIsReady = true
        operatorMayBeChanged = true
        calculationInRow = false
    }

    fun onClickCalculationButton(v: View) {
        playButtonClickSound()
        val typedNumber = mainTextView.text.toString().toDouble()

        if (calculationInRow) {
            var result = calculate(typedNumber, valueForCalculationInRow!!, operatorForCalculationInRow)

            if (result == 0.0) {
                result = 0.0
            }

            displayResult(result)
        }
        else if (num1 != null && operator != null && !operatorMayBeChanged) {
//            Log.d("TEST", "num1: $num1, typedNumber: $typedNumber, operator: $operator")
            var result = calculate(num1!!, typedNumber, operator)

            // In case of negative 0 make it as positive
            if (result == 0.0) {
                result = 0.0
            }

            valueForCalculationInRow = typedNumber
            operatorForCalculationInRow = operator
            calculationInRow = true

            operator = null
            num1 = null
            displayResult(result)
            numberIsReady = true
        }
    }

    fun onClickMemoryButton(v: View) {
        playButtonClickSound()
        val position = resources.getResourceName(v.id).last().toString().toInt()

        if(isMemorySlotEmpty(position)) {
            val typedNumber = mainTextView.text.toString().toDouble()

            if (num1 != null && operator != null && !operatorMayBeChanged) {
                // Calculate new number and save it
                val result = calculate(num1!!, typedNumber, operator)
                displayResult(result)
                saveNumberAt(result, position)
            } else {
                // Save number that typed
                saveNumberAt(typedNumber, position)
            }

            // Check on Tutorial #1
            if (infoPanelView.visibility == View.VISIBLE && !mInfoManager.hasInfo1ShownBefore && typedNumber != 0.0) {
                mInfoManager.hasInfo1ShownBefore = true
                hideInfoPanel()
            }

            num1 = null
            operator = null
            highlightOperator(null)
            mainTextView.text = "0"
        } else {
            useNumberFrom(position)
        }
        operatorMayBeChanged = false
        calculationInRow = false
    }

    private val onLongClickOnMemorySlotButton = View.OnLongClickListener {
        playButtonClickSound()
        val position = resources.getResourceName(it.id).last().toString().toInt()

        val typedNumber = mainTextView.text.toString().toDouble()

        if (num1 != null && operator != null && !operatorMayBeChanged) {
            val result = calculate(num1!!, typedNumber, operator)
            displayResult(result)
//            numberIsReady = true
            saveNumberAt(result, position)
        } else {
            saveNumberAt(typedNumber, position)
        }

        // Check on Tutorial #2
        if (infoPanelView.visibility == View.VISIBLE && !mInfoManager.hasInfo2ShownBefore && typedNumber != 0.0 && mInfoManager.hasInfo1ShownBefore) {
            mInfoManager.hasInfo2ShownBefore = true
            hideInfoPanel()
        }

        num1 = null
        operator = null
        highlightOperator(null)
        mainTextView.text = "0"
        operatorMayBeChanged = false
        calculationInRow = false

        true
    }

    fun onClickEraseButton(v: View) {
        playButtonClickSound()
        if (mainTextView.text.isEmpty() || mainTextView.text == "0") {
            // Total clear (saved num1 and saved operation)
            operator = null
            highlightOperator(null)
            num1 = null
            return
        }

        if (operator != null) {
            operatorMayBeChanged = true // ???
            highlightOperator(operator)
        }

        mainTextView.text = "0"

        calculationInRow = false
    }

    fun onClickDecimalPoint(v: View) {
        playButtonClickSound()
        var expression = mainTextView.text.toString()

        if (getDigitCount(expression) < MAX_DIGITS) {
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
        }

        mainTextView.text = expression
        operatorMayBeChanged = false
        calculationInRow = false
    }

    fun onClickChangeTheme(v: View) {
        playButtonClickSound()

        mPreferredThemeManager.changePreferredTheme()

        // Prepare Intent instant to migrate with memory-slot values
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

    private fun checkTutorialRequest() {
        if (!mInfoManager.hasInfo1ShownBefore) {
            showInfoPanel(resources.getString(R.string.info_1))
        } else {
            if (!mInfoManager.hasInfo2ShownBefore) {
                showInfoPanel(resources.getString(R.string.info_2))
            } else {
                // User has passed all tutorials
                buttonChangeTheme.visibility = View.VISIBLE
            }
        }
    }

    private fun showInfoPanel(text: String) {
        if (infoPanelView.visibility == View.VISIBLE) { return }
        Handler().postDelayed({
            infoPanelView.alpha = 0f
            infoPanelView.text = text

            infoPanelView.animate()
                .alpha(1.0f)
                .setDuration(500)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        infoPanelView.visibility = View.VISIBLE
                    }
                })
        }, 3000)
    }

    private fun hideInfoPanel() {
        if (infoPanelView.visibility == View.VISIBLE) {

            infoPanelView.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        infoPanelView.visibility = View.INVISIBLE
                        checkTutorialRequest()
                    }
                })
        }
    }

    private fun playButtonClickSound() {
        if (audioSystem.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            buttonSound.start()
        }
    }

    private fun getDigitCount(str: String): Int {
        var count = str.length

        if (str.contains(DECIMAL_POINT)) {
            count -= 1
        }
        return count
    }
    //endregion
}

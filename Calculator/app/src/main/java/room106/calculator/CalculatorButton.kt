package room106.calculator

import android.content.Context
import android.os.Debug
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import java.sql.Types

class CalculatorButton: RelativeLayout {

    enum class Type(val value: Int) {
        DIGIT(0),      //  [1] [2] ... [9]
        OPERATION(1),   //  [+] [-] [x] [/]
        MEMORY(2),      //  [81] [123] [+]
        ERASE(3),       //  [C]
        CALCULATION(4); //  [=]

        companion object {
            private val values = values();
            fun getByValue(value: Int) = values.firstOrNull { it.value == value }
        }
    }


    private lateinit var buttonLayout: RelativeLayout
    private lateinit var buttonText: TextView
    private lateinit var buttonImage: ImageView

    var type: Type? = null

    constructor(context: Context?, type: Type) : super(context) {
        initializeView(context)
        this.type = type
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initializeView(context)
        implementAttrs(context, attrs)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initializeView(context)
        implementAttrs(context, attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initializeView(context)
        implementAttrs(context, attrs)
    }

    private fun initializeView(context: Context?) {
        View.inflate(context, R.layout.calculator_button_layout, this)

        buttonLayout = findViewById(R.id.buttonLayout)
        buttonText = findViewById(R.id.buttonText)
        buttonImage = findViewById(R.id.buttonImage)
    }

    private fun implementAttrs(context: Context?, attrs: AttributeSet?) {
        context!!.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CalculatorButton,
            0, 0).apply {

            try {
                buttonLayout.setBackgroundResource(getResourceId(R.styleable.CalculatorButton_buttonBackground, 0))
                buttonText.text = getString(R.styleable.CalculatorButton_text)
                buttonImage.setImageResource(getResourceId(R.styleable.CalculatorButton_src, 0))

                type = Type.getByValue(getInt(R.styleable.CalculatorButton_type, 0))

            } finally {
                recycle()
            }
        }
    }

    fun getText(): String {
        return if (type!! == Type.DIGIT) {
            buttonText.text.toString()
        } else {
            ""
        }
    }

}
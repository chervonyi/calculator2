package room106.calculator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class CalculatorButton: RelativeLayout {


    private lateinit var buttonLayout: RelativeLayout
    private lateinit var buttonText: TextView
    private lateinit var buttonImage: ImageView

    constructor(context: Context?) : super(context) {
        initializeView(context)
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
            } finally {
                recycle()
            }
        }
    }
}
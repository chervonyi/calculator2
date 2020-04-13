package room106.calculator

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class InfoPanelView : LinearLayout {

    private lateinit var infoPanel: LinearLayout
    private lateinit var infoImage: ImageView
    private lateinit var infoText: TextView

    private lateinit var mPreferredThemeManager: PreferredThemeManager

    //region Constructors
    constructor(context: Context?) : super(context) {
        initializeView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initializeView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initializeView(context)
    }
    //endregion

    private fun initializeView(context: Context?) {
        View.inflate(context, R.layout.info_panel_layout, this)
        infoPanel = findViewById(R.id.infoPanel)
        infoImage = findViewById(R.id.infoImage)
        infoText = findViewById(R.id.infoText)

        mPreferredThemeManager = PreferredThemeManager(context!!)
        if (mPreferredThemeManager.getPreferredTheme() == PreferredThemeManager.Mode.DarkTheme) {
            infoImage.setImageResource(R.drawable.ic_info_black)
        } else {
            infoImage.setImageResource(R.drawable.ic_info_white)
        }
    }

    var text: String
        get() {
            return infoText.text.toString()
        }
        set(value) {
            infoText.text = value
        }



}
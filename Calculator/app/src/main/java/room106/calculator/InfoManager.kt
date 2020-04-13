package room106.calculator

import android.content.Context
import android.content.SharedPreferences

class InfoManager(context: Context) {

    private val SHARED_PREFERENCES_KEY = "info_shared_preferences_key"
    private val INFO1_KEY = "info1_key"
    private val INFO2_KEY = "info2_key"

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)

    var hasInfo1ShownBefore: Boolean
        get() {
            return sharedPreferences.getBoolean(INFO1_KEY, false)
        }

        set(value) {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean(INFO1_KEY, value)
            editor.apply()
        }

    var hasInfo2ShownBefore: Boolean
        get() {
            return sharedPreferences.getBoolean(INFO2_KEY, false)
        }

        set(value) {
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean(INFO2_KEY, value)
            editor.apply()
        }

}
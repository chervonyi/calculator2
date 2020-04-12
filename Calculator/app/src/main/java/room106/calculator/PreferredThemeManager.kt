package room106.calculator

import android.content.Context
import android.content.SharedPreferences

class PreferredThemeManager(context: Context) {

    enum class Mode(val code: Int) {
        LightTheme(1),
        DarkTheme(2)
    }

    private val SHARED_PREFERENCES_KEY = "shared_preferences_key"
    private val PREFERRED_THEME_KEY = "preferred_theme_key"

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)

    private fun savePreferredTheme(newTheme: Mode) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(PREFERRED_THEME_KEY, newTheme.code)
        editor.apply()
    }

    fun getPreferredTheme(): Mode {
        // Put "1" to set the Light Mode as default theme
        // Put "2" to set the Dark Mode as default theme
        val code = sharedPreferences.getInt(PREFERRED_THEME_KEY, 1) // Light Mode as default theme
        return if (code == 2) {
            Mode.DarkTheme
        } else {
            Mode.LightTheme
        }
    }

    fun changePreferredTheme() {
        if (getPreferredTheme() == Mode.LightTheme) {
            savePreferredTheme(Mode.DarkTheme)
        } else {
            savePreferredTheme(Mode.LightTheme)
        }
    }
}
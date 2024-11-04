package com.example.monumentalfinder.ui.settings

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.monumentalfinder.R
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import android.widget.Switch
import java.util.Locale
import android.content.res.Configuration
import com.example.monumentalfinder.MyFirebaseMessagingService

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var btnLeave: Button
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var languageSettingsBtn: Button
    private lateinit var themeSwitch: Switch
    private lateinit var offlineSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        // Find the exit button
        val exitButton: Button = rootView.findViewById(R.id.exitBtn)
        exitButton.setOnClickListener {
            requireActivity().finishAffinity()
        }

        // Initialize the "Language Settings" button
        languageSettingsBtn = rootView.findViewById(R.id.languageSettingsBtn)
        languageSettingsBtn.setOnClickListener {
            showLanguageDialog()
        }

        offlineSwitch = rootView.findViewById(R.id.offlineSwitch)

        themeSwitch = rootView.findViewById(R.id.themeSwitch)

        // Load saved theme preference
        val isDarkModeEnabled = loadThemePreference(requireContext())
        themeSwitch.isChecked = isDarkModeEnabled
        updateTheme(isDarkModeEnabled)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveThemePreference(requireContext(), isChecked)
            updateTheme(isChecked)
        }

        offlineSwitch.setOnClickListener { offlineModeSwitch() }

        return rootView
    }

    private fun offlineModeSwitch() {
        val title = if (offlineSwitch.isChecked) {
            "Offline"
        } else {
            "Online"
        }
        val message = if (offlineSwitch.isChecked) {
            "You've entered offline mode"
        } else {
            "You've entered online mode" // Updated message for online state
        }

        MyFirebaseMessagingService.showNotification(
            requireContext(),
            title,
            message
        )
    }


    private fun updateTheme(isDarkMode: Boolean) {
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun loadThemePreference(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("dark_mode", true)
    }

    private fun saveThemePreference(context: Context, isDarkMode: Boolean) {
        val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("dark_mode", isDarkMode)
            apply()
        }
    }

    // Function to display language selection dialog
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Afrikaans")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                val selectedLanguage = if (which == 0) "en" else "af"
                setLocale(requireContext(), selectedLanguage)
                requireActivity().recreate() // Refresh the activity to apply the language change
            }
            .create()
            .show()
    }

    // Function to apply the selected locale and restart the activity
    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}

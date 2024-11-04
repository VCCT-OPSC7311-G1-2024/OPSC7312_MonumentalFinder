package com.example.monumentalfinder.ui.settings

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

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private lateinit var btnLeave: Button
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var themeSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        // Find the exit button
        val exitButton: Button = rootView.findViewById(R.id.exitBtn)

        // Set an OnClickListener on the button
        exitButton.setOnClickListener {
            // Exit the application
            requireActivity().finishAffinity()
        }

        themeSwitch = rootView.findViewById(R.id.themeSwitch)

        // Load saved theme preference
        val isDarkModeEnabled = loadThemePreference(requireContext())
        themeSwitch.isChecked = isDarkModeEnabled

        // Set the current theme based on the switch
        updateTheme(isDarkModeEnabled)

        // Set up listener for the switch
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveThemePreference(requireContext(), isChecked)
            updateTheme(isChecked)
        }

        return rootView
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
        return sharedPref.getBoolean("dark_mode", true) // Default to dark mode
    }

    private fun saveThemePreference(context: Context, isDarkMode: Boolean) {
        val sharedPref = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("dark_mode", isDarkMode)
            apply()
        }
    }
}
package com.example.monumentalfinder.ui.wheel

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.monumentalfinder.MyFirebaseMessagingService
import kotlin.random.Random
import com.example.monumentalfinder.R
import com.example.monumentalfinder.SpinnerView

class WheelFragment : Fragment() {

    private lateinit var wheelImage: SpinnerView
    private lateinit var spinButton: Button
    private lateinit var resultText: TextView
    private lateinit var spinsRemainingText: TextView // New TextView for spins remaining
    private val wheelSections = arrayOf("Gift Card", "Re-Spin", "Nothing", "R10", "+2 Bingo", "Gift Card +")

    // Define spin limit
    private var spinLimit = 0
    private var currentSpinCount = 0 // Counter for current spins

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wheel, container, false)

        wheelImage = view.findViewById(R.id.spinnerView)
        spinButton = view.findViewById(R.id.spinButton)
        resultText = view.findViewById(R.id.resultText)
        spinsRemainingText = view.findViewById(R.id.spinsRemainingText) // Initialize spins remaining TextView

        loadSpinCount()

        // Initialize spins remaining display
        spinsRemainingText.text = "Spins Remaining: ${spinLimit}"

        spinButton.setOnClickListener {
            if (currentSpinCount < spinLimit) { // Check if spin limit is not reached
                spinWheel()
            } else {
                MyFirebaseMessagingService.showNotification(
                    requireContext(),
                    "Warning!",
                    "You've reached your spin limit."
                )
            }
        }

        return view
    }

    private fun loadSpinCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        spinLimit = sharedPreferences.getInt("spin_count", 0) // Default to 0 if not set
    }

    private fun saveSpinCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("spin_count", spinLimit)
            apply()
        }
    }

    private fun spinWheel() {
        // Random angle for the spin (between 0 and 360 degrees)
        val randomAngle = (0..360).random().toFloat()
        val totalRotation = 3600f + randomAngle // Total rotation

        // Animate the wheel spin
        wheelImage.animate()
            .rotationBy(totalRotation)
            .setDuration(3000)
            .withEndAction {
                // Calculate the actual ending angle
                val endingAngle = (totalRotation % 360)

                // Get the index based on the ending angle
                val sectionIndex = getSectionFromAngle(endingAngle)
                displayResult(sectionIndex)

                // Increment the spin counter
                currentSpinCount++
                spinLimit -= currentSpinCount
                spinsRemainingText.text = "Spins Remaining: ${spinLimit}" // Update spins remaining text

                saveSpinCount()
            }
            .start()
    }

    private fun getSectionFromAngle(angle: Float): Int {
        // Assuming equal sections, calculate which section the angle falls into
        val sectionSize = 360 / wheelSections.size
        return (angle / sectionSize).toInt()
    }

    private fun displayResult(sectionIndex: Int) {
        val result = wheelSections[sectionIndex % wheelSections.size] // Use wheelSections for results
        resultText.text = "Congratulations! You landed on: $result"
    }
}
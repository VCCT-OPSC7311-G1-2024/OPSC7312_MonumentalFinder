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
import kotlin.random.Random
import com.example.monumentalfinder.R
import com.example.monumentalfinder.SpinnerView

class WheelFragment : Fragment() {

    private lateinit var wheelImage: SpinnerView
    private lateinit var spinButton: Button
    private lateinit var resultText: TextView
    private val wheelSections = arrayOf("Prize 1", "Prize 2", "Prize 3", "Prize 4", "Prize 5")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_wheel, container, false)

        wheelImage = view.findViewById(R.id.spinnerView)
        spinButton = view.findViewById(R.id.spinButton)
        resultText = view.findViewById(R.id.resultText)

        spinButton.setOnClickListener {
            spinWheel()
        }

        return view
    }

    private fun spinWheel() {
        // Random angle for the spin (between 0 and 360 degrees)
        val randomAngle = (0..360).random().toFloat()

        // Animate the wheel spin
        wheelImage.animate()
            .rotationBy(3600f + randomAngle) // 3600 for 10 full rotations, plus random angle
            .setDuration(3000)
            .withEndAction {
                val sectionIndex = getSectionFromAngle(randomAngle)
                displayResult(sectionIndex)
            }
            .start()
    }

    private fun getSectionFromAngle(angle: Float): Int {
        // Assuming equal sections, calculate which section the angle falls into
        val sectionSize = 360 / wheelSections.size
        return (angle / sectionSize).toInt()
    }

    private fun displayResult(sectionIndex: Int) {
        val sections = wheelImage.getSections()
        val result = sections[sectionIndex % sections.size]
        resultText.text = "Congratulations! You landed on: $result"
    }
}

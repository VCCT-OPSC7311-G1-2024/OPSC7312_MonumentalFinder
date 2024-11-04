package com.example.monumentalfinder.ui.bingo

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.monumentalfinder.MyFirebaseMessagingService
import com.example.monumentalfinder.R
import com.example.monumentalfinder.databinding.FragmentBingoBinding

class BingoFragment : Fragment() {

    companion object {
        fun newInstance() = BingoFragment()
    }

    private val viewModel: BingoViewModel by viewModels()
    private var _binding: FragmentBingoBinding? = null
    private val binding get() = _binding!!
    private var crossesRemaining = 5 // Limit for the number of crosses
    private lateinit var crossesRemainingText: TextView
    private lateinit var bingoGrid: GridLayout

    private var spinCount: Int = 0

    private val crossedOffCells = mutableSetOf<Int>() // To track crossed-off cells

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBingoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crossesRemainingText = binding.crossesRemainingText
        bingoGrid = binding.bingoGrid

        loadSpinCount()

        setupBingoGrid()
        setupResetButton()
    }

    private fun loadSpinCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        spinCount = sharedPreferences.getInt("spin_count", 0) // Default to 0 if not set
    }

    private fun saveSpinCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("spin_count", spinCount)
            apply()
        }
    }

    private fun setupBingoGrid() {
        val placeTypes = listOf("Museum", "Park", "Restaurant", "Zoo", "Monument", "Library", "Gallery", "Beach", "Mountain", "Lake")
        val cellSize = resources.displayMetrics.widthPixels / 5 // Adjusts each cell to fit 5 per row

        for (i in 0 until 25) { // 5x5 grid, 25 cells
            val place = placeTypes.random()

            // Create FrameLayout to hold TextView and ImageView
            val cellContainer = FrameLayout(requireContext()).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    setMargins(4, 4, 4, 4)
                }
            }

            // TextView for the place name with background drawable
            val cellTextView = TextView(requireContext()).apply {
                text = place
                setBackgroundResource(getBackgroundDrawable(place))
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            // ImageView for the "X" overlay, initially invisible
            val overlayImageView = ImageView(requireContext()).apply {
                setImageResource(R.drawable.bingo_cell_x_overlay) // Overlay image resource with "X"
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                visibility = View.GONE // Hide initially
            }

            // Click listener to cross off the place
            cellContainer.setOnClickListener {
                if (crossesRemaining > 0 && overlayImageView.visibility == View.GONE) {
                    overlayImageView.visibility = View.VISIBLE // Show overlay
                    crossesRemaining-- // Decrease remaining crosses
                    crossesRemainingText.text = "Crosses: $crossesRemaining" // Update the displayed count
                    crossedOffCells.add(i) // Track crossed cell

                    if (checkBingo()) {
                        MyFirebaseMessagingService.showNotification(
                            requireContext(),
                            "Bingo!!",
                            "You've earned an extra spin."
                        )
                        addExtraSpin() // Add an extra spin if Bingo is achieved
                    }
                }
            }

            // Add TextView and overlay ImageView to FrameLayout
            cellContainer.addView(cellTextView)
            cellContainer.addView(overlayImageView)

            // Add FrameLayout (cell) to the GridLayout
            bingoGrid.addView(cellContainer)
        }
    }

    private fun checkBingo(): Boolean {
        // Check rows
        for (row in 0 until 5) {
            if ((0..4).all { crossedOffCells.contains(row * 5 + it) }) {
                return true
            }
        }
        // Check columns
        for (col in 0 until 5) {
            if ((0..4).all { crossedOffCells.contains(it * 5 + col) }) {
                return true
            }
        }
        // Check diagonals
        if ((0..4).all { crossedOffCells.contains(it * 6) }) { // Main diagonal
            return true
        }
        if ((1..5).all { crossedOffCells.contains(it * 4) }) { // Anti-diagonal
            return true
        }
        return false
    }

    private fun addExtraSpin() {
        spinCount++
        saveSpinCount()
    }

    private fun setupResetButton() {
        val resetButton: Button = binding.resetButton
        resetButton.setOnClickListener {
            crossesRemaining = 5 // Reset the crosses count
            crossesRemainingText.text = "Crosses: $crossesRemaining" // Update the displayed count
            bingoGrid.removeAllViews()
            setupBingoGrid() // Reset the grid
            crossedOffCells.clear() // Clear crossed cells
        }
    }

    // Helper method to return drawable background based on place type
    private fun getBackgroundDrawable(placeType: String): Int {
        return when (placeType) {
            "Restaurant" -> R.drawable.restaurant_background
            "Park" -> R.drawable.park_background
            "Museum" -> R.drawable.museum_background
            "Zoo" -> R.drawable.zoo_background
            "Monument" -> R.drawable.monument_background
            "Library" -> R.drawable.library_background
            "Gallery" -> R.drawable.gallery_background
            "Beach" -> R.drawable.beach_background
            "Mountain" -> R.drawable.mountain_background
            "Lake" -> R.drawable.lake_background
            else -> R.drawable.default_background
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

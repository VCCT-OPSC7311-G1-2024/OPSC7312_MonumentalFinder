package com.example.monumentalfinder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

public class SpinnerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val sections = listOf("Gift Card", "Re-Spin", "Nothing", "R10", "+2 Bingo", "Gift Card +")
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 40f // Adjust text size as needed
        textAlign = Paint.Align.CENTER
        color = Color.BLACK
    }
    private val sectionPaints = listOf(
        Paint().apply { color = Color.parseColor("#F7EF8A") },
        Paint().apply { color = Color.parseColor("#A5A9B4") },
        Paint().apply { color = Color.parseColor("#F7EF8A") },
        Paint().apply { color = Color.parseColor("#A5A9B4") },
        Paint().apply { color = Color.parseColor("#F7EF8A") },
        Paint().apply { color = Color.parseColor("#A5A9B4") }
    )
    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate the size of the wheel
        val diameter = min(width, height).toFloat()
        val radius = diameter / 2
        rectF.set(0f, 0f, diameter, diameter)

        // Calculate angle per section
        val anglePerSection = 360f / sections.size

        // Draw each section
        for (i in sections.indices) {
            // Draw the colored section
            canvas.drawArc(rectF, i * anglePerSection, anglePerSection, true, sectionPaints[i % sectionPaints.size])

            // Set text color based on background color for better visibility
            paint.color = if (sectionPaints[i % sectionPaints.size].color == Color.parseColor("#A5A9B4")) {
                Color.BLACK // Dark text on light background
            } else {
                Color.WHITE // Light text on dark background
            }

            // Draw the text on the section
            drawTextOnArc(canvas, sections[i], i * anglePerSection, anglePerSection, radius, paint)
        }

        // Draw central circle
        paint.color = Color.BLACK
        canvas.drawCircle(radius, radius, radius / 10, paint)
    }

    private fun drawTextOnArc(canvas: Canvas, text: String, startAngle: Float, sweepAngle: Float, radius: Float, paint: Paint) {
        val textOffset = 30f // Slightly adjust for better alignment (increase/decrease as needed)
        val angleRadians = Math.toRadians((startAngle + sweepAngle / 2 + textOffset).toDouble()) // Adjust angle for alignment
        val textRadius = radius * 0.7f // Position the text inside the section (adjust multiplier for positioning)

        // Calculate the x and y position for the text
        val xPos = (width / 2 + textRadius * cos(angleRadians)).toFloat()
        val yPos = (height / 2 + textRadius * sin(angleRadians)).toFloat()

        // Save the current state of the canvas before rotating
        canvas.save()

        // Move the canvas to the center of the wheel
        canvas.translate(width / 2f, height / 2f)

        // Rotate the canvas so that the text is aligned with the section
        canvas.rotate(startAngle + sweepAngle / 2 + textOffset)

        // Draw the text
        canvas.drawText(text, 0f, -textRadius, paint)

        // Restore the canvas to its previous state
        canvas.restore()
    }

    fun getSectionAtAngle(angle: Float): String {
        val anglePerSection = 360f / sections.size
        val sectionIndex = (angle / anglePerSection).toInt() % sections.size
        return sections[sectionIndex]
    }

    fun getSections(): List<String> {
        return sections
    }
}

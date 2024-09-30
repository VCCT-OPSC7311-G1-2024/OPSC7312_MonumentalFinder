package com.example.monumentalfinder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

public class SpinnerView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val sections = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5")
    private val paint = Paint()
    private val sectionPaints = listOf(
        Paint().apply { color = Color.RED },
        Paint().apply { color = Color.GREEN },
        Paint().apply { color = Color.BLUE },
        Paint().apply { color = Color.YELLOW },
        Paint().apply { color = Color.MAGENTA }
    )
    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate the size of the wheel
        val diameter = Math.min(width, height).toFloat()
        rectF.set(0f, 0f, diameter, diameter)

        // Calculate angle per section
        val anglePerSection = 360f / sections.size

        // Draw each section
        for (i in sections.indices) {
            canvas.drawArc(rectF, i * anglePerSection, anglePerSection, true, sectionPaints[i % sectionPaints.size])
        }

        // Draw central circle
        paint.color = Color.BLACK
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 10, paint)
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
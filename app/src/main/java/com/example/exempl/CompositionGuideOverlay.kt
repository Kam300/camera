package com.example.exempl

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun CompositionGuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRuleOfThirdsLines()
    }
}

private fun DrawScope.drawRuleOfThirdsLines() {
    val width = size.width
    val height = size.height

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    // Вертикальные линии
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = androidx.compose.ui.geometry.Offset(width / 3, 0f),
        end = androidx.compose.ui.geometry.Offset(width / 3, height),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )

    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = androidx.compose.ui.geometry.Offset(width * 2 / 3, 0f),
        end = androidx.compose.ui.geometry.Offset(width * 2 / 3, height),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )

    // Горизонтальные линии
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = androidx.compose.ui.geometry.Offset(0f, height / 3),
        end = androidx.compose.ui.geometry.Offset(width, height / 3),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )

    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = androidx.compose.ui.geometry.Offset(0f, height * 2 / 3),
        end = androidx.compose.ui.geometry.Offset(width, height * 2 / 3),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )
}

package com.example.exempl

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.abs

class PhotoCompositionAnalyzer(private val context: Context) {

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .build()
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    private var lastGuidanceTime = 0L
    private val guidanceInterval = 3000L // 3 секунды между подсказками

    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy, onGuidanceGenerated: (String) -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastGuidanceTime < guidanceInterval) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            objectDetector.process(image)
                .addOnSuccessListener { objects ->
                    poseDetector.process(image)
                        .addOnSuccessListener { pose ->
                            val guidance = generateCompositionGuidance(objects, pose, imageProxy)
                            if (guidance.isNotEmpty()) {
                                lastGuidanceTime = currentTime
                                onGuidanceGenerated(guidance)
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }
                .addOnFailureListener {
                    imageProxy.close()
                }
        }
    }

    private fun generateCompositionGuidance(
        objects: List<com.google.mlkit.vision.objects.DetectedObject>,
        pose: com.google.mlkit.vision.pose.Pose,
        imageProxy: ImageProxy
    ): String {
        val width = imageProxy.width
        val height = imageProxy.height

        // Находим главный объект (человека)
        val mainSubject = objects.firstOrNull { obj ->
            obj.labels.any { it.text == "Person" && it.confidence > 0.7f }
        }

        return when {
            mainSubject == null -> {
                if (objects.isNotEmpty()) {
                    "Объект найден! Попробуйте расположить его по правилу третей"
                } else {
                    "Ищу объекты для съемки..."
                }
            }

            // Проверка правила третей
            !isInRuleOfThirds(mainSubject.boundingBox, width, height) -> {
                "Переместите объект ближе к линиям сетки для лучшей композиции"
            }

            // Проверка центровки
            isObjectCentered(mainSubject.boundingBox, width, height) -> {
                "Попробуйте сместить объект из центра кадра"
            }

            // Проверка расстояния
            isObjectTooSmall(mainSubject.boundingBox, width, height) -> {
                "Подойдите ближе к объекту съемки"
            }

            isObjectTooLarge(mainSubject.boundingBox, width, height) -> {
                "Отойдите немного дальше от объекта"
            }

            // Проверка ориентации
            shouldSuggestPortrait(mainSubject.boundingBox) -> {
                "Поверните телефон вертикально для лучшего кадра"
            }

            shouldSuggestLandscape(mainSubject.boundingBox) -> {
                "Поверните телефон горизонтально для лучшего кадра"
            }

            else -> {
                "Отличная композиция! Готов к съемке"
            }
        }
    }

    private fun isInRuleOfThirds(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val centerX = boundingBox.centerX().toFloat() / width
        val centerY = boundingBox.centerY().toFloat() / height

        val thirdLines = listOf(0.33f, 0.67f)

        return thirdLines.any { line ->
            abs(centerX - line) < 0.15f || abs(centerY - line) < 0.15f
        }
    }

    private fun isObjectCentered(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val centerX = boundingBox.centerX().toFloat() / width
        val centerY = boundingBox.centerY().toFloat() / height

        return abs(centerX - 0.5f) < 0.1f && abs(centerY - 0.5f) < 0.1f
    }

    private fun isObjectTooSmall(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val objectArea = boundingBox.width() * boundingBox.height()
        val totalArea = width * height
        return (objectArea.toFloat() / totalArea) < 0.08f
    }

    private fun isObjectTooLarge(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val objectArea = boundingBox.width() * boundingBox.height()
        val totalArea = width * height
        return (objectArea.toFloat() / totalArea) > 0.75f
    }

    private fun shouldSuggestPortrait(boundingBox: android.graphics.Rect): Boolean {
        return boundingBox.height() > boundingBox.width() * 1.4f
    }

    private fun shouldSuggestLandscape(boundingBox: android.graphics.Rect): Boolean {
        return boundingBox.width() > boundingBox.height() * 1.4f
    }
}
